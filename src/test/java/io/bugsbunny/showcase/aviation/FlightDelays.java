/*******************************************************************************
 * Copyright (c) 2020 Konduit K.K.
 * Copyright (c) 2015-2019 Skymind, Inc.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Apache License, Version 2.0 which is available at
 * https://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 ******************************************************************************/

package io.bugsbunny.showcase.aviation;

import com.github.wnameless.json.flattener.JsonFlattener;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.bugsbunny.dataIngestion.util.CSVDataUtil;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;


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
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.nd4j.evaluation.classification.Evaluation;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.learning.config.Nesterovs;
import org.nd4j.linalg.lossfunctions.LossFunctions.LossFunction;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Example: training an embedding using the center loss model, on MNIST
 * The motivation is to use the class labels to learn embeddings that have the following properties:
 * (a) Intra-class similarity (i.e., similar vectors for same numbers)
 * (b) Inter-class dissimilarity (i.e., different vectors for different numbers)
 *
 * Refer to the paper "A Discriminative Feature Learning Approach for Deep Face Recognition", Wen et al. (2016)
 * http://ydwen.github.io/papers/WenECCV16.pdf
 *
 * This
 *
 * @author Alex Black
 */
public class FlightDelays {
    private static final Logger logger = LoggerFactory.getLogger(FlightDelays.class);

    public static void main(String[] args) throws Exception {
        int outputNum = 10; // The number of possible outcomes
        int batchSize = 10; // Test batch size
        int nEpochs = 2;   // Number of training epochs
        int seed = 123;

        //Lambda defines the relative strength of the center loss component.
        //lambda = 0.0 is equivalent to training with standard softmax only
        double lambda = 1.0;

        //Alpha can be thought of as the learning rate for the centers for each class
        double alpha = 0.1;

        double learningRate = 0.01;

        int numInputs = 3;
        int numOutputs = 3;
        int numHiddenNodes = 20;

        /*logger.info("Load data....");
        String data = IOUtils.toString(Thread.currentThread().getContextClassLoader().getResourceAsStream("aviation/flights0.json"),
            StandardCharsets.UTF_8);

        JsonObject ingestedData = JsonParser.parseString(data).getAsJsonObject();
        JsonArray dataArray = ingestedData.getAsJsonArray("data");
        Iterator<JsonElement> itr = dataArray.iterator();
        JsonArray csvArray = new JsonArray();
        int counter = 0;
        StringBuilder header = new StringBuilder();
        while(itr.hasNext())
        {
            JsonObject cour = itr.next().getAsJsonObject();
            Map<String, Object> flatObject = JsonFlattener.flattenAsMap(cour.toString());
            if(counter == 0) {
                Set<Map.Entry<String, Object>> entrySet = flatObject.entrySet();
                int size = entrySet.size();
                int keyCounter = 0;
                for (Map.Entry<String, Object> local : entrySet) {
                    String key = local.getKey();
                    logger.info("Key="+key);
                    header.append(key);
                    if(keyCounter < size-1)
                    {
                        header.append(",");
                    }
                    keyCounter++;
                }
                counter++;
            }
            JsonObject flatJson = JsonParser.parseString(flatObject.toString()).getAsJsonObject();
            //logger.info(flatJson.toString());
            csvArray.add(flatJson);
        }

        CSVDataUtil csvDataUtil = new CSVDataUtil();
        JsonObject csvData = csvDataUtil.convert(csvArray);
        logger.info(csvData.toString());

        String csv = csvData.get("data").getAsString();
        csv = header.toString() + "\n" + csv;
        FileUtils.write(new File("flightDelays.csv"), csv, StandardCharsets.UTF_8);

        //logger.info(JsonFlattener.flattenAsMap(data).toString());*/


        /*String[] columns = data.split(",");
        for(String column:columns)
        {
            logger.info(column);
        }*/

        String trainData = IOUtils.toString(Thread.currentThread().getContextClassLoader().getResourceAsStream("aviation/flight_delay_train.csv"),
                StandardCharsets.UTF_8);
        String[] dataRow = trainData.split("\n");
        StringBuilder curatedTrainDataSet = new StringBuilder();
        for(int j=0; j<dataRow.length; j++)
        {
            StringBuilder curatedRow = new StringBuilder();
            String[] columns = dataRow[j].split(",");
            for(int i=0; i<columns.length; i++)
            {
                String cour = columns[i];

                OffsetDateTime offsetDateTime = OffsetDateTime.parse(cour);
                long epochSecond = offsetDateTime.toEpochSecond();
                curatedRow.append(epochSecond);
                if(i < columns.length-1)
                {
                    curatedRow.append(",");
                }
            }
            curatedTrainDataSet.append(curatedRow.toString());
            if(j < dataRow.length-1)
            {
                curatedTrainDataSet.append("\n");
            }
        }
        FileUtils.write(new File("curated_training_data.csv"), curatedTrainDataSet, StandardCharsets.UTF_8);

        RecordReader rrTrain = new CSVRecordReader();
        File trainFile = new File("/Users/babyboy/mamasboy/appgallabs/braineous/secretariat/AIPlatform/curated_training_data.csv");
        rrTrain.initialize(new FileSplit(trainFile));
        DataSetIterator trainIter = new RecordReaderDataSetIterator(rrTrain, batchSize);
        logger.info(trainIter.inputColumns()+"");

        //Load the test/evaluation data:
        RecordReader rrTest = new CSVRecordReader();
        File testFile = new File("/Users/babyboy/mamasboy/appgallabs/braineous/secretariat/AIPlatform/curated_training_data.csv");
        rrTest.initialize(new FileSplit(testFile));
        DataSetIterator testIter = new RecordReaderDataSetIterator(rrTest, batchSize);
        logger.info(testIter.inputColumns()+"");

        logger.info("Build model....");
        MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
                .seed(seed)
                .weightInit(WeightInit.XAVIER)
                .updater(new Nesterovs(learningRate, 0.9))
                .list()
                .layer(new DenseLayer.Builder().nIn(numInputs).nOut(numHiddenNodes)
                        .activation(Activation.RELU)
                        .build())
                .layer(new OutputLayer.Builder(LossFunction.NEGATIVELOGLIKELIHOOD)
                        .activation(Activation.SOFTMAX)
                        .nIn(numHiddenNodes).nOut(numOutputs).build())
                .build();


        MultiLayerNetwork model = new MultiLayerNetwork(conf);
        model.init();
        model.setListeners(new ScoreIterationListener(10));  //Print score every 10 parameter updates
        model.fit(trainIter, nEpochs);

        System.out.println("Evaluate model....");
        Evaluation eval = new Evaluation(numOutputs);
        while (testIter.hasNext()) {
            logger.info("TESTITR....");
            DataSet t = testIter.next();
            INDArray features = t.getFeatures();
            INDArray labels = t.getLabels();
            INDArray predicted = model.output(features, false);
            eval.eval(labels, predicted);
        }
        //An alternate way to do the above loop
        //Evaluation evalResults = model.evaluate(testIter);

        //Print the evaluation statistics
        System.out.println(eval.stats());
    }

    /*public static void generateVisuals(MultiLayerNetwork model, DataSetIterator trainIter, DataSetIterator testIter) throws Exception {
        double xMin = 0;
        double xMax = 1.0;
        double yMin = -0.2;
        double yMax = 0.8;
        int nPointsPerAxis = 100;

        //Generate x,y points that span the whole range of features
        INDArray allXYPoints = PlotUtil.generatePointsOnGraph(xMin, xMax, yMin, yMax, nPointsPerAxis);
        //Get train data and plot with predictions
        PlotUtil.plotTrainingData(model, trainIter, allXYPoints, nPointsPerAxis);
        TimeUnit.SECONDS.sleep(3);
        //Get test data, run the test data through the network to generate predictions, and plot those predictions:
        PlotUtil.plotTestData(model, testIter, allXYPoints, nPointsPerAxis);
    }*/
}
