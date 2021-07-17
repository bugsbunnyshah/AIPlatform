package io.bugsbunny.dataIngestion.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;

public class StreamIngesterContext implements Serializable {
    private static Logger logger = LoggerFactory.getLogger(StreamIngesterContext.class);

    private static StreamIngester streamIngester = new StreamIngester();
    private static StreamIngesterContext streamIngesterContext = new StreamIngesterContext();

    private StreamIngesterQueue streamIngesterQueue;
    private String mongodbHost;
    private String mongodbPort;
    private String mongoDbUser;
    private String mongodbPassword;
    private MongoClient mongoClient;


    private StreamIngesterContext()
    {
        this.streamIngesterQueue = new StreamIngesterQueue();
    }

    public static StreamIngester getStreamIngester()
    {
        if(StreamIngesterContext.streamIngester == null){
            StreamIngesterContext.streamIngester = new StreamIngester();
        }
        return StreamIngesterContext.streamIngester;
    }

    public static StreamIngesterContext getStreamIngesterContext()
    {
        if(StreamIngesterContext.streamIngesterContext == null){
            StreamIngesterContext.streamIngesterContext = new StreamIngesterContext();
        }
        return StreamIngesterContext.streamIngesterContext;
    }

    public void addStreamObject(StreamObject streamObject)
    {
        this.streamIngesterQueue.add(streamObject);
    }

    public StreamObject getLatest(){
        return this.streamIngesterQueue.latest();
    }

    public String getMongodbHost() {
        return mongodbHost;
    }

    public void setMongodbHost(String mongodbHost) {
        this.mongodbHost = mongodbHost;
    }

    public String getMongodbPort() {
        return mongodbPort;
    }

    public void setMongodbPort(String mongodbPort) {
        this.mongodbPort = mongodbPort;
    }

    public String getMongoDbUser() {
        return mongoDbUser;
    }

    public void setMongoDbUser(String mongoDbUser) {
        this.mongoDbUser = mongoDbUser;
    }

    public String getMongodbPassword() {
        return mongodbPassword;
    }

    public void setMongodbPassword(String mongodbPassword) {
        this.mongodbPassword = mongodbPassword;
    }

    public void ingestData(String principal, JsonObject jsonObject)
    {
        String databaseName = principal + "_" + "aiplatform";
        MongoDatabase database = this.getClient().getDatabase(databaseName);

        logger.info("********************************************");
        logger.info(jsonObject.toString());
        logger.info("********************************************");

        MongoCollection<Document> collection = database.getCollection("datalake");
        collection.insertOne(Document.parse(jsonObject.toString()));
    }

    private MongoClient getClient()
    {
        try {
            if(this.mongoClient != null){
                return this.mongoClient;
            }

            //mongodb://[username:password@]host1[:port1][,host2[:port2],...[,hostN[:portN]]][/[database][?options]]
            StringBuilder connectStringBuilder = new StringBuilder();
            connectStringBuilder.append("mongodb://");

            if (this.mongoDbUser!=null && this.mongodbPassword!=null) {
                connectStringBuilder.append(this.mongoDbUser
                        + ":" + this.mongodbPassword + "@");
            }
            connectStringBuilder.append(mongodbHost + ":" + mongodbPort);

            String connectionString = connectStringBuilder.toString();
            MongoClient mongoClient = MongoClients.create(connectionString);

            return mongoClient;
        }
        catch(Exception e)
        {
            throw new RuntimeException(e);
        }
    }
}
