package io.bugsbunny.infrastructure;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import com.mongodb.client.*;

import io.bugsbunny.configuration.AIPlatformConfig;
import io.bugsbunny.preprocess.SecurityTokenContainer;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.*;

//TODO: DISTINGUISH_BETWEEN_DEV_AND_LIVE_MODELS

@ApplicationScoped
public class MongoDBJsonStore
{
    private static Logger logger = LoggerFactory.getLogger(MongoDBJsonStore.class);

    @Inject
    private SecurityTokenContainer securityTokenContainer;

    @Inject
    private AIPlatformConfig aiPlatformConfig;

    private MongoClient mongoClient;

    @PostConstruct
    public void start()
    {
        JsonObject config = this.aiPlatformConfig.getConfiguration();

        //mongodb://[username:password@]host1[:port1][,host2[:port2],...[,hostN[:portN]]][/[database][?options]]
        StringBuilder connectStringBuilder = new StringBuilder();
        connectStringBuilder.append("mongodb://");

        String mongodbHost = config.get("mongodbHost").getAsString();
        long mongodbPort = config.get("mongodbPort").getAsLong();
        if(config.has("mongodbUser") && config.has("mongodbPassword"))
        {
            connectStringBuilder.append(config.get("mongodbUser").getAsString()
            +":"+config.get("mongodbPassword").getAsString()+"@");
        }
        connectStringBuilder.append(mongodbHost+":"+mongodbPort);

        String connectionString = connectStringBuilder.toString();
        this.mongoClient = MongoClients.create(connectionString);
    }

    @PreDestroy
    public void stop()
    {
        this.mongoClient.close();
    }

    public SecurityTokenContainer getSecurityTokenContainer() {
        return securityTokenContainer;
    }

    public void setSecurityTokenContainer(SecurityTokenContainer securityTokenContainer) {
        this.securityTokenContainer = securityTokenContainer;
    }

    //Data Ingestion related operations-----------------------------------------------------
    public long storeIngestion(JsonObject jsonObject)
    {
        String principal = this.securityTokenContainer.getSecurityToken().getPrincipal();
        String databaseName = principal + "_" + "aiplatform";
        MongoDatabase database = mongoClient.getDatabase(databaseName);

        MongoCollection<Document> collection = database.getCollection("datalake");

        long oid = new Random().nextLong();
        jsonObject.addProperty("dataLakeId", oid);
        collection.insertOne(Document.parse(jsonObject.toString()));

        return oid;
    }

    public JsonObject getIngestion(long dataLakeId)
    {
        JsonObject ingestion = new JsonObject();

        String principal = this.securityTokenContainer.getSecurityToken().getPrincipal();
        String databaseName = principal + "_" + "aiplatform";
        MongoDatabase database = mongoClient.getDatabase(databaseName);

        MongoCollection<Document> collection = database.getCollection("datalake");

        String queryJson = "{\"dataLakeId\":"+dataLakeId+"}";
        Bson bson = Document.parse(queryJson);
        FindIterable<Document> iterable = collection.find(bson);
        MongoCursor<Document> cursor = iterable.cursor();
        while(cursor.hasNext())
        {
            Document document = cursor.next();
            String documentJson = document.toJson();
            ingestion = JsonParser.parseString(documentJson).getAsJsonObject();
            return ingestion;
        }
        return ingestion;
    }

    public JsonArray getIngestedDataSet()
    {
        JsonArray ingestedDataSet = new JsonArray();

        String principal = this.securityTokenContainer.getSecurityToken().getPrincipal();
        String databaseName = principal + "_" + "aiplatform";
        MongoDatabase database = mongoClient.getDatabase(databaseName);

        MongoCollection<Document> collection = database.getCollection("datalake");

        FindIterable<Document> iterable = collection.find();
        MongoCursor<Document> cursor = iterable.cursor();
        while(cursor.hasNext())
        {
            Document document = cursor.next();
            String documentJson = document.toJson();
            JsonObject ingestion = JsonParser.parseString(documentJson).getAsJsonObject();
            JsonObject actual = new JsonObject();
            actual.addProperty("ingestionId", ingestion.get("ingestionId").getAsString());
            actual.addProperty("data", ingestion.get("data").getAsString());
            ingestedDataSet.add(actual);
        }
        return ingestedDataSet;
    }
    //Data History related operations-----------------------------------------------------
    public String startDiffChain(JsonObject payload)
    {
        String principal = this.securityTokenContainer.getSecurityToken().getPrincipal();
        String databaseName = principal + "_" + "aiplatform";
        MongoDatabase database = mongoClient.getDatabase(databaseName);

        MongoCollection<Document> collection = database.getCollection("diffChain");

        String chainId = UUID.randomUUID().toString();
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("chainId", chainId);
        jsonObject.add("payload", payload);

        Document doc = Document.parse(jsonObject.toString());
        collection.insertOne(doc);

        return chainId;
    }

    public void addToDiffChain(String chainId, JsonObject payload)
    {
        String principal = this.securityTokenContainer.getSecurityToken().getPrincipal();
        String databaseName = principal + "_" + "aiplatform";
        MongoDatabase database = mongoClient.getDatabase(databaseName);

        MongoCollection<Document> collection = database.getCollection("diffChain");

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("chainId", chainId);
        jsonObject.add("payload", payload);

        Document doc = Document.parse(jsonObject.toString());
        collection.insertOne(doc);
    }

    public void addToDiffChain(String requestChainId, String chainId, JsonObject payload)
    {
        String principal = this.securityTokenContainer.getSecurityToken().getPrincipal();
        String databaseName = principal + "_" + "aiplatform";
        MongoDatabase database = mongoClient.getDatabase(databaseName);

        MongoCollection<Document> collection = database.getCollection("diffChain");

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("chainId", chainId);
        jsonObject.addProperty("requestChainId", requestChainId);
        jsonObject.add("payload", payload);

        Document doc = Document.parse(jsonObject.toString());
        collection.insertOne(doc);
    }

    public JsonObject getLastPayload(String chainId)
    {
        JsonObject lastPayload = new JsonObject();

        String principal = this.securityTokenContainer.getSecurityToken().getPrincipal();
        String databaseName = principal + "_" + "aiplatform";
        MongoDatabase database = mongoClient.getDatabase(databaseName);
        MongoCollection<Document> collection = database.getCollection("diffChain");

        String queryJson = "{\"chainId\":\""+chainId+"\"}";
        Bson bson = Document.parse(queryJson);
        FindIterable<Document> iterable = collection.find(bson);
        MongoCursor<Document> cursor = iterable.cursor();
        while(cursor.hasNext())
        {
            Document document = cursor.next();
            if(!cursor.hasNext())
            {
                String documentJson = document.toJson();
                lastPayload = JsonParser.parseString(documentJson).getAsJsonObject();
            }
        }

        lastPayload = lastPayload.getAsJsonObject("payload");
        return lastPayload;
    }

    public void addToDiff(String chainId, JsonObject objectDiff)
    {
        String principal = this.securityTokenContainer.getSecurityToken().getPrincipal();
        String databaseName = principal + "_" + "aiplatform";
        MongoDatabase database = mongoClient.getDatabase(databaseName);
        MongoCollection<Document> collection = database.getCollection("objectDiff");

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("chainId", chainId);
        jsonObject.add("objectDiff", objectDiff);

        Document doc = Document.parse(jsonObject.toString());
        collection.insertOne(doc);
    }

    public void addToDiff(String requestChainId, String chainId, JsonObject objectDiff)
    {
        String principal = this.securityTokenContainer.getSecurityToken().getPrincipal();
        String databaseName = principal + "_" + "aiplatform";
        MongoDatabase database = mongoClient.getDatabase(databaseName);
        MongoCollection<Document> collection = database.getCollection("objectDiff");

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("chainId", chainId);
        jsonObject.addProperty("requestChainId", requestChainId);
        jsonObject.add("objectDiff", objectDiff);

        Document doc = Document.parse(jsonObject.toString());
        collection.insertOne(doc);
    }

    public List<JsonObject> readDiffChain(String chainId)
    {
        List<JsonObject> chain = new LinkedList<>();

        String principal = this.securityTokenContainer.getSecurityToken().getPrincipal();
        String databaseName = principal + "_" + "aiplatform";
        MongoDatabase database = mongoClient.getDatabase(databaseName);

        MongoCollection<Document> collection = database.getCollection("diffChain");

        String queryJson = "{\"chainId\":\""+chainId+"\"}";
        Bson bson = Document.parse(queryJson);
        FindIterable<Document> iterable = collection.find(bson);
        MongoCursor<Document> cursor = iterable.cursor();
        while(cursor.hasNext()) {
            Document document = cursor.next();
            String documentJson = document.toJson();
            JsonObject objectDiff = JsonParser.parseString(documentJson).getAsJsonObject();
            chain.add(objectDiff);
        }

        return chain;
    }

    public List<JsonObject> readDiffs(String chainId)
    {
        List<JsonObject> diffs = new LinkedList<>();

        String principal = this.securityTokenContainer.getSecurityToken().getPrincipal();
        String databaseName = principal + "_" + "aiplatform";
        MongoDatabase database = mongoClient.getDatabase(databaseName);

        MongoCollection<Document> collection = database.getCollection("objectDiff");

        String queryJson = "{\"chainId\":\""+chainId+"\"}";
        Bson bson = Document.parse(queryJson);
        FindIterable<Document> iterable = collection.find(bson);
        MongoCursor<Document> cursor = iterable.cursor();
        while(cursor.hasNext()) {
            Document document = cursor.next();
            String documentJson = document.toJson();
            JsonObject objectDiff = JsonParser.parseString(documentJson).getAsJsonObject();
            diffs.add(objectDiff);
        }

        return diffs;
    }

    public List<JsonObject> readDiffChain(String region, String principal)
    {
        List<JsonObject> chain = new LinkedList<>();

        String databaseName = principal + "_" + "aiplatform";
        MongoDatabase database = mongoClient.getDatabase(databaseName);

        MongoCollection<Document> collection = database.getCollection("diffChain");

        FindIterable<Document> iterable = collection.find();
        MongoCursor<Document> cursor = iterable.cursor();
        while(cursor.hasNext()) {
            Document document = cursor.next();
            String documentJson = document.toJson();
            JsonObject objectDiff = JsonParser.parseString(documentJson).getAsJsonObject();
            chain.add(objectDiff);
        }

        return chain;
    }

    public List<JsonObject> readDiffs(String region, String principal)
    {
        List<JsonObject> diffs = new LinkedList<>();

        String databaseName = principal + "_" + "aiplatform";
        MongoDatabase database = mongoClient.getDatabase(databaseName);

        MongoCollection<Document> collection = database.getCollection("objectDiff");

        FindIterable<Document> iterable = collection.find();
        MongoCursor<Document> cursor = iterable.cursor();
        while(cursor.hasNext()) {
            Document document = cursor.next();
            String documentJson = document.toJson();
            JsonObject objectDiff = JsonParser.parseString(documentJson).getAsJsonObject();
            diffs.add(objectDiff);
        }

        return diffs;
    }
    //-----------------------------------------------------------------------------
    public long storeModel(JsonObject modelPackage)
    {
        String principal = this.securityTokenContainer.getSecurityToken().getPrincipal();
        String databaseName = principal + "_" + "aiplatform";
        MongoDatabase database = mongoClient.getDatabase(databaseName);

        MongoCollection<Document> collection = database.getCollection("aimodels");
        long modelId = new Random().nextLong();
        modelPackage.addProperty("modelId", modelId);
        Document doc = Document.parse(modelPackage.toString());
        collection.insertOne(doc);

        return modelId;
    }

    public String getModel(long modelId)
    {
        String principal = this.securityTokenContainer.getSecurityToken().getPrincipal();
        String databaseName = principal + "_" + "aiplatform";
        MongoDatabase database = mongoClient.getDatabase(databaseName);

        MongoCollection<Document> collection = database.getCollection("aimodels");

        String queryJson = "{\"modelId\":"+modelId+"}";
        Bson bson = Document.parse(queryJson);
        FindIterable<Document> iterable = collection.find(bson);
        MongoCursor<Document> cursor = iterable.cursor();
        if(cursor.hasNext())
        {
            Document document = cursor.next();
            String documentJson = document.toJson();
            return JsonParser.parseString(documentJson).getAsJsonObject().get("model").getAsString();
        }

        logger.info("***************************");
        logger.info("Database: "+databaseName);
        logger.info("MODEL_NOT_FOUND: "+modelId);
        logger.info("***************************");
        return null;
    }

    public JsonObject getModelPackage(long modelId)
    {
        String principal = this.securityTokenContainer.getSecurityToken().getPrincipal();
        String databaseName = principal + "_" + "aiplatform";
        MongoDatabase database = mongoClient.getDatabase(databaseName);

        MongoCollection<Document> collection = database.getCollection("aimodels");

        String queryJson = "{\"modelId\":"+modelId+"}";
        Bson bson = Document.parse(queryJson);
        FindIterable<Document> iterable = collection.find(bson);
        MongoCursor<Document> cursor = iterable.cursor();
        if(cursor.hasNext())
        {
            Document document = cursor.next();
            String documentJson = document.toJson();
            return JsonParser.parseString(documentJson).getAsJsonObject();
        }
        return null;
    }

    public void deployModel(long modelId)
    {
        String principal = this.securityTokenContainer.getSecurityToken().getPrincipal();
        String databaseName = principal + "_" + "aiplatform";
        MongoDatabase database = mongoClient.getDatabase(databaseName);
        MongoCollection<Document> collection = database.getCollection("aimodels");

        JsonObject currentModel = this.getModelPackage(modelId);
        Bson bson = Document.parse(currentModel.toString());
        collection.deleteOne(bson);

        currentModel.remove("_id");
        currentModel.addProperty("live", true);
        this.storeLiveModel(currentModel);
    }

    public void undeployModel(long modelId)
    {
        String principal = this.securityTokenContainer.getSecurityToken().getPrincipal();
        String databaseName = principal + "_" + "aiplatform";
        MongoDatabase database = mongoClient.getDatabase(databaseName);
        MongoCollection<Document> collection = database.getCollection("aimodels");

        JsonObject currentModel = this.getModelPackage(modelId);
        Bson bson = Document.parse(currentModel.toString());
        collection.deleteOne(bson);

        currentModel.remove("_id");
        currentModel.addProperty("live", false);
        this.storeLiveModel(currentModel);
    }

    private void storeLiveModel(JsonObject modelPackage)
    {
        String principal = this.securityTokenContainer.getSecurityToken().getPrincipal();
        String databaseName = principal + "_" + "aiplatform";
        MongoDatabase database = mongoClient.getDatabase(databaseName);

        MongoCollection<Document> collection = database.getCollection("aimodels");
        Document doc = Document.parse(modelPackage.toString());
        collection.insertOne(doc);
    }
    //DataLake related operations----------------------------------------------------------------
    public long storeTrainingDataSet(JsonObject dataSetJson)
    {
        String principal = this.securityTokenContainer.getSecurityToken().getPrincipal();
        String databaseName = principal + "_" + "aiplatform";
        MongoDatabase database = mongoClient.getDatabase(databaseName);

        MongoCollection<Document> collection = database.getCollection("dataset");

        long oid = new Random().nextLong();
        dataSetJson.addProperty("dataSetId", oid);
        dataSetJson.addProperty("dataSetType", "training");
        Document doc = Document.parse(dataSetJson.toString());
        collection.insertOne(doc);

        return oid;
    }

    public long storeEvalDataSet(JsonObject dataSetJson)
    {
        String principal = this.securityTokenContainer.getSecurityToken().getPrincipal();
        String databaseName = principal + "_" + "aiplatform";
        MongoDatabase database = mongoClient.getDatabase(databaseName);

        MongoCollection<Document> collection = database.getCollection("dataset");

        long oid = new Random().nextLong();
        dataSetJson.addProperty("dataSetId", oid);
        dataSetJson.addProperty("dataSetType", "evaluation");
        Document doc = Document.parse(dataSetJson.toString());
        collection.insertOne(doc);

        return oid;
    }

    public JsonObject readDataSet(long dataSetId)
    {
        String principal = this.securityTokenContainer.getSecurityToken().getPrincipal();
        String databaseName = principal + "_" + "aiplatform";
        MongoDatabase database = mongoClient.getDatabase(databaseName);

        MongoCollection<Document> collection = database.getCollection("dataset");

        String queryJson = "{\"dataSetId\":"+dataSetId+"}";
        Bson bson = Document.parse(queryJson);
        FindIterable<Document> iterable = collection.find(bson);
        MongoCursor<Document> cursor = iterable.cursor();
        if(cursor.hasNext())
        {
            Document document = cursor.next();
            String documentJson = document.toJson();

            JsonObject cour = JsonParser.parseString(documentJson).getAsJsonObject();
            return cour;
        }

        return null;
    }

    public JsonObject rollOverToTraningDataSets(long modelId)
    {
        JsonObject rolledOverDataSetIds = new JsonObject();

        JsonArray dataSetIds = new JsonArray();
        String dataSettype = "training";
        String principal = this.securityTokenContainer.getSecurityToken().getPrincipal();
        String databaseName = principal + "_" + "aiplatform";
        MongoDatabase database = mongoClient.getDatabase(databaseName);

        MongoCollection<Document> collection = database.getCollection("dataset");

        String queryJson = "{\"modelId\":"+modelId+"}";
        Bson bson = Document.parse(queryJson);
        FindIterable<Document> iterable = collection.find(bson);
        MongoCursor<Document> cursor = iterable.cursor();
        while(cursor.hasNext())
        {
            Document document = cursor.next();
            String documentJson = document.toJson();
            JsonObject dataSetJson = JsonParser.parseString(documentJson).getAsJsonObject();
            dataSetJson.remove("_id");
            dataSetJson.addProperty("dataSetType", "training");
            collection.insertOne(Document.parse(dataSetJson.toString()));
            dataSetIds.add(dataSetJson.get("dataSetId").getAsLong());
        }

        rolledOverDataSetIds.add("rolledOverDataSetIds", dataSetIds);
        return rolledOverDataSetIds;
    }
}
