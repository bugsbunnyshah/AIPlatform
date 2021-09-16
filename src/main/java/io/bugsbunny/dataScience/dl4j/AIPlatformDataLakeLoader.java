package io.bugsbunny.dataScience.dl4j;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.bugsbunny.infrastructure.MongoDBJsonStore;
import io.bugsbunny.preprocess.SecurityToken;
import io.bugsbunny.preprocess.SecurityTokenContainer;
import io.bugsbunny.util.JsonUtil;
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
        DataSet dataSet = null;
        try {
            String path = source.getPath();
            JsonArray dataSetArray = JsonParser.parseString(path).getAsJsonArray();
            SecurityToken securityToken = ((AIPlatformDataLakeSource) source).getSecurityToken();
            this.securityTokenContainer.setSecurityToken(securityToken);

            JsonUtil.print(AIPlatformDataLakeLoader.class,dataSetArray);
            for (int i=0; i<dataSetArray.size();i++){
                String id = dataSetArray.getAsString();
                JsonArray dataLakeArray = mongoDBJsonStore.getIngestion(this.securityTokenContainer.getTenant(), id);
                JsonUtil.print(AIPlatformDataLakeLoader.class,dataLakeArray);
            }

            /*long rows = 0l;
            long columns = 0l;
            Iterator<JsonElement> iterator = dataSetArray.iterator();
            StringBuilder csvData = new StringBuilder();
            while (iterator.hasNext()) {
                String id = iterator.next().getAsString();
                JsonArray dataLakeArray = mongoDBJsonStore.getIngestion(this.securityTokenContainer.getTenant(), id);
                for (int i =0; i< dataLakeArray.size(); i++){

                }
                String format = dataSetJson.get("format").getAsString();
                if(!format.equals("csv"))
                {
                    continue;
                }

                String data = dataSetJson.get("data").getAsString();

                if (dataSetJson.has("rows"))
                {
                    rows = dataSetJson.get("rows").getAsLong();
                }
                if (dataSetJson.has("columns"))
                {
                    columns = dataSetJson.get("columns").getAsLong();
                }

                csvData.append(data);
                if(iterator.hasNext())
                {
                    csvData.append("\n");
                }
            }

            if (rows > 0) {
                INDArray features = new NDArray(rows, columns);
                INDArray labels = new NDArray(rows, columns);
                dataSet = new DataSet(features, labels, null, null);
            } else {
                dataSet = new DataSet();
            }

            ///logger.info("********************************");
            //logger.info("ROWS: "+rows);
            //logger.info("COLUMNS: "+columns);
            //logger.info("********************************");

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            byteArrayOutputStream.writeBytes(csvData.toString().getBytes(StandardCharsets.UTF_8));
            dataSet.save(byteArrayOutputStream);
            }*/

            return dataSet;
        }
        catch(Exception e)
        {
            logger.error(e.getMessage(),e);
            return dataSet;
        }
    }
}
