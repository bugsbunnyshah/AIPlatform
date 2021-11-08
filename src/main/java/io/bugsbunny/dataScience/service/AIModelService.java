package io.bugsbunny.dataScience.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import com.google.gson.JsonParser;
import io.bugsbunny.dataScience.dl4j.AIPlatformDataLakeIteratorFactory;
import io.bugsbunny.dataScience.model.Artifact;
import io.bugsbunny.preprocess.SecurityTokenContainer;
import jep.Interpreter;
import jep.JepException;
import jep.SharedInterpreter;

import org.apache.commons.io.FileUtils;
import org.datavec.api.records.reader.RecordReader;
import org.datavec.api.records.reader.impl.LineRecordReader;
import org.datavec.api.records.reader.impl.csv.CSVRecordReader;
import org.datavec.api.split.FileSplit;
import org.datavec.api.split.InputStreamInputSplit;
import org.datavec.api.split.ListStringSplit;
import org.datavec.api.split.StringSplit;
import org.deeplearning4j.datasets.datavec.RecordReaderDataSetIterator;
import org.deeplearning4j.datasets.iterator.MultiDataSetWrapperIterator;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.util.ModelSerializer;
import org.json.JSONObject;
import org.json.XML;
import org.nd4j.evaluation.classification.Evaluation;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;

import org.nd4j.linalg.dataset.api.iterator.MultiDataSetIterator;
import org.nd4j.linalg.dataset.api.iterator.MultiDataSetIteratorFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.*;

import io.bugsbunny.dataScience.dl4j.AIPlatformDataSetIteratorFactory;
import io.bugsbunny.infrastructure.MongoDBJsonStore;

@Singleton
public class AIModelService
{
    private static Logger logger = LoggerFactory.getLogger(AIModelService.class);

    @Inject
    private MongoDBJsonStore mongoDBJsonStore;

    @Inject
    private AIPlatformDataSetIteratorFactory aiPlatformDataSetIteratorFactory;

    @Inject
    private AIPlatformDataLakeIteratorFactory aiPlatformDataLakeIteratorFactory;

    @Inject
    private ModelDataSetService modelDataSetService;

    private Map<String, MultiLayerNetwork> activeModels;
    private Map<String, MultiLayerNetwork> trainingModels;

    @Inject
    private SecurityTokenContainer securityTokenContainer;

    public AIModelService()
    {
        this.activeModels = new HashMap<>();
        this.trainingModels = new HashMap<>();
    }

    public void deployModel(String modelId) throws ModelNotFoundException
    {
        JsonObject modelPackage = this.mongoDBJsonStore.getModelPackage(this.securityTokenContainer.getTenant(), modelId);
        if(modelPackage == null)
        {
            throw new ModelNotFoundException("MODEL_NOT_FOUND:"+modelId);
        }
        String modelString = modelPackage.get("model").getAsString();
        try {
            //Taking care of idempotency, whatever that means...I know and understand..but what a freaking word lol
            if(modelPackage.get("live").getAsBoolean())
            {
                return;
            }

            JsonObject currentModel = this.mongoDBJsonStore.getModelPackage(this.securityTokenContainer.getTenant(), modelId);
            currentModel.addProperty("model", modelString);
            this.mongoDBJsonStore.deployModel(this.securityTokenContainer.getTenant(), modelId);


            ByteArrayInputStream restoreStream = new ByteArrayInputStream(Base64.getDecoder().decode(modelString));
            MultiLayerNetwork network = ModelSerializer.restoreMultiLayerNetwork(restoreStream, true);
            this.activeModels.put(modelId, network);
            this.trainingModels.remove(modelId);
        }
        catch(Exception e)
        {
            logger.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public String evalPython(String modelId, String[] dataSetIds) throws JepException
    {
        /*String output;
        JsonObject modelPackage = this.packagingService.getModelPackage(modelId);
        String pythonScript = modelPackage.get("script").getAsString();
        try (Interpreter interp = new SharedInterpreter())
        {
            JsonArray dataSetIdArray = new JsonArray();
            for(String dataSetId:dataSetIds)
            {
                dataSetIdArray.add(dataSetId);
            }
            interp.set("dataSetId", dataSetIdArray.toString());
            interp.set("modelId", modelId);
            interp.exec(pythonScript);
            output = interp.getValue("output", String.class);
        }
        return output;*/
        return null;
    }

    public JsonArray rollOverToTraningDataSets(String modelId) throws ModelNotFoundException,ModelIsNotLive
    {
        JsonObject modelPackage = this.mongoDBJsonStore.getModelPackage(this.securityTokenContainer.getTenant(), modelId);
        if(modelPackage == null)
        {
            throw new ModelNotFoundException("MODEL_NOT_FOUND:"+modelId);
        }
        if(!modelPackage.get("live").getAsBoolean())
        {
            throw new ModelIsNotLive("MODEL_IS_NOT_LIVE_YET:"+modelId);
        }

        JsonObject rollback = this.mongoDBJsonStore.rollOverToTraningDataSets(this.securityTokenContainer.getTenant(), modelId);

        JsonArray rollbackDataSetIds = rollback.getAsJsonArray("rolledOverDataSetIds");
        if(rollbackDataSetIds == null)
        {
            return new JsonArray();
        }

        //Also undeploy the model
        this.mongoDBJsonStore.undeployModel(this.securityTokenContainer.getTenant(), modelId);
        this.activeModels.remove(modelId);

        return rollbackDataSetIds;
    }
    //PRODUCTION_MODEL_RELATED**************************************************************************************************************************************************************************************************************************************************************************************************
    public JsonObject trainModelFromDataLake(Artifact artifact,int nEpochs) throws
            ModelNotFoundException, ModelIsLive {
        String modelId = artifact.getAiModel().getModelId();
        JsonObject modelPackage = this.mongoDBJsonStore.getModelPackage(this.securityTokenContainer.getTenant(), modelId);
        String[] dataLakeIds = artifact.getDataSet().getDataLakeIds();

        if(modelPackage == null)
        {
            throw new ModelNotFoundException("MODEL_NOT_FOUND:"+modelId);
        }
        boolean isLive = modelPackage.get("live").getAsBoolean();
        if(isLive){
            throw new ModelIsLive("MODEL_IS_LIVE");
        }

        String modelString = modelPackage.get("model").getAsString();
        try
        {
            MultiLayerNetwork network = this.activeModels.get(modelId);

            if(network == null)
            {
                //logger.info("******************************************");
                //logger.info("DESERIALZING_THE_MODEL: "+modelId);
                //logger.info("******************************************");
                ByteArrayInputStream restoreStream = new ByteArrayInputStream(Base64.getDecoder().decode(modelString));
                network = ModelSerializer.restoreMultiLayerNetwork(restoreStream, true);
                this.activeModels.put(modelId, network);
            }

            String csvData;
            StringBuilder csvBuilder = new StringBuilder();
            for(String dataLakeId:dataLakeIds) {
                JsonArray dataLakeArray = mongoDBJsonStore.getIngestion(this.securityTokenContainer.getTenant(), dataLakeId);
                //Get the Data
                boolean isJson = false;
                boolean isXml = false;
                JsonArray dataArray = new JsonArray();
                int size = dataLakeArray.size();
                for (int i = 0; i < size; i++) {
                    JsonObject cour = dataLakeArray.get(i).getAsJsonObject();
                    String data = cour.get("data").getAsString();
                    if (data.startsWith("{") || data.startsWith("[")) {
                        //Its json
                        dataArray.add(JsonParser.parseString(data).getAsJsonObject());
                        isJson = true;
                    } else if (data.contains("<") && data.contains(">")) {
                        //Its xml
                        JSONObject sourceJson = XML.toJSONObject(data);
                        String json = sourceJson.toString(4);
                        JsonObject sourceJsonObject = JsonParser.parseString(json).getAsJsonObject();
                        dataArray.add(sourceJsonObject);
                        isXml = true;
                    } else {
                        //Its CSV
                        dataArray.add(data);
                    }
                }

                //Convert To Csv
                if (isJson) {
                    csvBuilder.append(artifact.convertJsonToCsv(dataArray)+"\n");
                } else if (isXml) {
                    csvBuilder.append(artifact.convertXmlToCsv(dataArray)+"\n");
                } else {
                    csvBuilder.append(dataArray.get(0).getAsString()+"\n");
                }
            }

            csvData = csvBuilder.toString().trim();
            int batchSize = csvData.length();
            ResettableStreamSplit inputStreamSplit = new ResettableStreamSplit(
                    csvData);

            //This should be a parameter
            int labelIndex = artifact.getLabelIndex();
            int possibleLabels = artifact.getNumberOfLabels();
            RecordReader rrTrain = new CSVRecordReader();
            rrTrain.initialize(inputStreamSplit);
            DataSetIterator trainIter = new RecordReaderDataSetIterator(rrTrain,
                    batchSize, labelIndex, possibleLabels);

            network.fit(trainIter,nEpochs);

            Evaluation evaluation = network.evaluate(trainIter);

            return JsonParser.parseString(evaluation.toJson()).getAsJsonObject();
        }
        catch(Exception e)
        {
            logger.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public JsonObject trainModelFromDataSet(Artifact artifact, int nEpochs) throws
            ModelNotFoundException, ModelIsLive {
        String modelId = artifact.getAiModel().getModelId();
        JsonObject modelPackage = this.mongoDBJsonStore.getModelPackage(this.securityTokenContainer.getTenant(), modelId);
        String[] dataSetIds = artifact.getDataSet().getDataSetIds();

        if(modelPackage == null)
        {
            throw new ModelNotFoundException("MODEL_NOT_FOUND:"+modelId);
        }
        boolean isLive = modelPackage.get("live").getAsBoolean();
        if(isLive){
            throw new ModelIsLive("MODEL_IS_LIVE");
        }

        String modelString = modelPackage.get("model").getAsString();
        try
        {
            MultiLayerNetwork network = this.activeModels.get(modelId);

            if(network == null)
            {
                //logger.info("******************************************");
                //logger.info("DESERIALZING_THE_MODEL: "+modelId);
                //logger.info("******************************************");
                ByteArrayInputStream restoreStream = new ByteArrayInputStream(Base64.getDecoder().decode(modelString));
                network = ModelSerializer.restoreMultiLayerNetwork(restoreStream, true);
                this.activeModels.put(modelId, network);
            }

            String storedData;
            StringBuilder dataSetBuilder = new StringBuilder();
            for(String dataSetId:dataSetIds) {
                JsonObject dataSetJson = mongoDBJsonStore.readDataSet(this.securityTokenContainer.getTenant(),
                        dataSetId);
                dataSetBuilder.append(dataSetJson.get("data").getAsString()+"\n");
            }
            storedData = dataSetBuilder.toString().trim();

            //This should be a parameter
            int batchSize = storedData.length();
            ResettableStreamSplit inputStreamSplit = new ResettableStreamSplit(
                    storedData);

            //This should be a parameter
            int labelIndex = artifact.getLabelIndex();
            int possibleLabels = artifact.getNumberOfLabels();
            RecordReader rrTrain = new CSVRecordReader();
            rrTrain.initialize(inputStreamSplit);
            DataSetIterator trainIter = new RecordReaderDataSetIterator(rrTrain,
                    batchSize, labelIndex, possibleLabels);

            network.fit(trainIter,nEpochs);

            Evaluation evaluation = network.evaluate(trainIter);

            return JsonParser.parseString(evaluation.toJson()).getAsJsonObject();
        }
        catch(Exception e)
        {
            logger.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public String evalJava(String modelId, String[] dataSetIds) throws ModelNotFoundException, ModelIsNotLive
    {
        JsonObject modelPackage = this.mongoDBJsonStore.getModelPackage(this.securityTokenContainer.getTenant(), modelId);

        if(modelPackage == null)
        {
            throw new ModelNotFoundException("MODEL_NOT_FOUND:"+modelId);
        }
        if(!modelPackage.get("live").getAsBoolean())
        {
            throw new ModelIsNotLive("MODEL_IS_NOT_LIVE_YET:"+modelId);
        }


        String modelString = modelPackage.get("model").getAsString();
        try
        {
            MultiLayerNetwork network = this.activeModels.get(modelId);

            if(network == null)
            {
                //logger.info("******************************************");
                //logger.info("DESERIALZING_THE_MODEL: "+modelId);
                //logger.info("******************************************");
                ByteArrayInputStream restoreStream = new ByteArrayInputStream(Base64.getDecoder().decode(modelString));
                network = ModelSerializer.restoreMultiLayerNetwork(restoreStream, true);
                this.activeModels.put(modelId, network);
            }

            JsonObject dataSetJson = mongoDBJsonStore.readDataSet(this.securityTokenContainer.getTenant(),dataSetIds[0]);
            String storedData = dataSetJson.get("data").getAsString();
            int batchSize = storedData.length();
            ResettableStreamSplit inputStreamSplit = new ResettableStreamSplit(
                    storedData);

            //This should be a parameter
            int nEpochs = 30;
            int labelIndex = 0;
            int possibleLabels = 2;
            RecordReader rrTest = new CSVRecordReader();
            rrTest.initialize(inputStreamSplit);
            DataSetIterator testIter = new RecordReaderDataSetIterator(rrTest,
                    batchSize, labelIndex, possibleLabels);

            network.fit(testIter,nEpochs);

            Evaluation evaluation = network.evaluate(testIter);
            //logger.info("*********CLOUD_EVAL**************");
            //logger.info(evaluation.stats());

            return evaluation.toJson();
        }
        catch(Exception e)
        {
            logger.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public String evalJavaFromDataLake(String modelId, String[] dataLakeIds) throws ModelNotFoundException, ModelIsNotLive
    {
        JsonObject modelPackage = this.mongoDBJsonStore.getModelPackage(this.securityTokenContainer.getTenant(), modelId);

        if(modelPackage == null)
        {
            throw new ModelNotFoundException("MODEL_NOT_FOUND:"+modelId);
        }
        if(!modelPackage.get("live").getAsBoolean())
        {
            throw new ModelIsNotLive("MODEL_IS_NOT_LIVE_YET:"+modelId);
        }
        String modelString = modelPackage.get("model").getAsString();
        try
        {
            MultiLayerNetwork network = this.activeModels.get(modelId);

            if(network == null)
            {
                //logger.info("******************************************");
                //logger.info("DESERIALZING_THE_MODEL: "+modelId);
                //logger.info("******************************************");
                ByteArrayInputStream restoreStream = new ByteArrayInputStream(Base64.getDecoder().decode(modelString));
                network = ModelSerializer.restoreMultiLayerNetwork(restoreStream, true);
                this.activeModels.put(modelId, network);
            }

            String[] dataSetIds = new String[dataLakeIds.length];
            for(int i=0; i<dataLakeIds.length;i++)
            {
                String dataLakeId = dataLakeIds[i];
                JsonArray ingestedData = this.mongoDBJsonStore.getIngestion(this.securityTokenContainer.getTenant(), dataLakeId);
                //logger.info("***************************************************");
                //logger.info("DataLakeId: "+dataLakeId+":"+ingestedData.toString());
                //logger.info("***************************************************");

                JsonObject input = new JsonObject();
                input.addProperty("format", "csv");
                input.addProperty("data", ingestedData.toString());
                String dataSetId = this.modelDataSetService.storeTrainingDataSet(input);
                dataSetIds[i] = dataSetId;
            }

            DataSetIterator dataSetIterator = this.aiPlatformDataSetIteratorFactory.
                    getInstance(dataSetIds);
            Evaluation evaluation = network.evaluate(dataSetIterator);

            return evaluation.toJson();
        }
        catch(Exception e)
        {
            logger.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }
}
