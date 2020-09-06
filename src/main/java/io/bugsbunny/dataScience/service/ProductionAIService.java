package io.bugsbunny.dataScience.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.bugsbunny.dataScience.model.PortableAIModel;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@ApplicationScoped
public class ProductionAIService
{
    private static Logger logger = LoggerFactory.getLogger(ProductionAIService.class);

    private PortableAIModel aiModel;

    public ProductionAIService()
    {
    }

    @PostConstruct
    public void start()
    {
        String runId = "04b0ecf158834908b1beebb1f28aca15";
        JsonObject model = this.getModel();
        //JsonObject modelJson = JsonParser.parseString(runJson).getAsJsonObject();
        //JsonObject data = modelJson.get("run").getAsJsonObject().get("data").getAsJsonObject();
        //String value = data.get("tags").getAsJsonArray().get(0).getAsJsonObject().get("value").getAsString();
        //JsonArray valueArray = JsonParser.parseString(value).getAsJsonArray();
        //String encodedModelString = valueArray.get(0).getAsJsonObject().get("modelSer").getAsString();
        String encodedModelString = model.get("modelSer").getAsString();
        this.aiModel = new PortableAIModel();
        this.aiModel.load(encodedModelString);
    }

    private JsonObject getModel()
    {
        ByteArrayOutputStream modelStream = null;
        ObjectOutputStream out = null;
        try
        {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("run", "blah");
            jsonObject.addProperty("data", "data");

            //this.testClassifier();

            //Process the Training Results
            //TODO: Delete this file once it is entered into the Repositories
            String model = IOUtils.toString(new FileInputStream("devModel/1/model.ser"),
                    StandardCharsets.UTF_8);

            //Register the Trained Model with the DataBricks Repository
            modelStream = new ByteArrayOutputStream();
            out = new ObjectOutputStream(modelStream);
            out.writeObject(model);

            jsonObject.addProperty("modelSer", Base64.getEncoder().encodeToString(modelStream.toByteArray()));

            return jsonObject;
        }
        catch(Exception e)
        {
            logger.info(e.getMessage(), e);
            throw new RuntimeException(e);
        }
        finally
        {
            if(out != null)
            {
                try
                {
                    out.close();
                }
                catch(IOException ioe)
                {
                    //Tried to cleanup..no biggie no biggie if still problemo time (lol)
                }
            }
            if(modelStream != null)
            {
                try
                {
                    modelStream.close();
                }
                catch(IOException ioe)
                {
                    //Tried to cleanup..no biggie no biggie if still problemo time (lol)
                }
            }
        }
    }

    @PreDestroy
    public void stop()
    {
        this.aiModel.unload();
    }

    public Double processLiveModelRequest(JsonObject json)
    {
        try
        {
            final double v = this.aiModel.calculate();
            return v;
        }
        catch (Exception e){
            logger.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }
}
