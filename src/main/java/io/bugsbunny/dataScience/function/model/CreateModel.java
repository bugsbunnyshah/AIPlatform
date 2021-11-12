package io.bugsbunny.dataScience.function.model;

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

    private int seed;
    private double learningRate;
    private double momentum = 0.9;
    private int inputs;
    private int outputs;
    private int hiddenNodes = 20;
    private String weight;
    private InputLayer[] inputLayers;
    private io.bugsbunny.dataScience.function.model.OutputLayer[] outputLayers;

    public CreateModel(int seed,double learningRate,int inputs,String weight,
                            InputLayer[] inputLayers,
                            io.bugsbunny.dataScience.function.model.OutputLayer[] outputLayers)
    {
        this.seed = seed;
        this.learningRate = learningRate;
        this.inputs = inputs;
        this.outputs = this.inputs+1;
        this.weight = weight;
        this.inputLayers = inputLayers;
        this.outputLayers = outputLayers;
    }

    public void momentum(double momentum){
        this.momentum = momentum;
    }

    public void hiddenNodes(int hiddenNodes){
        this.hiddenNodes = hiddenNodes;
    }

    public void outputs(int outputs){
        this.outputs = outputs;
    }


    public JsonObject execute(){
        try {
            JsonObject json = new JsonObject();

            NeuralNetConfiguration.ListBuilder listBuilder = new NeuralNetConfiguration.Builder()
                    .seed(this.seed)
                    .weightInit(this.getWeightInit(this.weight))
                    .updater(new Nesterovs(this.learningRate, this.momentum))
                    .list();

            for(int i=0; i<this.inputLayers.length;i++){
                String activation = this.inputLayers[i].getActivation();
                DenseLayer denseLayer = new DenseLayer.Builder().nIn(this.inputs).
                        nOut(this.hiddenNodes)
                        .activation(this.getActivation(activation))
                        .build();
                listBuilder.layer(denseLayer);
            }

            for(int i=0; i<this.outputLayers.length;i++){
                String activation = this.outputLayers[i].getActivation();
                String lossFunction = this.outputLayers[i].getLossFunction();
                OutputLayer outputLayer = new OutputLayer.Builder(this.getLossFunction(lossFunction))
                        .activation(this.getActivation(activation))
                        .nIn(this.hiddenNodes).nOut(this.outputs).build();
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
