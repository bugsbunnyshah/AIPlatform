package io.bugsbunny.dataScience.model;

import com.github.wnameless.json.flattener.JsonFlattener;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.*;

public class Artifact implements Serializable {
    private static Logger logger = LoggerFactory.getLogger(Team.class);

    private String artifactId;
    private PortableAIModelInterface aiModel;
    private DataSet dataSet;

    private List<Label> labels;
    private List<Feature> features;
    private Map<String,String> parameters;

    public Artifact() {
        this.labels = new ArrayList<>();
        this.features = new ArrayList<>();
        this.parameters = new HashMap<>();
    }

    public PortableAIModelInterface getAiModel() {
        return aiModel;
    }

    public void setAiModel(PortableAIModelInterface aiModel) {
        this.aiModel = aiModel;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public void setArtifactId(String artifactId) {
        this.artifactId = artifactId;
    }

    public DataSet getDataSet() {
        return dataSet;
    }

    public void setDataSet(DataSet dataSet) {
        this.dataSet = dataSet;
    }

    public List<Label> getLabels() {
        return labels;
    }

    public void setLabels(List<Label> labels) {
        this.labels = labels;
    }

    public void addLabel(Label label){
        this.labels.add(label);
    }

    public List<Feature> getFeatures() {
        return features;
    }

    public void setFeatures(List<Feature> features) {
        this.features = features;
    }

    public void addFeature(Feature feature){
        this.features.add(feature);
    }

    public Map<String, String> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, String> parameters) {
        this.parameters = parameters;
    }

    public void addParameter(String parameter,String value){
        this.parameters.put(parameter,value);
    }

    public String convertJsonToCsv(JsonArray jsonArray){
        StringBuilder csvBuilder = new StringBuilder();
        csvBuilder.append(this.getHeader()+"\n");
        int labelSize = this.labels.size();
        Map<String, Object> fieldMap = JsonFlattener.flattenAsMap(jsonArray.get(0).getAsJsonObject().toString());

        Map<String,List<String>> conversion = new HashMap<>();
        for(int i=0; i<labelSize;i++) {
            Label label = this.labels.get(i);
            String labelValue = label.getValue();
            String field = label.getField();
            Object value = fieldMap.get(field);
            if(value != null) {
                List<String> column = conversion.get(labelValue);
                if(column == null){
                    column = new ArrayList<>();
                    conversion.put(labelValue,column);
                }
                column.add(value.toString());
            }else{
                //Array
                String[] tokens = field.split("\\.");

                String realField = tokens[tokens.length-1];

                Set<String> fieldKeys = fieldMap.keySet();
                String variable=null;
                for(String token:tokens)
                {
                    String latestPath = variable;
                    if(latestPath == null || latestPath.trim().length() == 0) {
                        variable = token + "[";
                    }
                    else{
                        String temp = latestPath.substring(0, latestPath.length() - 1);
                        variable = temp + "." + token + "[";
                    }
                    for(String key:fieldKeys)
                    {
                        if(key.startsWith(variable) && (key.endsWith(realField) || key.endsWith(realField+".content"))){
                            value = fieldMap.get(key);
                            List<String> column = conversion.get(labelValue);
                            if(column == null){
                                column = new ArrayList<>();
                                conversion.put(labelValue,column);
                            }
                            column.add(value.toString());
                        }
                    }
                }
            }
        }
        //Prepare the cols
        Collection<List<String>> tmp = conversion.values();
        List<List<String>> cols = new ArrayList<>();
        for(List<String> cour:tmp){
            cols.add(cour);
        }

        //Prepare the rows
        List<Row> rows = new ArrayList<>();
        for(int i=0; i< cols.get(0).size();i++){
            rows.add(new Row());
        }

        for(int fieldsCount=0; fieldsCount<cols.size();fieldsCount++){
            List<String> fields = cols.get(fieldsCount);
            for(int i=0; i<fields.size();i++){
                String value = fields.get(i);
                Row row = rows.get(i);
                row.columns.add(value);
            }
        }

        for(Row row: rows){
            csvBuilder.append(row.toCsv()+"\n");
        }

        return csvBuilder.toString();
    }

    public String convertXmlToCsv(JsonArray jsonArray){
        JsonObject json = jsonArray.get(0).getAsJsonObject();
        json = json.get("rootXml").getAsJsonObject();

        JsonArray array = new JsonArray();
        array.add(json);
        return this.convertJsonToCsv(array);
    }

    private String getHeader(){
        StringBuilder headerBuilder = new StringBuilder();
        for(Label cour: this.labels){
            headerBuilder.append(cour.getValue()+",");
        }

        String header = headerBuilder.toString().substring(0, headerBuilder.toString().length()-1);

        return header;
    }

    @Override
    public String toString() {
        return this.toJson().toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Artifact artifact = (Artifact) o;
        return artifactId.equals(artifact.artifactId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(artifactId);
    }

    public JsonObject toJson(){
        JsonObject json = new JsonObject();

        if(this.artifactId != null){
            json.addProperty("artifactId",this.artifactId);
        }

        if(this.dataSet != null){
            json.add("dataSet", this.dataSet.toJson());
        }

        if(this.aiModel != null){
            json.add("aiModel",this.aiModel.toJson());
        }

        if(this.labels != null){
            json.add("labels", JsonParser.parseString(this.labels.toString()).getAsJsonArray());
        }

        if(this.features != null){
            json.add("features", JsonParser.parseString(this.features.toString()).getAsJsonArray());
        }

        if(this.parameters != null){
            JsonObject paramsJson = new JsonObject();
            Set<Map.Entry<String,String>> entrySet = this.parameters.entrySet();
            for(Map.Entry<String,String> entry:entrySet){
                String key = entry.getKey();
                String value = entry.getValue();
                paramsJson.addProperty(key,value);
            }
            json.add("parameters",paramsJson);
        }

        return json;
    }

    public static Artifact parse(String jsonString){
        Artifact artifact = new Artifact();

        JsonObject json = JsonParser.parseString(jsonString).getAsJsonObject();

        if(json.has("artifactId")){
            artifact.artifactId = json.get("artifactId").getAsString();
        }

        if(json.has("dataSet")){
            artifact.dataSet = DataSet.parse(json.get("dataSet").getAsJsonObject().toString());
        }

        if(json.has("aiModel") && json.get("aiModel").getAsJsonObject().has("modelId")){
            AIModel aiModel = AIModel.parse(json.get("aiModel").getAsJsonObject().toString());
            artifact.aiModel = aiModel;
        }

        if(json.has("labels")){
            JsonArray array = json.get("labels").getAsJsonArray();
            for(int i=0; i<array.size(); i++){
                JsonObject cour = array.get(i).getAsJsonObject();
                artifact.addLabel(new Label(cour.get("value").getAsString(),cour.get("field").getAsString()));
            }
        }

        if(json.has("features")){
            JsonArray array = json.get("features").getAsJsonArray();
            for(int i=0; i<array.size(); i++){
                JsonObject cour = array.get(i).getAsJsonObject();
                artifact.addFeature(new Feature(cour.get("value").getAsString()));
            }
        }

        if(json.has("parameters")){
            JsonObject paramsJson = json.get("parameters").getAsJsonObject();
            Set<Map.Entry<String, JsonElement>> entrySet = paramsJson.entrySet();
            for(Map.Entry<String, JsonElement> entry:entrySet){
                String key = entry.getKey();
                JsonElement value = entry.getValue();
                artifact.addParameter(key,value.getAsString());
            }
        }

        return artifact;
    }

    private static class Row{
        List<String> columns;

        private Row(){
            this.columns = new ArrayList<>();
        }

        @Override
        public String toString() {
            return "Row{" +
                    "columns=" + columns +
                    '}';
        }

        public String toCsv(){
            StringBuilder csv = new StringBuilder();
            for(String column:columns){
                csv.append(column+",");
            }
            return csv.substring(0,csv.toString().length()-1);
        }
    }
}
