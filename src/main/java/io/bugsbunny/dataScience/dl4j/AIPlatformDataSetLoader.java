package io.bugsbunny.dataScience.dl4j;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import io.bugsbunny.endpoint.SecurityToken;
import io.bugsbunny.endpoint.SecurityTokenContainer;
import io.bugsbunny.persistence.MongoDBJsonStore;

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

/*
String resource = "dataScience/saturn_data_train.csv";
String resource = "dataScience/mnistTrain.bin";
*/

@ApplicationScoped
public class AIPlatformDataSetLoader implements Loader
{
    private static Logger logger = LoggerFactory.getLogger(AIPlatformDataSetLoader.class);

    @Inject
    private MongoDBJsonStore mongoDBJsonStore;

    @Inject
    private SecurityTokenContainer securityTokenContainer;

    @Override
    public Object load(Source source) throws IOException
    {
        DataSet dataSet = null;
        try
        {
            String path = source.getPath();
            long dataSetId = Long.parseLong(path);
            SecurityToken securityToken = ((AIPlatformDataSetSource) source).getSecurityToken();
            this.securityTokenContainer.setSecurityToken(securityToken);


            JsonObject dataSetJson = JsonParser.parseString(mongoDBJsonStore.readDataSet(dataSetId).toString()).getAsJsonObject();

            String data = dataSetJson.get("data").getAsString();
            long rows = 0l;
            if(dataSetJson.has("rows"))
            {
                rows = dataSetJson.get("rows").getAsLong();
            }
            long columns = 0l;
            if(dataSetJson.has("columns"))
            {
                columns = dataSetJson.get("columns").getAsLong();
            }

            if(rows > 0) {
                INDArray features = new NDArray(rows, columns);
                INDArray labels = new NDArray(rows, columns);
                dataSet = new DataSet(features, labels, null, null);
            }
            else
            {
                dataSet = new DataSet();
            }

            logger.info("********************************");
            logger.info("ROWS: "+rows);
            logger.info("COLUMNS: "+columns);
            logger.info("********************************");

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            byteArrayOutputStream.writeBytes(data.getBytes(StandardCharsets.UTF_8));
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
