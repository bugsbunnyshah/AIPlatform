package io.bugsbunny.persistence;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import com.mongodb.client.*;

import io.bugsbunny.endpoint.SecurityTokenContainer;

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

    private MongoClient mongoClient = MongoClients.create();

    @Inject
    private SecurityTokenContainer securityTokenContainer;

    @PostConstruct
    public void start()
    {
        this.mongoClient = MongoClients.create();
    }

    @PreDestroy
    public void stop()
    {
        this.mongoClient.close();
    }

    //Data Ingestion related operations-----------------------------------------------------
    public void storeIngestion(List<JsonObject> jsonObjects)
    {
        String principal = securityTokenContainer.getTokenContainer().get().getPrincipal();
        String region = securityTokenContainer.getTokenContainer().get().getRegion();
        String databaseName = region + "_" + principal + "_" + "aiplatform";
        MongoDatabase database = mongoClient.getDatabase(databaseName);

        MongoCollection<Document> collection = database.getCollection("ingestion");

        for(JsonObject jsonObject:jsonObjects) {
            Document doc = Document.parse(jsonObject.toString());
            collection.insertOne(doc);
        }
    }

    public JsonObject getIngestion(String ingestionId)
    {
        JsonObject ingestion = new JsonObject();

        String principal = securityTokenContainer.getTokenContainer().get().getPrincipal();
        String region = securityTokenContainer.getTokenContainer().get().getRegion();
        String databaseName = region + "_" + principal + "_" + "aiplatform";
        MongoDatabase database = mongoClient.getDatabase(databaseName);

        MongoCollection<Document> collection = database.getCollection("ingestion");

        String queryJson = "{\"ingestionId\":\""+ingestionId+"\"}";
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

        String principal = securityTokenContainer.getTokenContainer().get().getPrincipal();
        String region = securityTokenContainer.getTokenContainer().get().getRegion();
        String databaseName = region + "_" + principal + "_" + "aiplatform";
        MongoDatabase database = mongoClient.getDatabase(databaseName);

        MongoCollection<Document> collection = database.getCollection("ingestion");

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
        String principal = securityTokenContainer.getTokenContainer().get().getPrincipal();
        String region = securityTokenContainer.getTokenContainer().get().getRegion();
        String databaseName = region + "_" + principal + "_" + "aiplatform";
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
        String principal = securityTokenContainer.getTokenContainer().get().getPrincipal();
        String region = securityTokenContainer.getTokenContainer().get().getRegion();
        String databaseName = region + "_" + principal + "_" + "aiplatform";
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
        String principal = securityTokenContainer.getTokenContainer().get().getPrincipal();
        String region = securityTokenContainer.getTokenContainer().get().getRegion();
        String databaseName = region + "_" + principal + "_" + "aiplatform";
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

        String principal = securityTokenContainer.getTokenContainer().get().getPrincipal();
        String region = securityTokenContainer.getTokenContainer().get().getRegion();
        String databaseName = region + "_" + principal + "_" + "aiplatform";
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
        String principal = securityTokenContainer.getTokenContainer().get().getPrincipal();
        String region = securityTokenContainer.getTokenContainer().get().getRegion();
        String databaseName = region + "_" + principal + "_" + "aiplatform";
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
        String principal = securityTokenContainer.getTokenContainer().get().getPrincipal();
        String region = securityTokenContainer.getTokenContainer().get().getRegion();
        String databaseName = region + "_" + principal + "_" + "aiplatform";
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

        String principal = securityTokenContainer.getTokenContainer().get().getPrincipal();
        String region = securityTokenContainer.getTokenContainer().get().getRegion();
        String databaseName = region + "_" + principal + "_" + "aiplatform";
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

        String principal = securityTokenContainer.getTokenContainer().get().getPrincipal();
        String region = securityTokenContainer.getTokenContainer().get().getRegion();
        String databaseName = region + "_" + principal + "_" + "aiplatform";
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

        String databaseName = region + "_" + principal + "_" + "aiplatform";
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

        String databaseName = region + "_" + principal + "_" + "aiplatform";
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
        String principal = securityTokenContainer.getTokenContainer().get().getPrincipal();
        String region = securityTokenContainer.getTokenContainer().get().getRegion();
        String databaseName = region + "_" + principal + "_" + "aiplatform";
        MongoDatabase database = mongoClient.getDatabase(databaseName);

        MongoCollection<Document> collection = database.getCollection("liveModels");
        long modelId = new Random().nextLong();
        modelPackage.addProperty("modelId", modelId);
        Document doc = Document.parse(modelPackage.toString());
        collection.insertOne(doc);

        return modelId;
    }

    public String getModel(long modelId)
    {
        String principal = securityTokenContainer.getTokenContainer().get().getPrincipal();
        String region = securityTokenContainer.getTokenContainer().get().getRegion();
        String databaseName = region + "_" + principal + "_" + "aiplatform";
        MongoDatabase database = mongoClient.getDatabase(databaseName);

        MongoCollection<Document> collection = database.getCollection("liveModels");

        String queryJson = "{\"modelId\":"+modelId+"}";
        Bson bson = Document.parse(queryJson);
        FindIterable<Document> iterable = collection.find(bson);
        MongoCursor<Document> cursor = iterable.cursor();
        Document document = cursor.next();
        String documentJson = document.toJson();
        String model = JsonParser.parseString(documentJson).getAsJsonObject().get("model").getAsString();
        return model;
    }
    //DataLake related operations----------------------------------------------------------------
    public long storeDataSet(String dataFormat, String dataSetType, String data)
    {
        String principal = securityTokenContainer.getTokenContainer().get().getPrincipal();
        String region = securityTokenContainer.getTokenContainer().get().getRegion();
        String databaseName = region + "_" + principal + "_" + "aiplatform";
        MongoDatabase database = mongoClient.getDatabase(databaseName);

        MongoCollection<Document> collection = database.getCollection("dataset");

        long oid = new Random().nextLong();
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("dataSetId", oid);
        jsonObject.addProperty("format",dataFormat);
        jsonObject.addProperty("dataSetType", dataSetType);
        jsonObject.addProperty("data", data);
        Document doc = Document.parse(jsonObject.toString());
        collection.insertOne(doc);

        return oid;
    }

    public long storeDataSet(long modelId, String dataFormat, String dataSetType, String data)
    {
        String principal = securityTokenContainer.getTokenContainer().get().getPrincipal();
        String region = securityTokenContainer.getTokenContainer().get().getRegion();
        String databaseName = region + "_" + principal + "_" + "aiplatform";
        MongoDatabase database = mongoClient.getDatabase(databaseName);

        MongoCollection<Document> collection = database.getCollection("dataset");

        long oid = new Random().nextLong();
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("modelId", modelId);
        jsonObject.addProperty("dataSetId", oid);
        jsonObject.addProperty("format",dataFormat);
        jsonObject.addProperty("dataSetType", dataSetType);
        jsonObject.addProperty("data", data);
        Document doc = Document.parse(jsonObject.toString());
        collection.insertOne(doc);

        return oid;
    }

    public JsonObject readDataSet(long dataSetId)
    {
        String principal = securityTokenContainer.getTokenContainer().get().getPrincipal();
        String region = securityTokenContainer.getTokenContainer().get().getRegion();
        String databaseName = region + "_" + principal + "_" + "aiplatform";
        MongoDatabase database = mongoClient.getDatabase(databaseName);

        MongoCollection<Document> collection = database.getCollection("dataset");

        String queryJson = "{\"dataSetId\":"+dataSetId+"}";
        Bson bson = Document.parse(queryJson);
        FindIterable<Document> iterable = collection.find(bson);
        MongoCursor<Document> cursor = iterable.cursor();
        Document document = cursor.next();
        String documentJson = document.toJson();

        return JsonParser.parseString(documentJson).getAsJsonObject();
    }

    public JsonObject rollOverToTraningDataSets(long modelId)
    {
        JsonObject rolledOverDataSetIds = new JsonObject();

        JsonArray dataSetIds = new JsonArray();
        String dataSettype = "training";
        String principal = securityTokenContainer.getTokenContainer().get().getPrincipal();
        String region = securityTokenContainer.getTokenContainer().get().getRegion();
        String databaseName = region + "_" + principal + "_" + "aiplatform";
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
