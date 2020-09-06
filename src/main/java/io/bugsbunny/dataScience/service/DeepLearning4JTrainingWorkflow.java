package io.bugsbunny.dataScience.service;

import com.google.gson.JsonObject;
import io.bugsbunny.persistence.MongoDBJsonStore;
import io.bugsbunny.pipeline.ModelDeployer;
import io.bugsbunny.restClient.ElasticSearchClient;
import io.bugsbunny.restclient.MLFlowRunClient;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;


//import org.castor.core.util.Base64Encoder;


@ApplicationScoped
public class DeepLearning4JTrainingWorkflow extends TrainingWorkflowBase
{
    private static Logger log = LoggerFactory.getLogger(TensorFlowTrainingWorkflow.class);

    @Inject
    private MongoDBJsonStore mongoDBJsonStore;

    @Inject
    private ElasticSearchClient elasticSearchClient;

    @Inject
    private ModelDeployer modelDeployer;

    @Inject
    private MLFlowRunClient mlFlowRunClient;

    /*private Map<Integer,String> eats = new HashMap<>();
    private Map<Integer,String> sounds = new HashMap<>();
    private Map<Integer,String> classifiers = new HashMap<>();*/

    @Override
    public String startTraining(JsonObject trainingMetaData)
    {
        ByteArrayOutputStream modelStream = null;
        ObjectOutputStream out = null;
        try
        {
            String runId = this.mlFlowRunClient.createRun();;
            String script = trainingMetaData.get("script").getAsString();

            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("artifact_path", "model");
            jsonObject.addProperty("utc_time_created", "2020-06-26 18:00:56.056775");
            jsonObject.addProperty("run_id", runId);
            jsonObject.add("flavors", new JsonObject());
            jsonObject.addProperty("mlPlatform", trainingMetaData.get("mlPlatform").getAsString());
            jsonObject.addProperty("script", script);
            //jsonObject.addProperty("data", trainingMetaData.get("data").getAsString());

            //this.testClassifier();

            //Process the Training Results
            //TODO: Delete this file once it is entered into the Repositories
            String model = IOUtils.toString(new FileInputStream("devModel/1/saved_model.pb"),
                    StandardCharsets.UTF_8);

            //Register the Trained Model with the DataBricks Repository
            modelStream = new ByteArrayOutputStream();
            out = new ObjectOutputStream(modelStream);
            out.writeObject(model);

            jsonObject.addProperty("modelSer", Base64.getEncoder().encodeToString(modelStream.toByteArray()));

            String json = jsonObject.toString();
            this.mlFlowRunClient.logModel(runId, json);

            //Register the Trained Model with the BugsBunny Repository
            this.mongoDBJsonStore.storeDevModels(jsonObject);

            return runId;
        }
        catch(Exception e)
        {
            log.info(e.getMessage(), e);
            throw new RuntimeException(e);
        }
        finally
        {
            if(out != null)
            {
                try
                {
                    out.close();
                }
                catch(IOException ioe)
                {
                    //Tried to cleanup..no biggie no biggie if still problemo time (lol)
                }
            }
            if(modelStream != null)
            {
                try
                {
                    modelStream.close();
                }
                catch(IOException ioe)
                {
                    //Tried to cleanup..no biggie no biggie if still problemo time (lol)
                }
            }
        }
    }

    @Override
    public String getData(String runId)
    {
        JsonObject devModel = this.mongoDBJsonStore.getDevModel(runId);
        if(devModel != null && devModel.has("data"))
        {
            String data = devModel.get("data").getAsString();
            return data;
        }
        return null;
    }

    //--------------------------------------------------------------------
    /*private void testClassifier() throws Exception
    {

        try {

            //Second: the RecordReaderDataSetIterator handles conversion to DataSet objects, ready for use in neural network
            int labelIndex = 4;     //5 values in each row of the iris.txt CSV: 4 input features followed by an integer label (class) index. Labels are the 5th value (index 4) in each row
            int numClasses = 3;     //3 classes (types of iris flowers) in the iris data set. Classes have integer values 0, 1 or 2

            int batchSizeTraining = 30;    //Iris data set: 150 examples total. We are loading all of them into one DataSet (not recommended for large data sets)
            DataSet trainingData = this.readCSVDataset(batchSizeTraining, labelIndex, numClasses);

            // this is the data we want to classify
            int batchSizeTest = 44;
            DataSet testData = this.readCSVDataset(batchSizeTest, labelIndex, numClasses);


            // make the data model for records prior to normalization, because it
            // changes the data.
            Map<Integer,Map<String,Object>> animals = makeAnimalsForTesting(testData);


            //We need to normalize our data. We'll use NormalizeStandardize (which gives us mean 0, unit variance):
            DataNormalization normalizer = new NormalizerStandardize();
            normalizer.fit(trainingData);           //Collect the statistics (mean/stdev) from the training data. This does not modify the input data
            normalizer.transform(trainingData);     //Apply normalization to the training data
            normalizer.transform(testData);         //Apply normalization to the test data. This is using statistics calculated from the *training* set

            final int numInputs = 4;
            int outputNum = 3;
            int iterations = 1000;
            long seed = 6;

            log.info("Build model....");
            MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
                    .seed(seed)
                    .iterations(iterations)
                    .activation(Activation.TANH)
                    .weightInit(WeightInit.XAVIER)
                    .learningRate(0.1)
                    .regularization(true).l2(1e-4)
                    .list()
                    .layer(0, new DenseLayer.Builder().nIn(numInputs).nOut(3).build())
                    .layer(1, new DenseLayer.Builder().nIn(3).nOut(3).build())
                    .layer(2, new OutputLayer.Builder(LossFunctions.LossFunction.NEGATIVELOGLIKELIHOOD)
                            .activation(Activation.SOFTMAX).nIn(3).nOut(outputNum).build())
                    .backprop(true).pretrain(false)
                    .build();

            //run the model
            MultiLayerNetwork model = new MultiLayerNetwork(conf);
            model.init();
            model.setListeners(new ScoreIterationListener(100));

            model.fit(trainingData);

            //evaluate the model on the test set
            Evaluation eval = new Evaluation(3);
            INDArray output = model.output(testData.getFeatureMatrix());

            eval.eval(testData.getLabels(), output);
            log.info(eval.stats());

            setFittedClassifiers(output, animals);
            logAnimals(animals);

            ByteArrayOutputStream modelStream = null;
            ObjectOutputStream out = null;
            try {
                modelStream = new ByteArrayOutputStream();
                out = new ObjectOutputStream(modelStream);
                out.writeObject(model);
            }
            finally
            {
                out.close();
                modelStream.close();
            }

            //Restore serialized model
            Base64.Encoder encoder = Base64.getEncoder();
            String modelString = encoder.encodeToString(modelStream.toByteArray());
            ObjectInputStream in = null;
            MultiLayerNetwork restoredModel = null;
            try {
                Base64.Decoder decoder = Base64.getDecoder();
                in = new ObjectInputStream(new ByteArrayInputStream(decoder.decode(modelString)));
                //in = new ObjectInputStream(new ByteArrayInputStream(modelStream.toByteArray()));
                restoredModel = (MultiLayerNetwork) in.readObject();
            } finally
            {
                if(in != null) {
                    in.close();
                }
            }

            restoredModel.fit(trainingData);

            //evaluate the model on the test set
            eval = new Evaluation(3);
            output = restoredModel.output(testData.getFeatureMatrix());

            eval.eval(testData.getLabels(), output);
            log.info(eval.stats());

        } catch (Exception e){
            e.printStackTrace();
        }

    }*/

    /*private void logAnimals(Map<Integer,Map<String,Object>> animals){
        for(Map<String,Object> a:animals.values())
            log.info(a.toString());
    }*/

    /*private void setFittedClassifiers(INDArray output, Map<Integer,Map<String,Object>> animals){
        for (int i = 0; i < output.rows() ; i++) {

            // set the classification from the fitted results
            animals.get(i).put("classifier",
                    classifiers.get(maxIndex(getFloatArrayFromSlice(output.slice(i)))));

        }

    }*/

    /*private float[] getFloatArrayFromSlice(INDArray rowSlice){
        float[] result = new float[rowSlice.columns()];
        for (int i = 0; i < rowSlice.columns(); i++) {
            result[i] = rowSlice.getFloat(i);
        }
        return result;
    }*/

    /*private int maxIndex(float[] vals){
        int maxIndex = 0;
        for (int i = 1; i < vals.length; i++){
            float newnumber = vals[i];
            if ((newnumber > vals[maxIndex])){
                maxIndex = i;
            }
        }
        return maxIndex;
    }*/

    /*private Map<Integer,Map<String,Object>> makeAnimalsForTesting(DataSet testData){
        Map<Integer,Map<String,Object>> animals = new HashMap<>();

        INDArray features = testData.getFeatureMatrix();
        for (int i = 0; i < features.rows() ; i++) {
            INDArray slice = features.slice(i);
            Map<String,Object> animal = new HashMap();

            //set the attributes
            animal.put("yearsLived", slice.getInt(0));
            animal.put("eats", eats.get(slice.getInt(1)));
            animal.put("sounds", sounds.get(slice.getInt(2)));
            animal.put("weight", slice.getFloat(3));

            animals.put(i,animal);
        }
        return animals;

    }*/

    /*private Map<Integer,String> readEnumCSV() {
        try{
            List<JsonObject> jsons = this.mongoDBJsonStore.getIngestionImages();
            JsonObject json = jsons.get(0);
            String data = json.get("data").getAsString();
            InputStream is = new ByteArrayInputStream(data.getBytes(StandardCharsets.UTF_8));
            List<String> lines = IOUtils.readLines(is);
            Map<Integer,String> enums = new HashMap<>();
            for(String line:lines){
                String[] parts = line.split(",");
                enums.put(Integer.parseInt(parts[0]),parts[1]);
            }
            return enums;
        } catch (Exception e){
            e.printStackTrace();
            return null;
        }

    }*/

    /*private DataSet readCSVDataset(int batchSize, int labelIndex, int numClasses)
            throws IOException, InterruptedException{
        List<JsonObject> jsons = mongoDBJsonStore.getIngestionImages();
        JsonObject json = jsons.get(0);
        String data = json.get("data").getAsString();

        File file = new File("tmp/data");
        FileOutputStream fos = new FileOutputStream(file);
        IOUtils.write(data, fos, StandardCharsets.UTF_8);

        RecordReader rr = new CSVRecordReader();
        rr.initialize(new FileSplit(file));
        DataSetIterator iterator = new RecordReaderDataSetIterator(rr, batchSize, labelIndex, numClasses);
        return iterator.next();
    }*/
}
