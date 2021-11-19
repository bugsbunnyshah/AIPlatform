package com.appgallabs.dataScience.function.model;

import com.appgallabs.util.JsonUtil;
import com.google.gson.JsonObject;
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
        InputLayer inputLayer = new InputLayer(Activation.RELU.name());
        OutputLayer outputLayer = new OutputLayer(Activation.SOFTMAX.name(),
                LossFunctions.LossFunction.SQUARED_LOSS.name());

        CreateModel function = new CreateModel(123,
                0.008,
                1,WeightInit.XAVIER.name(),
                new InputLayer[]{inputLayer},
                new OutputLayer[]{outputLayer}
                );
        function.momentum(0.9);
        function.outputs(2);
        function.hiddenNodes(20);
        JsonObject model = function.execute();
        JsonUtil.print(model);
        assertTrue(model.has("serializedModel"));
        assertNotNull(model.get("serializedModel").getAsString());
    }

    @Test
    public void requiredInputsOnly() throws Exception{
        InputLayer inputLayer = new InputLayer(Activation.RELU.name());
        OutputLayer outputLayer = new OutputLayer(Activation.SOFTMAX.name(),
                LossFunctions.LossFunction.SQUARED_LOSS.name());

        CreateModel function = new CreateModel(123,
                0.008,
                1,WeightInit.XAVIER.name(),
                new InputLayer[]{inputLayer},
                new OutputLayer[]{outputLayer}
        );
        //function.momentum(0.9);
        //function.outputs(2);
        //function.hiddenNodes(20);
        JsonObject model = function.execute();
        JsonUtil.print(model);
        assertTrue(model.has("serializedModel"));
        assertNotNull(model.get("serializedModel").getAsString());
    }

    @Test
    public void requiredInvalidData() throws Exception{
        InputLayer inputLayer = new InputLayer(Activation.RELU.name());
        OutputLayer outputLayer = new OutputLayer(Activation.SOFTMAX.name(),
                LossFunctions.LossFunction.SQUARED_LOSS.name());

        CreateModel function = new CreateModel(123,
                0.008,
                1,"BLAH_WEIGHT",
                new InputLayer[]{inputLayer},
                new OutputLayer[]{outputLayer}
        );

        boolean mustFail = false;
        try {
            function.execute();
        }
        catch(Exception e){
            mustFail = true;
        }
        assertTrue(mustFail);
    }
}
