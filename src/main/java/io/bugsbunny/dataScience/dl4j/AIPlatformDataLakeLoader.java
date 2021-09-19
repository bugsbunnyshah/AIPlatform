package io.bugsbunny.dataScience.dl4j;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.bugsbunny.dataScience.model.Artifact;
import io.bugsbunny.infrastructure.MongoDBJsonStore;
import io.bugsbunny.preprocess.SecurityToken;
import io.bugsbunny.preprocess.SecurityTokenContainer;
import io.bugsbunny.util.JsonUtil;
import org.json.JSONObject;
import org.json.XML;
import org.nd4j.common.loader.Loader;
import org.nd4j.common.loader.Source;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.cpu.nativecpu.NDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;

/*
String resource = "dataScience/saturn_data_train.csv";
String resource = "dataScience/mnistTrain.bin";
*/

@ApplicationScoped
public class AIPlatformDataLakeLoader implements Loader
{
    private static Logger logger = LoggerFactory.getLogger(AIPlatformDataLakeLoader.class);

    @Inject
    private MongoDBJsonStore mongoDBJsonStore;

    @Inject
    private SecurityTokenContainer securityTokenContainer;

    public MongoDBJsonStore getMongoDBJsonStore() {
        return mongoDBJsonStore;
    }

    public void setMongoDBJsonStore(MongoDBJsonStore mongoDBJsonStore) {
        this.mongoDBJsonStore = mongoDBJsonStore;
    }

    public SecurityTokenContainer getSecurityTokenContainer() {
        return securityTokenContainer;
    }

    public void setSecurityTokenContainer(SecurityTokenContainer securityTokenContainer) {
        this.securityTokenContainer = securityTokenContainer;
    }

    @Override
    public Object load(Source source) throws IOException
    {
        DataSet dataSet = new DataSet();
        try {
            String path = source.getPath();
            Artifact artifact = ((AIPlatformDataLakeSource)source).getArtifact();
            JsonArray dataSetArray = JsonParser.parseString(path).getAsJsonArray();
            SecurityToken securityToken = ((AIPlatformDataLakeSource) source).getSecurityToken();
            this.securityTokenContainer.setSecurityToken(securityToken);

            String id = dataSetArray.get(0).getAsString();
            JsonArray dataLakeArray = mongoDBJsonStore.getIngestion(this.securityTokenContainer.getTenant(), id);

            //Get the Data
            boolean isJson = false;
            boolean isXml = false;
            JsonArray dataArray = new JsonArray();
            int size = dataLakeArray.size();
            for(int i=0; i<size; i++){
                JsonObject cour = dataLakeArray.get(i).getAsJsonObject();
                String data = cour.get("data").getAsString();
                if(data.startsWith("{") || data.startsWith("[")) {
                    //Its json
                    dataArray.add(JsonParser.parseString(data).getAsJsonObject());
                    isJson = true;
                }
                else if(data.contains("<") && data.contains(">")){
                    //Its xml
                    JSONObject sourceJson = XML.toJSONObject(data);
                    String json = sourceJson.toString(4);
                    JsonObject sourceJsonObject = JsonParser.parseString(json).getAsJsonObject();
                    dataArray.add(sourceJsonObject);
                    isXml = true;
                }
                else {
                    //Its CSV
                    dataArray.add(data);
                }
            }

            //Convert To Csv
            String csvData;
            if(isJson){
                csvData = artifact.convertJsonToCsv(dataArray);
            }
            else if(isXml){
                csvData = artifact.convertXmlToCsv(dataArray);
            }
            else{
                csvData = dataArray.get(0).getAsString();
            }

            logger.info("CSVData: "+csvData);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            byteArrayOutputStream.writeBytes(csvData.getBytes(StandardCharsets.UTF_8));
            dataSet.save(byteArrayOutputStream);

            return dataSet;
        }
        catch(Exception e)
        {
            logger.error(e.getMessage(),e);
            return dataSet;
        }
    }
}
