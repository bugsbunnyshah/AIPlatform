package com.appgallabs.dataScience.codelabs;

import org.apache.log4j.BasicConfigurator;
import org.datavec.api.records.reader.RecordReader;
import org.datavec.api.records.reader.impl.csv.CSVRecordReader;
import org.datavec.api.split.FileSplit;
import org.deeplearning4j.datasets.datavec.RecordReaderDataSetIterator;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.eclipse.microprofile.context.ThreadContext;
import org.nd4j.common.io.ClassPathResource;
import org.nd4j.evaluation.classification.Evaluation;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.SplitTestAndTrain;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.dataset.api.preprocessor.DataNormalization;
import org.nd4j.linalg.dataset.api.preprocessor.NormalizerStandardize;
import org.nd4j.linalg.learning.config.Nesterovs;
import org.nd4j.linalg.lossfunctions.LossFunctions;

import java.io.File;

public class IrisClassification {

    private static final int FEATURES_COUNT = 4;
    private static final int CLASSES_COUNT = 3;

    public static void main(String[] args) {

        BasicConfigurator.configure();
        loadData();

    }

    private static void loadData() {
        try(RecordReader recordReader = new CSVRecordReader(0,',')) {
            recordReader.initialize(new FileSplit(
                    new File(Thread.currentThread().getContextClassLoader().getResource("dataScience/iris.csv").getFile())
            ));

            DataSetIterator iterator = new RecordReaderDataSetIterator(recordReader, 150, FEATURES_COUNT, CLASSES_COUNT);
            DataSet allData = iterator.next();
            allData.shuffle(123);

            DataNormalization normalizer = new NormalizerStandardize();
            normalizer.fit(allData);
            normalizer.transform(allData);

            SplitTestAndTrain testAndTrain = allData.splitTestAndTrain(0.65);
            DataSet trainingData = testAndTrain.getTrain();
            DataSet testingData = testAndTrain.getTest();

            irisNNetwork(trainingData, testingData);

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error: " + e.getLocalizedMessage());
        }
    }

    private static void irisNNetwork(DataSet trainingData, DataSet testData) {

        MultiLayerConfiguration configuration = new NeuralNetConfiguration.Builder()
                .activation(Activation.TANH)
                .weightInit(WeightInit.XAVIER)
                .updater(new Nesterovs(0.1, 0.9))
                .l2(0.0001)
                .list()
                .layer(0, new DenseLayer.Builder().nIn(FEATURES_COUNT).nOut(3).build())
                .layer(1, new DenseLayer.Builder().nIn(3).nOut(3).build())
                .layer(2, new OutputLayer.Builder(
                        LossFunctions.LossFunction.NEGATIVELOGLIKELIHOOD).activation(Activation.SOFTMAX)
                        .nIn(3).nOut(CLASSES_COUNT).build())
                .build();

        MultiLayerNetwork model = new MultiLayerNetwork(configuration);
        model.init();
        model.fit(trainingData);

        /*INDArray output = model.output(testData.iterateWithMiniBatches());
        Evaluation eval = new Evaluation(3);
        eval.eval(testData.getLabels(), output);
        System.out.println(eval.stats());*/
        Evaluation evaluation = model.evaluate(testData.iterateWithMiniBatches());
        System.out.println(evaluation.stats());
    }
}
