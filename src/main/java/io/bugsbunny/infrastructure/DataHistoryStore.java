package io.bugsbunny.infrastructure;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Singleton;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mongodb.client.*;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.OffsetDateTime;

@Singleton
public class DataHistoryStore {
    private static Logger logger = LoggerFactory.getLogger(DataHistoryStore.class);

    public void storeHistoryObject(Tenant tenant, MongoClient mongoClient, JsonObject json){
        String principal = tenant.getPrincipal();
        String databaseName = principal + "_" + "aiplatform";

        MongoDatabase database = mongoClient.getDatabase(databaseName);
        MongoCollection<Document> collection = database.getCollection("datahistory");
        collection.insertOne(Document.parse(json.toString()));
    }

    public JsonArray readHistory(Tenant tenant, MongoClient mongoClient, OffsetDateTime endTime){
        JsonArray history = new JsonArray();

        String principal = tenant.getPrincipal();
        String databaseName = principal + "_" + "aiplatform";
        MongoDatabase database = mongoClient.getDatabase(databaseName);

        MongoCollection<Document> collection = database.getCollection("datahistory");

        String queryJson = "{\"timestamp\":{ $lte: "+endTime.toEpochSecond()+"}}";
        System.out.println("********************");
        System.out.println(queryJson);
        Bson bson = Document.parse(queryJson);
        FindIterable<Document> iterable = collection.find(bson);
        MongoCursor<Document> cursor = iterable.cursor();
        while(cursor.hasNext())
        {
            Document document = cursor.next();
            String documentJson = document.toJson();
            JsonObject cour = JsonParser.parseString(documentJson).getAsJsonObject();
            history.add(cour);
        }

        return history;
    }
}
