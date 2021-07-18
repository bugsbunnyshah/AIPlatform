package io.bugsbunny.infrastructure;

import com.google.gson.*;

import io.bugsbunny.dataScience.service.PackagingService;
import io.bugsbunny.preprocess.SecurityTokenContainer;
import io.bugsbunny.test.components.BaseTest;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.Response;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
public class MongoDBJsonStoreTests extends BaseTest {
    private static Logger logger = LoggerFactory.getLogger(MongoDBJsonStoreTests.class);

    private Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .create();

    @Inject
    private MongoDBJsonStore mongoDBJsonStore;

    @Inject
    private PackagingService packagingService;

    @Inject
    private SecurityTokenContainer securityTokenContainer;


    @AfterEach
    public void tearDown()
    {

    }

    @Test
    public void testStoreDataSetRealDataForEval() throws Exception
    {
        String csv = IOUtils.toString(
                Thread.currentThread().getContextClassLoader().getResourceAsStream("dataScience/saturn_data_eval.csv"),
                StandardCharsets.UTF_8);
        //logger.info(csv);

        JsonObject dataSetJson = new JsonObject();
        dataSetJson.addProperty("format","csv");
        dataSetJson.addProperty("data", csv);
        String oid = this.mongoDBJsonStore.storeTrainingDataSet(this.securityTokenContainer.getTenant(),dataSetJson);
        logger.info("Lookup DataSetId: "+oid);

        JsonObject dataSet = this.mongoDBJsonStore.readDataSet(this.securityTokenContainer.getTenant(),oid);
        String csvData = dataSet.get("data").getAsString();
        String storedOid = dataSet.get("dataSetId").getAsString();
        logger.info(""+storedOid);
        assertEquals(oid, storedOid);
        assertEquals(csv, csvData);
    }

    @Test
    public void testRollOverToTraningDataSets() throws Exception
    {
        String modelPackage = IOUtils.resourceToString("dataScience/aiplatform-model.json", StandardCharsets.UTF_8,
                Thread.currentThread().getContextClassLoader());

        JsonObject liveModelDeployedJson = this.packagingService.performPackaging(modelPackage);
        String modelId = liveModelDeployedJson.get("modelId").getAsString();

        String data = IOUtils.resourceToString("dataScience/saturn_data_eval.csv", StandardCharsets.UTF_8,
                Thread.currentThread().getContextClassLoader());
        JsonObject input = new JsonObject();
        input.addProperty("modelId", modelId);
        input.addProperty("format", "csv");
        input.addProperty("data", data);
        Response response = given().body(input.toString()).when().post("/dataset/storeEvalDataSet/").andReturn();
        logger.info("************************");
        logger.info("ModelId: "+modelId);
        logger.info(response.statusLine());
        response.body().prettyPrint();
        logger.info("************************");
        assertEquals(200, response.getStatusCode());
        String dataSetId = JsonParser.parseString(response.body().asString()).getAsJsonObject().get("dataSetId").getAsString();


        JsonObject rolledOverDataSetIds = this.mongoDBJsonStore.rollOverToTraningDataSets(this.securityTokenContainer.getTenant(),
                modelId);
        logger.info(rolledOverDataSetIds.toString());

        //Assert
        List<String> dataSetIds = new ArrayList<>();
        JsonArray array = rolledOverDataSetIds.getAsJsonArray("rolledOverDataSetIds");
        Iterator<JsonElement> iterator = array.iterator();
        while(iterator.hasNext())
        {
            dataSetIds.add(iterator.next().getAsString());
        }
        assertTrue(dataSetIds.contains(dataSetId));
    }

    //TODO
    /*@Test
    public void testIngestion() throws Exception
    {
        String csv = IOUtils.toString(
                Thread.currentThread().getContextClassLoader().getResourceAsStream("dataScience/saturn_data_eval.csv"),
                StandardCharsets.UTF_8);
        //logger.info(csv);

        JsonObject dataSetJson = new JsonObject();
        dataSetJson.addProperty("data", csv);
        long dataLakeId = this.mongoDBJsonStore.storeIngestion(dataSetJson);
        logger.info("Lookup DataLakeId: "+dataLakeId);

        JsonObject data = this.mongoDBJsonStore.getIngestion(dataLakeId);
        logger.info(data.toString());
        logger.info(data.get("dataLakeId").getAsString());
    }*/
}
