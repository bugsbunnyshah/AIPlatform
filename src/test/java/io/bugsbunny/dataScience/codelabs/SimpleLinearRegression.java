package io.bugsbunny.dataScience.codelabs;

import io.bugsbunny.dataScience.service.ResettableStreamSplit;
import org.apache.commons.io.IOUtils;
import org.apache.hadoop.yarn.webapp.hamlet.Hamlet;
import org.datavec.api.records.reader.RecordReader;
import org.datavec.api.records.reader.impl.csv.CSVRecordReader;
import org.deeplearning4j.datasets.datavec.RecordReaderDataSetIterator;
import org.deeplearning4j.datasets.iterator.INDArrayDataSetIterator;
import org.deeplearning4j.nn.conf.ComputationGraphConfiguration;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.layers.CnnLossLayer;
import org.deeplearning4j.nn.conf.layers.ConvolutionLayer;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.graph.ComputationGraph;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.junit.jupiter.api.Test;
import org.nd4j.common.primitives.Pair;
import org.nd4j.evaluation.classification.Evaluation;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.learning.config.Nesterovs;
import org.nd4j.linalg.learning.config.Sgd;
import org.nd4j.linalg.lossfunctions.LossFunctions;
import org.nd4j.linalg.lossfunctions.impl.LossMCXENT;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import java.nio.charset.StandardCharsets;
import java.util.*;

public class SimpleLinearRegression {
    private static Logger logger = LoggerFactory.getLogger(SimpleLinearRegression.class);

    @Test
    public void syntheticData() throws Exception{
        int seed = 123;
        double learningRate = 0.008;
        double momentum = 0.9;
        int numInputs = 1;
        int numOutputs = 2;
        int numHiddenNodes = 20;
        MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
                .seed(seed)
                .weightInit(WeightInit.XAVIER)
                .updater(new Nesterovs(learningRate, momentum))
                .list()
                .layer(new DenseLayer.Builder().nIn(numInputs).nOut(numHiddenNodes)
                        .activation(Activation.RELU)
                        .build())
                .layer(new OutputLayer.Builder(LossFunctions.LossFunction.SQUARED_LOSS)
                        .activation(Activation.SOFTMAX)
                        .nIn(numHiddenNodes).nOut(numOutputs).build())
                .build();
        MultiLayerNetwork network = new MultiLayerNetwork(conf);
        network.init();
        network.setListeners(new ScoreIterationListener(10));

        String storedData = IOUtils.resourceToString("dataScience/syntheticData.csv", StandardCharsets.UTF_8,
                Thread.currentThread().getContextClassLoader());
        ResettableStreamSplit inputStreamSplit = new ResettableStreamSplit(
                storedData);

        //This should be a parameter
        int batchSize = storedData.length();
        int nEpochs = 30;
        int labelIndex = 0;
        int possibleLabels = numInputs+1;
        RecordReader rrTrain = new CSVRecordReader();
        rrTrain.initialize(inputStreamSplit);
        DataSetIterator trainIter = new RecordReaderDataSetIterator(rrTrain,
                batchSize, labelIndex, possibleLabels);

        network.fit(trainIter,nEpochs);

        Evaluation evaluation = network.evaluate(trainIter);
        System.out.println(evaluation);
    }
}
