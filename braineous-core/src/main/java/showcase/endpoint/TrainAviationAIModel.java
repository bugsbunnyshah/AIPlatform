package showcase.endpoint;

import com.google.gson.JsonObject;

import io.bugsbunny.dataScience.service.AIModelService;
import io.bugsbunny.dataScience.service.PackagingService;
import showcase.service.AviationDataIngestionService;

import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.util.ModelSerializer;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.learning.config.Sgd;
import org.nd4j.linalg.lossfunctions.LossFunctions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

@Path("aviation")
public class TrainAviationAIModel
{
    private static Logger logger = LoggerFactory.getLogger(TrainAviationAIModel.class);

    @Inject
    private AIModelService trainingAIModelService;

    @Inject
    private AviationDataIngestionService aviationDataIngestionService;

    @Inject
    private PackagingService packagingService;

    @Path("train")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response train()
    {
        try
        {
            this.aviationDataIngestionService.startIngestion();
            Thread.sleep(20000);

            List<Long> dataSetIds = this.aviationDataIngestionService.getDataSetIds();
            int counter = 100;
            while(dataSetIds.isEmpty() && counter > 0)
            {
                dataSetIds = this.aviationDataIngestionService.getDataSetIds();
                counter--;
            }

            long[] trainingDataSets = new long[dataSetIds.size()];
            for(int i=0; i<dataSetIds.size();i++)
            {
                trainingDataSets[i] = dataSetIds.get(i);
            }

            final int numInputs = 5;
            int outputNum = 5;
            long seed = 6;
            MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
                    .seed(seed)
                    .activation(Activation.TANH)
                    .weightInit(WeightInit.XAVIER)
                    .updater(new Sgd(0.1))
                    .l2(1e-4)
                    .list()
                    .layer(new DenseLayer.Builder().nIn(numInputs).nOut(outputNum)
                            .build())
                    .layer(new DenseLayer.Builder().nIn(numInputs).nOut(outputNum)
                            .build())
                    .layer( new OutputLayer.Builder(LossFunctions.LossFunction.NEGATIVELOGLIKELIHOOD)
                            .activation(Activation.SOFTMAX) //Override the global TANH activation with softmax for this layer
                            .nIn(numInputs).nOut(outputNum).build())
                    .build();

            //run the model
            MultiLayerNetwork model = new MultiLayerNetwork(conf);

            //Deploy the Model
            ByteArrayOutputStream modelBytes = new ByteArrayOutputStream();
            ModelSerializer.writeModel(model, modelBytes, false);
            String modelString = Base64.getEncoder().encodeToString(modelBytes.toByteArray());

            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("name", UUID.randomUUID().toString());
            jsonObject.addProperty("model", modelString);

            JsonObject deployedModel = this.packagingService.performPackaging(jsonObject.toString());
            long modelId = deployedModel.get("modelId").getAsLong();

            String trainingResult = this.trainingAIModelService.trainJava(modelId, trainingDataSets);

            JsonObject result = new JsonObject();
            result.addProperty("result", trainingResult);
            result.addProperty("modelId", modelId);
            Response response = Response.ok(result.toString()).build();
            return response;
        }
        catch(Exception e)
        {
            logger.error(e.getMessage(), e);
            JsonObject error = new JsonObject();
            error.addProperty("exception", e.getMessage());
            return Response.status(500).entity(error.toString()).build();
        }
    }
}