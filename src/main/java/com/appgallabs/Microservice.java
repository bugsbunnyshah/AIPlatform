package com.appgallabs;

import com.appgallabs.configuration.AIPlatformConfig;
import com.appgallabs.dataScience.endpoint.TrainModel;
import com.appgallabs.infrastructure.PythonEnvironment;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import jep.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestBody;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.util.*;

@Path("/microservice")
public class Microservice
{
    private static Logger logger = LoggerFactory.getLogger(Microservice.class);

    @Inject
    private AIPlatformConfig aiPlatformConfig;

    @Inject
    private PythonEnvironment pythonEnvironment;

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Response microservice(@RequestBody String json)
    {
        try {
            JsonObject jsonObject = JsonParser.parseString(json).getAsJsonObject();
            logger.info(jsonObject.toString());

            jsonObject.addProperty("product", "braineous");
            jsonObject.addProperty("oid", UUID.randomUUID().toString());
            jsonObject.addProperty("message", "HELLO_TO_HUMANITY");

            return Response.ok(jsonObject.toString()).build();
        }
        catch(Exception e)
        {
            logger.error(e.getMessage(), e);
            JsonObject error = new JsonObject();
            error.addProperty("exception", e.getMessage());
            return Response.status(500).entity(error.toString()).build();
        }
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response microserviceGet()
    {
        try {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("product", "braineous");
            jsonObject.addProperty("oid", UUID.randomUUID().toString());
            jsonObject.addProperty("message", "HELLO_TO_HUMANITY");

            String pythonScript = "# Boiler plate stuff to start the module\n" +
                    "import jpype\n" +
                    "import jpype.imports\n" +
                    "from jpype.types import *\n" +
                    "import pandas as pd\n" +
                    "from java.util import Random\n" +
                    "from com.appgallabs.dataScience.function.model import CreateModel\n" +
                    "from com.appgallabs.dataScience.function.model import InputLayer\n" +
                    "from com.appgallabs.dataScience.function.model import OutputLayer\n" +
                    "from org.nd4j.linalg.activations import Activation\n" +
                    "from org.nd4j.linalg.lossfunctions import LossFunctions\n" +
                    "from org.deeplearning4j.nn.weights import WeightInit\n" +
                    "\n" +
                    "df = pd.DataFrame()\n" +
                    "\n" +
                    "inputLayer = InputLayer(Activation.RELU.name());\n" +
                    "outputLayer = OutputLayer(Activation.SOFTMAX.name(),\n" +
                    "                          LossFunctions.LossFunction.SQUARED_LOSS.name());\n" +
                    "inputLayers = [inputLayer]\n" +
                    "\n" +
                    "outputLayers = [outputLayer]\n" +
                    "\n" +
                    "cloudMlFunction = CreateModel(123,\n" +
                    "                              0.008,\n" +
                    "                              1,WeightInit.XAVIER.name(),\n" +
                    "                              inputLayers,\n" +
                    "                              outputLayers\n" +
                    "                              )\n" +
                    "model = cloudMlFunction.execute()";

            this.pythonEnvironment.executeScript(pythonScript);

            return Response.ok(jsonObject.toString()).build();
        }
        catch(Exception e)
        {
            logger.error(e.getMessage(), e);
            JsonObject error = new JsonObject();
            error.addProperty("exception", e.getMessage());
            return Response.status(500).entity(error.toString()).build();
        }
    }
}