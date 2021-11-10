package io.bugsbunny.dataScience.function.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.util.ModelSerializer;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.learning.config.Nesterovs;
import org.nd4j.linalg.lossfunctions.LossFunctions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.util.Base64;

public class CreateModel {
    private static Logger logger = LoggerFactory.getLogger(CreateModel.class);

    public JsonObject execute(JsonObject configuration){
        try {
            JsonObject json = new JsonObject();

            int seed = configuration.get("seed").getAsInt();
            double learningRate = configuration.get("learningRate").getAsDouble();
            double momentum = 0.9;
            if(configuration.has("momentum")){
                momentum = configuration.get("momentum").getAsDouble();
            }
            int numInputs = configuration.get("inputs").getAsInt();
            int numOutputs = numInputs+1;
            if(configuration.has("outputs")){
                numOutputs = configuration.get("outputs").getAsInt();
            }
            int numHiddenNodes = 20; //TODO: decide the default_value
            if(configuration.has("hiddenNodes")){
                numHiddenNodes = configuration.get("hiddenNodes").getAsInt();
            }

            String weightInit = configuration.get("weightInit").getAsString();

            NeuralNetConfiguration.ListBuilder listBuilder = new NeuralNetConfiguration.Builder()
                    .seed(seed)
                    .weightInit(this.getWeightInit(weightInit))
                    .updater(new Nesterovs(learningRate, momentum))
                    .list();

            JsonArray inputLayers = configuration.get("inputLayers").getAsJsonArray();
            for(int i=0; i<inputLayers.size();i++){
                JsonObject cour = inputLayers.get(i).getAsJsonObject();
                String activation = cour.get("activation").getAsString();
                DenseLayer denseLayer = new DenseLayer.Builder().nIn(numInputs).nOut(numHiddenNodes)
                        .activation(this.getActivation(activation))
                        .build();
                listBuilder.layer(denseLayer);
            }

            JsonArray outputLayers = configuration.get("outputLayers").getAsJsonArray();
            for(int i=0; i<outputLayers.size();i++){
                JsonObject cour = outputLayers.get(i).getAsJsonObject();
                String activation = cour.get("activation").getAsString();
                String lossFunction = cour.get("lossFunction").getAsString();
                OutputLayer outputLayer = new OutputLayer.Builder(this.getLossFunction(lossFunction))
                        .activation(this.getActivation(activation))
                        .nIn(numHiddenNodes).nOut(numOutputs).build();
                listBuilder.layer(outputLayer);
            }

            MultiLayerConfiguration conf = listBuilder.build();
            MultiLayerNetwork network = new MultiLayerNetwork(conf);
            network.init();

            ByteArrayOutputStream modelBytes = new ByteArrayOutputStream();
            ModelSerializer.writeModel(network, modelBytes, false);
            String modelString = Base64.getEncoder().encodeToString(modelBytes.toByteArray());

            json.addProperty("serializedModel",modelString);

            return json;
        }
        catch(Exception e){
            throw new RuntimeException(e);
        }
    }

    private WeightInit getWeightInit(String weightInit){
        WeightInit[] values =  WeightInit.values();
        for(WeightInit value:values){
            if(value.name().equalsIgnoreCase(weightInit.toLowerCase())){
                return value;
            }
        }
        return null;
    }

    private Activation getActivation(String activation){
        Activation[] values =  Activation.values();
        for(Activation value:values){
            if(value.name().equalsIgnoreCase(activation.toLowerCase())){
                return value;
            }
        }
        return null;
    }

    private LossFunctions.LossFunction getLossFunction(String lossFunction){
        LossFunctions.LossFunction[] values =  LossFunctions.LossFunction.values();
        for(LossFunctions.LossFunction value:values){
            if(value.name().equalsIgnoreCase(lossFunction.toLowerCase())){
                return value;
            }
        }
        return null;
    }
}
