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

            String id = dataSetArray.get(0).getAsString();
            JsonArray dataLakeArray = mongoDBJsonStore.getIngestion(this.securityTokenContainer.getTenant(), id);

            JsonObject dataLakeObject = dataLakeArray.get(0).getAsJsonObject();
            String csvData = dataLakeObject.get("data").getAsString();
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
