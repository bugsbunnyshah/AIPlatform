package io.bugsbunny.datascience.service;

import org.junit.jupiter.api.Test;
import org.nd4j.evaluation.classification.Evaluation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import org.deeplearning4j.datasets.iterator.impl.MnistDataSetIterator;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.inputs.InputType;
import org.deeplearning4j.nn.conf.layers.CenterLossOutputLayer;
import org.deeplearning4j.nn.conf.layers.ConvolutionLayer;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.SubsamplingLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.deeplearning4j.util.ModelSerializer;
import org.nd4j.common.primitives.Pair;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.learning.config.Adam;
import org.nd4j.linalg.lossfunctions.LossFunctions;

public class TrainingWorkflowTests {
    private static Logger log = LoggerFactory.getLogger(TrainingWorkflowTests.class);

    @Test
    public void testStartTraining() throws Exception
    {
        int outputNum = 10; // The number of possible outcomes
        int batchSize = 64; // Test batch size
        int nEpochs = 1;   // Number of training epochs
        int seed = 123;

        //Lambda defines the relative strength of the center loss component.
        //lambda = 0.0 is equivalent to training with standard softmax only
        double lambda = 1.0;

        //Alpha can be thought of as the learning rate for the centers for each class
        double alpha = 0.1;

        log.info("Load data....");
        DataSetIterator mnistTrain = new MnistDataSetIterator(batchSize, true, 12345);
        DataSetIterator mnistTest = new MnistDataSetIterator(10000, false, 12345);

        log.info("Build model....");
        MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
                .seed(seed)
                .l2(0.0005)
                .activation(Activation.LEAKYRELU)
                .weightInit(WeightInit.RELU)
                .updater(new Adam(0.01))
                .list()
                .layer(new ConvolutionLayer.Builder(5, 5).stride(1, 1).nOut(32).activation(Activation.LEAKYRELU).build())
                .layer(new SubsamplingLayer.Builder(SubsamplingLayer.PoolingType.MAX).kernelSize(2, 2).stride(2, 2).build())
                .layer(new ConvolutionLayer.Builder(5, 5).stride(1, 1).nOut(64).build())
                .layer(new SubsamplingLayer.Builder(SubsamplingLayer.PoolingType.MAX).kernelSize(2, 2).stride(2, 2).build())
                .layer(new DenseLayer.Builder().nOut(256).build())
                //Layer 5 is our embedding layer: 2 dimensions, just so we can plot it on X/Y grid. Usually use more in practice
                .layer(new DenseLayer.Builder().activation(Activation.IDENTITY).weightInit(WeightInit.XAVIER).nOut(2)
                        //Larger L2 value on the embedding layer: can help to stop the embedding layer weights
                        // (and hence activations) from getting too large. This is especially problematic with small values of
                        // lambda such as 0.0
                        .l2(0.1).build())
                .layer(new CenterLossOutputLayer.Builder(LossFunctions.LossFunction.NEGATIVELOGLIKELIHOOD)
                        .nIn(2).nOut(outputNum)
                        .weightInit(WeightInit.XAVIER).activation(Activation.SOFTMAX)
                        //Alpha and lambda hyperparameters are specific to center loss model: see comments above and paper
                        .alpha(alpha).lambda(lambda)
                        .build())
                .setInputType(InputType.convolutionalFlat(28, 28, 1))
                .build();

        MultiLayerNetwork orig = new MultiLayerNetwork(conf);
        orig.init();

        log.info("Train model....");
        Evaluation evaluation = orig.evaluate(mnistTest);
        log.info(evaluation.stats());
        //orig.setListeners(new ScoreIterationListener(100));
        //List<Pair<INDArray, INDArray>> embeddingByEpoch = new ArrayList<>();
        //List<Integer> epochNum = new ArrayList<>();

        //DataSet testData = mnistTest.next();
        //orig.fit(mnistTrain);
        //log.info("*** Completed epoch {} ***", i);
        //Feed forward to the embedding layer (layer 5) to get the 2d embedding to plot later
        //INDArray embedding = orig.feedForwardToLayer(5, testData.getFeatures()).get(6);
        //embeddingByEpoch.add(new Pair<>(embedding, testData.getLabels()));
        //epochNum.add(i);
        /*for (int i = 0; i < nEpochs; i++) {
            orig.fit(mnistTrain);
            log.info("*** Completed epoch {} ***", i);

            //Feed forward to the embedding layer (layer 5) to get the 2d embedding to plot later
            INDArray embedding = orig.feedForwardToLayer(5, testData.getFeatures()).get(6);

            embeddingByEpoch.add(new Pair<>(embedding, testData.getLabels()));
            epochNum.add(i);
        }*/

        ByteArrayOutputStream modelStream = new ByteArrayOutputStream();
        ModelSerializer.writeModel(orig, modelStream, true);
        String modelSer = Base64.getEncoder().encodeToString(modelStream.toByteArray());
        //log.info(modelSer);

        ByteArrayInputStream restoreStream = new ByteArrayInputStream(Base64.getDecoder().decode(modelSer));
        MultiLayerNetwork restored = ModelSerializer.restoreMultiLayerNetwork(restoreStream, true);
        //restored.init();

        evaluation = restored.evaluate(mnistTest);
        log.info(evaluation.toJson());
    }
}
