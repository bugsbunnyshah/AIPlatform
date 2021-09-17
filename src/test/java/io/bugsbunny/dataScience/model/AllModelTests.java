package io.bugsbunny.dataScience.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import io.bugsbunny.util.JsonUtil;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class AllModelTests {
    private static Logger logger = LoggerFactory.getLogger(AllModelTests.class);

    @Test
    public void testScientistSer() throws Exception{
        Scientist scientist = mockScientist();
        String email = scientist.getEmail();

        JsonObject json = scientist.toJson();
        JsonUtil.print(json);
        String emailInJson = json.get("email").getAsString();
        assertEquals(email,emailInJson);

        Scientist deser = Scientist.parse(json.toString());
        logger.info(deser.toString());
        assertEquals(email,deser.getEmail());
    }

    @Test
    public void testTeamSer() throws Exception{
        Team team = mockTeam();

        JsonObject json = team.toJson();
        JsonUtil.print(json);
        JsonArray array = JsonParser.parseString(json.toString()).getAsJsonObject().
                get("scientists").getAsJsonArray();
        assertEquals(array.size(),team.getScientists().size());

        Team deser = Team.parse(json.toString());
        assertEquals(team.getScientists().size(),deser.getScientists().size());
        Scientist original = team.getScientists().get(0);
        assertTrue(deser.getScientists().contains(original));
    }

    @Test
    public void testDataItemSer() throws Exception{
        DataItem dataItem = mockDataItem();

        JsonObject json = dataItem.toJson();
        JsonUtil.print(json);
        String dataLakeIdOriginal = json.get("dataLakeId").getAsString();
        assertEquals(dataItem.getDataLakeId(),dataLakeIdOriginal);

        DataItem deser = DataItem.parse(json.toString());
        deser.setData("Different");
        logger.info(deser.toString());
        assertEquals(dataItem.getDataLakeId(),deser.getDataLakeId());
        assertNotEquals(dataItem.getData(),deser.getData());
        assertEquals(dataItem,deser);
    }

    @Test
    public void testDataSetSer() throws Exception{
        DataSet dataSet = mockDataSet();

        JsonObject json = dataSet.toJson();
        JsonUtil.print(json);
        String dataSetIdOriginal = json.get("dataSetId").getAsString();
        assertEquals(dataSet.getDataSetId(),dataSetIdOriginal);

        DataSet deser = DataSet.parse(json.toString());
        logger.info(deser.toString());
        assertEquals(dataSet.getDataSetId(),deser.getDataSetId());
        assertEquals(dataSet.getData(),deser.getData());
    }

    @Test
    public void testArtifactSer() throws Exception{
        Artifact artifact = mockArtifact();

        JsonObject json = artifact.toJson();
        JsonUtil.print(AllModelTests.class,json);
        String artifactIdOriginal = json.get("artifactId").getAsString();
        assertEquals(artifact.getArtifactId(),artifactIdOriginal);

        Artifact deser = Artifact.parse(json.toString());
        JsonUtil.print(AllModelTests.class,deser.toJson());
        assertEquals(artifact.getArtifactId(),deser.getArtifactId());
        assertEquals(artifact.getAiModel().getModelId(),deser.getAiModel().getModelId());
        assertEquals(artifact.getAiModel(),deser.getAiModel());
        assertEquals(artifact.getDataSet().getDataSetId(),deser.getDataSet().getDataSetId());
        assertEquals(artifact.getDataSet().getData(),deser.getDataSet().getData());
        assertEquals(artifact.getLabels(),deser.getLabels());
        assertEquals(artifact.getFeatures(),deser.getFeatures());

        String data = IOUtils.toString(Thread.currentThread().
                        getContextClassLoader().
                        getResourceAsStream("aviation/flights0.json"),
                StandardCharsets.UTF_8);

        JsonArray dataJson = JsonParser.parseString(data).getAsJsonObject().get("data").getAsJsonArray();
        String csv = deser.convertJsonToCsv(dataJson);
        logger.info("*****CSV******");
        logger.info(csv);
        assertNotNull(csv);
    }

    @Test
    public void testProjectSer() throws Exception{
        Project project = this.mockProject();

        JsonObject json = project.toJson();
        JsonUtil.print(json);
        String idOriginal = json.get("projectId").getAsString();
        assertEquals(project.getProjectId(),idOriginal);

        Project deser = Project.parse(json.toString());
        JsonUtil.print(deser.toJson());
        assertEquals(project.getProjectId(),deser.getProjectId());
        assertEquals(project.getTeam().getScientists(),deser.getTeam().getScientists());
        assertEquals(project.getArtifacts(),deser.getArtifacts());
    }

    @Test
    public void testDataSetSerWithActualData() throws Exception{
        String[] data = new String[]{"0","1","2"};
        DataSet dataSet = mockDataSet(data);

        JsonObject json = dataSet.toJson();
        JsonUtil.print(json);
        String dataSetIdOriginal = json.get("dataSetId").getAsString();
        assertEquals(dataSet.getDataSetId(),dataSetIdOriginal);

        DataSet deser = DataSet.parse(json.toString());
        JsonUtil.print(deser.toJson());
        assertEquals(dataSet.getDataSetId(),deser.getDataSetId());
        assertEquals(dataSet.getData(),deser.getData());

        List<String> original = Arrays.asList(data);
        List<String> deserData = deser.getDataList();

        logger.info(deserData.toString());
        assertEquals(original,deserData);
    }


    public static Scientist mockScientist(){
        String username = UUID.randomUUID().toString();
        String email = username+"@test.io";
        Scientist scientist = new Scientist();
        scientist.setEmail(email);
        return scientist;
    }

    public static Team mockTeam(){
        Team team = new Team();
        for(int i=0; i<3; i++) {
            Scientist scientist = new Scientist();
            scientist.setEmail("test"+i+"@test.io");
            team.addScientist(scientist);
        }
        return team;
    }

    public static DataItem mockDataItem(){
        DataItem dataItem = new DataItem();

        dataItem.setTenantId(UUID.randomUUID().toString());
        dataItem.setDataLakeId(UUID.randomUUID().toString());
        dataItem.setData(UUID.randomUUID().toString());
        dataItem.setChainId(UUID.randomUUID().toString());

        return dataItem;
    }

    public static List<DataItem> mockDataItems(String[] data){
        List<DataItem> items = new ArrayList<>();

        for(String cour:data) {
            DataItem dataItem = new DataItem();
            dataItem.setDataLakeId("braineous_null");
            dataItem.setTenantId(UUID.randomUUID().toString());
            dataItem.setData(cour);
            items.add(dataItem);
        }

        return items;
    }

    public static DataSet mockDataSet(String[] data){
        DataSet dataSet = new DataSet();

        dataSet.setDataSetId(UUID.randomUUID().toString());
        List<DataItem> dataItems = mockDataItems(data);
        dataSet.setData(dataItems);

        return dataSet;
    }

    public static DataSet mockDataSet(){
        DataSet dataSet = new DataSet();

        dataSet.setDataSetId(UUID.randomUUID().toString());
        for(int i=0; i<3; i++){
            dataSet.addDataItem(mockDataItem());
        }

        return dataSet;
    }

    public static  Artifact mockArtifact(){
        Artifact artifact = new Artifact();
        artifact.setArtifactId(UUID.randomUUID().toString());

        AIModel aiModel = new AIModel();
        aiModel.setModelId(UUID.randomUUID().toString());
        aiModel.setLanguage("java");
        artifact.setDataSet(mockDataSet());
        artifact.setAiModel(aiModel);

        for(int i=0; i<2; i++){
            if(i==0) {
                artifact.addLabel(new Label("l" + i, "flight_date"));
            }
            else if(i==1){
                artifact.addLabel(new Label("l" + i, "flight_status"));
            }
        }

        for(int i=0; i<3; i++){
            artifact.addFeature(new Feature("f"+i));
        }

        return artifact;
    }

    public static Project mockProject(){
        Project project = new Project();
        project.setProjectId(UUID.randomUUID().toString());
        project.setTeam(mockTeam());
        for(int i=0; i<3; i++){
            project.addArtifact(mockArtifact());
        }
        return project;
    }
}
