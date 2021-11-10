package io.bugsbunny.dataScience.function.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.bugsbunny.util.JsonUtil;
import io.quarkus.test.junit.QuarkusTest;
import org.deeplearning4j.nn.weights.WeightInit;
import org.junit.jupiter.api.Test;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.lossfunctions.LossFunctions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
public class CreateModelTests {
    private static Logger logger = LoggerFactory.getLogger(CreateModelTests.class);

    @Test
    public void allInputs() throws Exception{
        CreateModel createModel = new CreateModel();

        JsonObject json = new JsonObject();
        json.addProperty("seed",123);
        json.addProperty("learningRate",0.008);
        json.addProperty("momentum",0.9);
        json.addProperty("inputs",1);
        json.addProperty("outputs",2);
        json.addProperty("hiddenNodes",20);
        json.addProperty("weightInit", WeightInit.XAVIER.name());

        JsonArray layers = new JsonArray();
        for(int i=0; i<1; i++) {
            JsonObject layer = new JsonObject();
            layer.addProperty("activation", Activation.RELU.name());
            layers.add(layer);
        }
        json.add("inputLayers",layers);

        layers = new JsonArray();
        for(int i=0; i<1; i++) {
            JsonObject layer = new JsonObject();
            layer.addProperty("activation", Activation.SOFTMAX.name());
            layer.addProperty("lossFunction", LossFunctions.LossFunction.SQUARED_LOSS.name());
            layers.add(layer);
        }
        json.add("outputLayers",layers);

        JsonObject model = createModel.execute(json);
        JsonUtil.print(model);
        assertTrue(model.has("serializedModel"));
        assertNotNull(model.get("serializedModel").getAsString());
    }

    @Test
    public void requiredInputsOnly() throws Exception{
        CreateModel createModel = new CreateModel();

        JsonObject json = new JsonObject();
        json.addProperty("seed",123);
        json.addProperty("learningRate",0.008);
        json.addProperty("inputs",1);
        json.addProperty("weightInit", WeightInit.XAVIER.name());
        //json.addProperty("momentum",0.9);
        //json.addProperty("outputs",2);
        //json.addProperty("hiddenNodes",20);

        JsonArray layers = new JsonArray();
        for(int i=0; i<1; i++) {
            JsonObject layer = new JsonObject();
            layer.addProperty("activation", Activation.RELU.name());
            layers.add(layer);
        }
        json.add("inputLayers",layers);

        layers = new JsonArray();
        for(int i=0; i<1; i++) {
            JsonObject layer = new JsonObject();
            layer.addProperty("activation", Activation.SOFTMAX.name());
            layer.addProperty("lossFunction", LossFunctions.LossFunction.SQUARED_LOSS.name());
            layers.add(layer);
        }
        json.add("outputLayers",layers);

        JsonObject model = createModel.execute(json);
        JsonUtil.print(model);
        assertTrue(model.has("serializedModel"));
        assertNotNull(model.get("serializedModel").getAsString());
    }

    @Test
    public void requiredInvalidData() throws Exception{
        CreateModel createModel = new CreateModel();

        JsonObject json = new JsonObject();
        json.addProperty("seed",123);
        json.addProperty("learningRate",0.008);
        json.addProperty("inputs",1);
        json.addProperty("weightInit", "blah"); //Invalid
        //json.addProperty("momentum",0.9);
        //json.addProperty("outputs",2);
        //json.addProperty("hiddenNodes",20);

        JsonArray layers = new JsonArray();
        for(int i=0; i<1; i++) {
            JsonObject layer = new JsonObject();
            layer.addProperty("activation", Activation.RELU.name());
            layers.add(layer);
        }
        json.add("inputLayers",layers);

        layers = new JsonArray();
        for(int i=0; i<1; i++) {
            JsonObject layer = new JsonObject();
            layer.addProperty("activation", Activation.SOFTMAX.name());
            layer.addProperty("lossFunction", LossFunctions.LossFunction.SQUARED_LOSS.name());
            layers.add(layer);
        }
        json.add("outputLayers",layers);

        boolean mustFail = false;
        try {
            createModel.execute(json);
        }
        catch(Exception e){
            mustFail = true;
        }
        assertTrue(mustFail);
    }
}
