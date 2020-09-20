//
package io.bugsbunny.dataScience.dl4j;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import io.bugsbunny.persistence.MongoDBJsonStore;

import org.apache.commons.io.IOUtils;

import org.nd4j.common.loader.Loader;
import org.nd4j.common.loader.Source;
import org.nd4j.linalg.dataset.DataSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class AIPlatformDataSetLoader implements Loader
{
    private static Logger logger = LoggerFactory.getLogger(AIPlatformDataSetLoader.class);

    @Override
    public Object load(Source source) throws IOException
    {
        //TODO: FINISH_IMPL
        /*MongoDBJsonStore mongoDBJsonStore = new MongoDBJsonStore();
        JsonObject dataSet = JsonParser.parseString(mongoDBJsonStore.readDataSet(93).toString()).getAsJsonObject();
        String data = dataSet.get("data").getAsString();

        logger.info("*************AIPlatformDataSetLoader**********************");
        logger.info(data);
        logger.info("**********************************************************");

        return data;*/

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        //String resource = "dataScience/saturn_data_train.csv";
        String resource = "dataScience/mnistTrain.bin";
        byteArrayOutputStream.writeBytes(IOUtils.resourceToByteArray(resource,
                Thread.currentThread().getContextClassLoader()));

        DataSet dataSet = new DataSet();
        dataSet.save(byteArrayOutputStream);
        return dataSet;
    }
}
