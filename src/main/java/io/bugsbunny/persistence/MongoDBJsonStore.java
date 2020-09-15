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
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class MongoDBJsonStore {
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

    //Image Data Ingestion related operations-----------------------------------------------------
    public void storeIngestionImage(JsonObject jsonObject)
    {
        String principal = securityTokenContainer.getTokenContainer().get().getPrincipal();
        String region = securityTokenContainer.getTokenContainer().get().getRegion();
        String databaseName = region + "_" + principal + "_" + "aiplatform";
        MongoDatabase database = mongoClient.getDatabase(databaseName);

        MongoCollection<Document> collection = database.getCollection("ingestionImage");

        Document doc = Document.parse(jsonObject.toString());
        collection.insertOne(doc);
    }

    public List<JsonObject> getIngestionImages()
    {
        List<JsonObject> ingestion = new ArrayList<>();

        String principal = securityTokenContainer.getTokenContainer().get().getPrincipal();
        String region = securityTokenContainer.getTokenContainer().get().getRegion();
        String databaseName = region + "_" + principal + "_" + "aiplatform";
        MongoDatabase database = mongoClient.getDatabase(databaseName);

        MongoCollection<Document> collection = database.getCollection("ingestionImage");

        FindIterable<Document> iterable = collection.find();
        MongoCursor<Document> cursor = iterable.cursor();
        while(cursor.hasNext())
        {
            Document document = cursor.next();
            String documentJson = document.toJson();
            JsonObject jsonObject = JsonParser.parseString(documentJson).getAsJsonObject();
            jsonObject.remove("_id");
            ingestion.add(jsonObject);
        }
        return ingestion;
    }

    //AIModelService related operations-----------------------------------------------------
    public void storeDevModels(JsonObject jsonObject)
    {
        String principal = securityTokenContainer.getTokenContainer().get().getPrincipal();
        String region = securityTokenContainer.getTokenContainer().get().getRegion();
        String databaseName = region + "_" + principal + "_" + "aiplatform";
        MongoDatabase database = mongoClient.getDatabase(databaseName);

        MongoCollection<Document> collection = database.getCollection("devModels");

        Document doc = Document.parse(jsonObject.toString());
        collection.insertOne(doc);
    }

    public JsonObject getDevModel(String runId)
    {
        JsonObject devModel = new JsonObject();

        String principal = securityTokenContainer.getTokenContainer().get().getPrincipal();
        String region = securityTokenContainer.getTokenContainer().get().getRegion();
        String databaseName = region + "_" + principal + "_" + "aiplatform";
        MongoDatabase database = mongoClient.getDatabase(databaseName);

        MongoCollection<Document> collection = database.getCollection("devModels");

        String queryJson = "{\"run_id\":\""+runId+"\"}";
        Bson bson = Document.parse(queryJson);
        FindIterable<Document> iterable = collection.find(bson);
        MongoCursor<Document> cursor = iterable.cursor();
        while(cursor.hasNext())
        {
            Document document = cursor.next();
            String documentJson = document.toJson();
            devModel = JsonParser.parseString(documentJson).getAsJsonObject();
            return devModel;
        }
        return devModel;
    }

    public void storeLiveModel(JsonObject jsonObject)
    {
        String principal = securityTokenContainer.getTokenContainer().get().getPrincipal();
        String region = securityTokenContainer.getTokenContainer().get().getRegion();
        String databaseName = region + "_" + principal + "_" + "aiplatform";
        MongoDatabase database = mongoClient.getDatabase(databaseName);

        MongoCollection<Document> collection = database.getCollection("liveModels");

        Document doc = Document.parse(jsonObject.toString());
        collection.insertOne(doc);
    }

    public JsonObject getLiveModel(String runId)
    {
        JsonObject liveModel = new JsonObject();

        String principal = securityTokenContainer.getTokenContainer().get().getPrincipal();
        String region = securityTokenContainer.getTokenContainer().get().getRegion();
        String databaseName = region + "_" + principal + "_" + "aiplatform";
        MongoDatabase database = mongoClient.getDatabase(databaseName);
        MongoCollection<Document> collection = database.getCollection("liveModels");

        String queryJson = "{\"run_id\":\""+runId+"\"}";
        Bson bson = Document.parse(queryJson);
        FindIterable<Document> iterable = collection.find(bson);
        MongoCursor<Document> cursor = iterable.cursor();
        while(cursor.hasNext())
        {
            Document document = cursor.next();
            String documentJson = document.toJson();
            liveModel = JsonParser.parseString(documentJson).getAsJsonObject();
            return liveModel;
        }
        return liveModel;
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
    public void storeModel(String model)
    {
        String principal = securityTokenContainer.getTokenContainer().get().getPrincipal();
        String region = securityTokenContainer.getTokenContainer().get().getRegion();
        String databaseName = region + "_" + principal + "_" + "aiplatform";
        MongoDatabase database = mongoClient.getDatabase(databaseName);

        MongoCollection<Document> collection = database.getCollection("liveModels");

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("modelId", "0");
        jsonObject.addProperty("model", model);
        Document doc = Document.parse(jsonObject.toString());
        collection.insertOne(doc);
    }

    public String getModel(String modelId)
    {
        String principal = securityTokenContainer.getTokenContainer().get().getPrincipal();
        String region = securityTokenContainer.getTokenContainer().get().getRegion();
        String databaseName = region + "_" + principal + "_" + "aiplatform";
        MongoDatabase database = mongoClient.getDatabase(databaseName);

        MongoCollection<Document> collection = database.getCollection("liveModels");

        String queryJson = "{\"modelId\":\""+modelId+"\"}";
        Bson bson = Document.parse(queryJson);
        FindIterable<Document> iterable = collection.find(bson);
        MongoCursor<Document> cursor = iterable.cursor();
        Document document = cursor.next();
        String documentJson = document.toJson();
        String model = JsonParser.parseString(documentJson).getAsJsonObject().get("model").getAsString();
        return model;
    }
}
