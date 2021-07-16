package io.bugsbunny.dataIngestion.service;

import akka.NotUsed;
import akka.actor.ActorSystem;
import akka.japi.function.Procedure;
import akka.stream.javadsl.Source;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.bugsbunny.Braineous;
import io.bugsbunny.data.history.service.DataReplayService;
import io.bugsbunny.dataIngestion.util.CSVDataUtil;
import io.bugsbunny.infrastructure.MongoDBJsonStore;
import org.apache.commons.io.IOUtils;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.StorageLevels;
import org.apache.spark.api.java.function.VoidFunction;
import org.apache.spark.storage.StorageLevel;
import org.apache.spark.streaming.Duration;
import org.apache.spark.streaming.api.java.JavaDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import org.apache.spark.streaming.receiver.Receiver;
import org.mitre.harmony.matchers.ElementPair;
import org.mitre.harmony.matchers.MatcherManager;
import org.mitre.harmony.matchers.MatcherScore;
import org.mitre.harmony.matchers.MatcherScores;
import org.mitre.harmony.matchers.matchers.Matcher;
import org.mitre.schemastore.model.Entity;
import org.mitre.schemastore.model.Schema;
import org.mitre.schemastore.model.SchemaElement;
import org.mitre.schemastore.model.schemaInfo.FilteredSchemaInfo;
import org.mitre.schemastore.model.schemaInfo.HierarchicalSchemaInfo;
import org.mitre.schemastore.model.schemaInfo.SchemaInfo;
import org.mitre.schemastore.model.schemaInfo.model.RelationalSchemaModel;
import org.mitre.schemastore.model.schemaInfo.model.SchemaModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.IOException;
import java.io.Serializable;
import java.util.*;

@ApplicationScoped
public class MapperService implements Serializable {
    private static Logger logger = LoggerFactory.getLogger(MapperService.class);

    public JsonObject map(JsonArray sourceData)
    {
        JsonObject result = StreamIngesterContext.getStreamIngester().submit(sourceData);
        return result;
    }


    public JsonObject mapXml(JsonObject sourceData)
    {
        JsonArray result = new JsonArray();
        this.traverse(sourceData, result);
        return this.map(result);
    }
    //---------------------------------------------------------------------------------------------------------------------
    static HierarchicalSchemaInfo createHierachialSchemaInfo(String schemaName)
    {
        Schema schema = new Schema();
        schema.setName(schemaName);

        SchemaModel schemaModel = new RelationalSchemaModel();
        schemaModel.setName(schemaName+"Model");
        SchemaInfo schemaInfo1 = new SchemaInfo(schema, new ArrayList<>(), new ArrayList<>());
        HierarchicalSchemaInfo schemaInfo = new HierarchicalSchemaInfo(schemaInfo1);
        schemaInfo.setModel(schemaModel);

        return schemaInfo;
    }

    static HierarchicalSchemaInfo populateHierarchialSchema(String object, String sourceData, String parent)
    {
        HierarchicalSchemaInfo schemaInfo = createHierachialSchemaInfo(object);
        JsonElement sourceElement = JsonParser.parseString(sourceData);
        JsonObject jsonObject = new JsonObject();
        if(!sourceElement.isJsonPrimitive())
        {
            jsonObject = sourceElement.getAsJsonObject();
        }
        else
        {
            jsonObject = new JsonObject();
            jsonObject.addProperty(sourceData, sourceElement.toString());
        }

        Set<Map.Entry<String, JsonElement>> entrySet = jsonObject.entrySet();
        for(Map.Entry<String, JsonElement> entry:entrySet)
        {
            String field = entry.getKey();
            JsonElement jsonElement = entry.getValue();

            if(jsonElement.isJsonObject())
            {
                Entity element = new Entity();
                element.setId(field.hashCode());
                element.setName(field);
                element.setDescription(field);
                schemaInfo.addElement(element);
                HierarchicalSchemaInfo fieldInfos = populateHierarchialSchema(field,
                        jsonElement.getAsJsonObject().toString(), object);

                ArrayList<SchemaElement> blah = fieldInfos.getElements(Entity.class);
                for(SchemaElement local:blah)
                {
                    schemaInfo.addElement(local);
                }

                continue;
            }
            else if(jsonElement.isJsonArray())
            {
                JsonElement top = jsonElement.getAsJsonArray().get(0);
                HierarchicalSchemaInfo fieldInfos = populateHierarchialSchema(field,
                        top.toString(), object);

                ArrayList<SchemaElement> blah = fieldInfos.getElements(Entity.class);
                for(SchemaElement local:blah)
                {
                    schemaInfo.addElement(local);
                }

                continue;
            }
            else
            {
                String objectLocation = parent + "." + object + "." + field;
                Entity element = new Entity();
                element.setId(objectLocation.hashCode());
                element.setName(objectLocation);
                element.setDescription(objectLocation);
                schemaInfo.addElement(element);
            }
        }

        return schemaInfo;
    }

    static JsonObject performMapping(Map<SchemaElement, Double> scores, String json) throws IOException
    {
        JsonObject jsonObject = JsonParser.parseString(json).getAsJsonObject();

        JsonObject result = new JsonObject();
        Set<Map.Entry<SchemaElement, Double>> entrySet = scores.entrySet();
        for(Map.Entry<SchemaElement, Double> entry: entrySet)
        {
            SchemaElement schemaElement = entry.getKey();
            Double score = entry.getValue();
            String field = schemaElement.getName();
            StringTokenizer tokenizer = new StringTokenizer(field, ".");
            while(tokenizer.hasMoreTokens())
            {
                String local = tokenizer.nextToken();
                if(!jsonObject.has(local))
                {
                    continue;
                }
                result.add(local, jsonObject.get(local));
            }
        }

        return result;
    }

    static Map<SchemaElement,Double> findMatches(FilteredSchemaInfo f1, FilteredSchemaInfo f2,
                                                  ArrayList<SchemaElement> sourceElements)
    {
        Map<SchemaElement, Double> result = new HashMap<>();
        Matcher matcher = MatcherManager.getMatcher(
                "org.mitre.harmony.matchers.matchers.EditDistanceMatcher");
        matcher.initialize(f1, f2);

        MatcherScores matcherScores = matcher.match();
        Set<ElementPair> elementPairs = matcherScores.getElementPairs();
        for (ElementPair elementPair : elementPairs) {
            MatcherScore matcherScore = matcherScores.getScore(elementPair);
            Double score = 0d;
            if(matcherScore != null) {
                score = matcherScore.getTotalEvidence();
            }
            for(SchemaElement schemaElement: sourceElements)
            {
                if(schemaElement.getId() == elementPair.getSourceElement())
                {
                    result.put(schemaElement, score);
                }
            }
        }
        return result;
    }

    private void traverse(JsonObject currentObject, JsonArray result)
    {
        Iterator<String> allProps = currentObject.keySet().iterator();
        while(allProps.hasNext())
        {
            String nextObject = allProps.next();
            JsonElement resolve = currentObject.get(nextObject);
            if(resolve.isJsonObject())
            {
                JsonObject resolveJson = resolve.getAsJsonObject();
                if(resolveJson.keySet().size()==0)
                {
                    //EMPTY TAG...skip it
                    continue;
                }
                if(resolveJson.keySet().size()==1) {
                    //logger.info(nextObject+": RESOLVING");
                    this.resolve(nextObject, resolveJson, result);
                }
                else
                {
                    //logger.info(nextObject+": TRAVERSING");
                    this.traverse(resolveJson, result);
                }
            }
        }
    }

    private void resolve(String parent, JsonObject leaf, JsonArray result)
    {
        JsonArray finalResult=null;
        if (leaf.isJsonObject()) {
            String child = leaf.keySet().iterator().next();
            JsonElement childElement = leaf.get(child);
            if(childElement.isJsonArray()) {
                //logger.info(parent+": CHILD_ARRAY");
                finalResult = childElement.getAsJsonArray();
            }
            else
            {
                //logger.info(parent+": CHILD_OBJECT");
                finalResult = new JsonArray();
                finalResult.add(childElement);
                //this.traverse(childElement.getAsJsonObject(), result);
            }
        } else {
            //logger.info(parent+": LEAF_ARRAY");
            finalResult = leaf.getAsJsonArray();
        }


        if(finalResult != null) {
            //logger.info(parent+": CALCULATING");
            Iterator<JsonElement> itr = finalResult.iterator();
            JsonArray jsonArray = new JsonArray();
            while (itr.hasNext())
            {
                JsonElement jsonElement = itr.next();
                if(jsonElement.isJsonPrimitive())
                {
                    JsonObject primitive = new JsonObject();
                    primitive.addProperty(parent,jsonElement.toString());
                    jsonArray.add(primitive);
                }
                else {
                    jsonArray.add(jsonElement);
                }
            }
            JsonObject jsonObject = new JsonObject();
            jsonObject.add(parent,jsonArray);
            result.add(jsonObject);
        }
    }

    private static class JavaCustomReceiver extends Receiver<String> {
        private String data;

        public JavaCustomReceiver(StorageLevel storageLevel) {
            super(storageLevel);
        }

        @Override
        public void onStart() {
            try {

                // Start the thread that receives data over a connection
                new Thread(this::receive).start();
            }
            catch (Exception e){
                throw new RuntimeException(e);
            }
        }

        @Override
        public void onStop() {
            // There is nothing much to do as the thread calling receive()
            // is designed to stop by itself if isStopped() returns false
        }

        void receiveData(String data)
        {
            this.data = data;
        }

        private void receive() {
            try {
                // Until stopped or connection broken continue reading
                while (!isStopped()) {
                    if(this.data != null) {
                        JsonArray jsonArray = JsonParser.parseString(this.data).getAsJsonArray();
                        Iterator<JsonElement> iterator = jsonArray.iterator();
                        while (iterator.hasNext()) {
                            store(iterator.next().getAsJsonObject().toString());
                        }
                        this.data = null;
                    }
                }
                restart("RESTARTING.......");
            } catch(Throwable t) {
                // restart if there is any other error
                t.printStackTrace();
                restart("Error receiving data", t);
            }
        }
    }
}
