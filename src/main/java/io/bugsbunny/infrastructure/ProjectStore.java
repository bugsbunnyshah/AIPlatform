package io.bugsbunny.infrastructure;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import com.mongodb.client.*;

import io.bugsbunny.dataScience.model.Project;

import org.bson.Document;
import org.bson.conversions.Bson;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
class ProjectStore {
    private Logger logger = LoggerFactory.getLogger(ProjectStore.class);

    public List<Project> readProjects(Tenant tenant, MongoClient mongoClient)
    {
        List<Project> projects = new ArrayList<>();

        System.out.println(tenant);

        //String principal = tenant.getPrincipal();
        String principal = "-2061008798";
        String databaseName = principal + "_" + "aiplatform";
        MongoDatabase database = mongoClient.getDatabase(databaseName);

        MongoCollection<Document> collection = database.getCollection("project");

        FindIterable<Document> iterable = collection.find();
        MongoCursor<Document> cursor = iterable.cursor();
        if(cursor.hasNext())
        {
            Document document = cursor.next();
            String documentJson = document.toJson();
            JsonObject cour = JsonParser.parseString(documentJson).getAsJsonObject();
            projects.add(Project.parse(cour.toString()));
        }

        return projects;
    }

    public Project readProject(Tenant tenant, MongoClient mongoClient,String projectId)
    {
        Project project = null;

        String principal = tenant.getPrincipal();
        String databaseName = principal + "_" + "aiplatform";
        MongoDatabase database = mongoClient.getDatabase(databaseName);

        MongoCollection<Document> collection = database.getCollection("project");

        String queryJson = "{\"projectId\":\""+projectId+"\"}";
        Bson bson = Document.parse(queryJson);
        FindIterable<Document> iterable = collection.find(bson);
        MongoCursor<Document> cursor = iterable.cursor();
        if(cursor.hasNext())
        {
            Document document = cursor.next();
            String documentJson = document.toJson();
            JsonObject cour = JsonParser.parseString(documentJson).getAsJsonObject();
            project = Project.parse(cour.toString());
        }

        return project;
    }

    public void addProject(Tenant tenant, MongoClient mongoClient,Project project){
        String principal = tenant.getPrincipal();
        String databaseName = principal + "_" + "aiplatform";

        JsonObject json = project.toJson();
        MongoDatabase database = mongoClient.getDatabase(databaseName);
        MongoCollection<Document> collection = database.getCollection("project");
        collection.insertOne(Document.parse(json.toString()));
    }

    public void updateProject(Tenant tenant, MongoClient mongoClient, Project project){
        String principal = tenant.getPrincipal();
        String databaseName = principal + "_" + "aiplatform";

        MongoDatabase database = mongoClient.getDatabase(databaseName);
        MongoCollection<Document> collection = database.getCollection("project");

        String projectId = project.getProjectId();
        String queryJson = "{\"projectId\":\""+projectId+"\"}";
        Bson bson = Document.parse(queryJson);
        collection.replaceOne(bson,Document.parse(project.toString()));
    }
}
