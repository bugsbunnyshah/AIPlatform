package com.appgallabs.dataScience.function.model;

public class OutputLayer {
    private String activation;
    private String lossFunction;

    public OutputLayer(String activation, String lossFunction) {
        this.activation = activation;
        this.lossFunction = lossFunction;
    }

    public String getActivation() {
        return activation;
    }

    public String getLossFunction() {
        return lossFunction;
    }
}
