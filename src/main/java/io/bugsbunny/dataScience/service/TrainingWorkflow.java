package io.bugsbunny.dataScience.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.bugsbunny.persistence.MongoDBJsonStore;
import io.bugsbunny.pipeline.ModelDeployer;
import io.bugsbunny.restClient.ElasticSearchClient;
import org.apache.commons.io.IOUtils;
import org.datavec.api.records.reader.RecordReader;
import org.datavec.api.records.reader.impl.csv.CSVRecordReader;
import org.datavec.api.split.FileSplit;
import org.deeplearning4j.datasets.datavec.RecordReaderDataSetIterator;
import org.deeplearning4j.eval.Evaluation;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.dataset.api.preprocessor.DataNormalization;
import org.nd4j.linalg.dataset.api.preprocessor.NormalizerStandardize;
import org.nd4j.linalg.lossfunctions.LossFunctions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class TrainingWorkflow {
    private static Logger logger = LoggerFactory.getLogger(TrainingWorkflow.class);

    @Inject
    private MongoDBJsonStore mongoDBJsonStore;

    @Inject
    private ElasticSearchClient elasticSearchClient;

    @Inject
    private ModelDeployer modelDeployer;

    private Map<Integer,String> eats = new HashMap<>();
    private Map<Integer,String> sounds = new HashMap<>();
    private Map<Integer,String> classifiers = new HashMap<>();

    public String startTraining()
    {
        try
        {
            String runId = null;

            this.eats = this.readEnumCSV();
            this.sounds = this.readEnumCSV();
            this.classifiers = this.readEnumCSV();

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

            logger.info("Build model....");
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
            //logger.info(eval.stats());

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

            //Store the model in the DataBricks Repository
            //runId = this.mlFlowRunClient.createRun();


            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("artifact_path", "model");
            jsonObject.addProperty("utc_time_created", "2020-06-26 18:00:56.056775");
            jsonObject.addProperty("run_id", runId);
            jsonObject.add("flavors", new JsonObject());
            jsonObject.addProperty("modelSer", Base64.getEncoder().encodeToString(modelStream.toByteArray()));

            String json = jsonObject.toString();

            logger.info("*********************************************");
            logger.info(json);
            logger.info("*********************************************");

            //this.mlFlowRunClient.logModel(runId, json);
            this.mongoDBJsonStore.storeDevModels(jsonObject);

            return runId;
        } catch (Exception e){
            logger.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public String startTrainingTensorFlowModel()
    {
        try {
            String runId = "blah";

            this.modelDeployer.deployPythonTraining();
            String model = IOUtils.toString(new FileInputStream("devModel/1/saved_model.pb"),
                    StandardCharsets.UTF_8);

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

            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("artifact_path", "model");
            jsonObject.addProperty("utc_time_created", "2020-06-26 18:00:56.056775");
            jsonObject.addProperty("run_id", runId);
            jsonObject.add("flavors", new JsonObject());
            jsonObject.addProperty("mlPlatform", "tensorflow");
            //jsonObject.addProperty("modelSer", Base64.getEncoder().encodeToString(modelStream.toByteArray()));

            String json = jsonObject.toString();

            logger.info("*********************************************");
            logger.info(json);
            logger.info("*********************************************");

            //this.mlFlowRunClient.logModel(runId, json);
            this.mongoDBJsonStore.storeDevModels(jsonObject);

            return runId;
        }
        catch(Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    public void updateIndex() throws IOException
    {
        JsonArray dataSet = this.mongoDBJsonStore.getIngestedDataSet();
        logger.info(this.elasticSearchClient.updateIndex(dataSet));
    }

    //--------------------------------------------------------------------------------------------------------------------
    private String readIngestedData()
    {
        List<JsonObject> jsons = mongoDBJsonStore.getIngestionImages();
        String data = "";
        int length = jsons.size();
        int counter = 0;
        for(JsonObject local:jsons)
        {
            data += local.get("data").getAsString();
            counter++;
            if(counter < length)
            {
                data += "\n";
            }
        }
        return data.trim();
    }

    private DataSet readCSVDataset(int batchSize, int labelIndex, int numClasses) throws IOException, InterruptedException
    {
        //make this hdfs
        File file = new File("tmp/data");
        FileOutputStream fos = new FileOutputStream(file);

        String data = this.readIngestedData();
        //logger.info(data);

        IOUtils.write(data, fos, StandardCharsets.UTF_8);
        RecordReader rr = new CSVRecordReader();
        rr.initialize(new FileSplit(file));
        DataSetIterator iterator = new RecordReaderDataSetIterator(rr, batchSize, labelIndex, numClasses);
        return iterator.next();
    }

    private Map<Integer,String> readEnumCSV() throws IOException
    {
        String data = this.readIngestedData();
        //logger.info(data);

        InputStream is = new ByteArrayInputStream(data.getBytes(StandardCharsets.UTF_8));
        List<String> lines = IOUtils.readLines(is);
        Map<Integer,String> enums = new HashMap<>();
        for(String line:lines)
        {
            String[] parts = line.split(",");
            if(parts[0].trim().length()==0)
            {
                break;
            }
            enums.put(Integer.parseInt(parts[0]),parts[1]);
        }
        return enums;
    }

    private Map<Integer,Map<String,Object>> makeAnimalsForTesting(DataSet testData)
    {
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
    }

    private int maxIndex(float[] vals)
    {
        int maxIndex = 0;
        for (int i = 1; i < vals.length; i++){
            float newnumber = vals[i];
            if ((newnumber > vals[maxIndex])){
                maxIndex = i;
            }
        }
        return maxIndex;
    }

    private float[] getFloatArrayFromSlice(INDArray rowSlice)
    {
        float[] result = new float[rowSlice.columns()];
        for (int i = 0; i < rowSlice.columns(); i++) {
            result[i] = rowSlice.getFloat(i);
        }
        return result;
    }

    private void logAnimals(Map<Integer,Map<String,Object>> animals){
        for(Map<String,Object> a:animals.values())
            logger.info(a.toString());
    }

    private void setFittedClassifiers(INDArray output, Map<Integer,Map<String,Object>> animals){
        for (int i = 0; i < output.rows() ; i++) {

            // set the classification from the fitted results
            animals.get(i).put("classifier",
                    classifiers.get(maxIndex(getFloatArrayFromSlice(output.slice(i)))));

        }

    }
}
