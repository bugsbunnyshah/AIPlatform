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
    private PackagingService packagingService;

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
        String output;
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
        return output;
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

            DataSetIterator dataSetIterator = this.aiPlatformDataSetIteratorFactory.
                    getInstance(dataSetIds);

            JsonObject dataSetJson = mongoDBJsonStore.readDataSet(this.securityTokenContainer.getTenant(),dataSetIds[0]);
            String storedData = dataSetJson.get("data").getAsString();
            int batchSize = storedData.length();
            ResettableStreamSplit inputStreamSplit = new ResettableStreamSplit(
                    storedData);

            int nEpochs = 30;
            RecordReader rrTest = new CSVRecordReader();
            rrTest.initialize(inputStreamSplit);
            DataSetIterator testIter = new RecordReaderDataSetIterator(rrTest, batchSize, 0, 2);

            network.fit(testIter,nEpochs);

            Evaluation evaluation = network.evaluate(testIter);
            logger.info("*********CLOUD_EVAL**************");
            logger.info(evaluation.stats());

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

    public String trainJava(Artifact artifact, String[] dataSetIds) throws ModelNotFoundException, ModelIsLive{
        JsonObject json = this.trainJavaFromDataSetInProject(artifact, dataSetIds);
        return json.toString();
    }

    public JsonObject trainJavaFromDataSetInProject(Artifact artifact, String[] dataSetIds) throws
            ModelNotFoundException, ModelIsLive {
        String modelId = artifact.getAiModel().getModelId();
        JsonObject modelPackage = this.mongoDBJsonStore.getModelPackage(this.securityTokenContainer.getTenant(), modelId);

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

            DataSetIterator dataSetIterator = this.aiPlatformDataSetIteratorFactory.
                    getInstance(dataSetIds);
            Evaluation evaluation = network.evaluate(dataSetIterator);

            return JsonParser.parseString(evaluation.toJson()).getAsJsonObject();
        }
        catch(Exception e)
        {
            logger.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public String trainJavaFromDataLake(Artifact artifact, String[] dataLakeIds) throws ModelNotFoundException, ModelIsLive
    {
        JsonObject json = this.trainJavaFromDataLakeInProject(artifact,dataLakeIds);
        return json.toString();
    }

    public JsonObject trainJavaFromDataLakeInProject(Artifact artifact, String[] dataLakeIds) throws
            ModelNotFoundException,ModelIsLive
    {
        String modelId = artifact.getAiModel().getModelId();
        JsonObject modelPackage = this.mongoDBJsonStore.getModelPackage(this.securityTokenContainer.getTenant(), modelId);

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

            DataSetIterator dataSetIterator = this.aiPlatformDataLakeIteratorFactory.
                    getInstance(artifact,dataLakeIds);
            Evaluation evaluation = network.evaluate(dataSetIterator);

            return JsonParser.parseString(evaluation.toJson()).getAsJsonObject();
        }
        catch(Exception e)
        {
            logger.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }
}
