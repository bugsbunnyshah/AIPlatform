package io.bugsbunny.persistence;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mongodb.client.*;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class MongoDBJsonStore {
    private static Logger logger = LoggerFactory.getLogger(MongoDBJsonStore.class);

    private MongoClient mongoClient = MongoClients.create();;

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
        MongoDatabase database = mongoClient.getDatabase("aiplatform");

        MongoCollection<Document> collection = database.getCollection("ingestion");

        for(JsonObject jsonObject:jsonObjects) {
            Document doc = Document.parse(jsonObject.toString());
            collection.insertOne(doc);
        }
    }


    public JsonObject getIngestion(String ingestionId)
    {
        JsonObject ingestion = new JsonObject();

        MongoDatabase database = mongoClient.getDatabase("aiplatform");

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

        MongoDatabase database = mongoClient.getDatabase("aiplatform");

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
        MongoDatabase database = mongoClient.getDatabase("aiplatform");

        MongoCollection<Document> collection = database.getCollection("ingestionImage");

        Document doc = Document.parse(jsonObject.toString());
        collection.insertOne(doc);
    }

    public List<JsonObject> getIngestionImages()
    {
        List<JsonObject> ingestion = new ArrayList<>();

        MongoDatabase database = mongoClient.getDatabase("aiplatform");

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

    //AIModel related operations-----------------------------------------------------
    public void storeDevModels(JsonObject jsonObject)
    {
        MongoDatabase database = mongoClient.getDatabase("aiplatform");

        MongoCollection<Document> collection = database.getCollection("devModels");

        Document doc = Document.parse(jsonObject.toString());
        collection.insertOne(doc);
    }

    public JsonObject getDevModel(String runId)
    {
        JsonObject devModel = new JsonObject();

        MongoDatabase database = mongoClient.getDatabase("aiplatform");

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

    //Data History related operations-----------------------------------------------------
    public String startDiffChain(JsonObject payload)
    {
        MongoDatabase database = mongoClient.getDatabase("aiplatform");

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
        MongoDatabase database = mongoClient.getDatabase("aiplatform");
        MongoCollection<Document> collection = database.getCollection("diffChain");

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("chainId", chainId);
        jsonObject.add("payload", payload);

        Document doc = Document.parse(jsonObject.toString());
        collection.insertOne(doc);
    }

    public JsonObject getLastPayload(String chainId)
    {
        JsonObject lastPayload = new JsonObject();

        MongoDatabase database = mongoClient.getDatabase("aiplatform");
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
        MongoDatabase database = mongoClient.getDatabase("aiplatform");
        MongoCollection<Document> collection = database.getCollection("objectDiff");

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("chainId", chainId);
        jsonObject.add("objectDiff", objectDiff);

        Document doc = Document.parse(jsonObject.toString());
        collection.insertOne(doc);
    }

    public List<JsonObject> readDiffs(String chainId)
    {
        List<JsonObject> diffs = new LinkedList<>();

        MongoDatabase database = mongoClient.getDatabase("aiplatform");

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

    public List<JsonObject> readDiffChain(String chainId)
    {
        List<JsonObject> chain = new LinkedList<>();

        MongoDatabase database = mongoClient.getDatabase("aiplatform");

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
}
