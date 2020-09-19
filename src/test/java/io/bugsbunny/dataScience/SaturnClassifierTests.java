/* *****************************************************************************
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

package io.bugsbunny.dataScience;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.bugsbunny.dataIngestion.util.CSVDataUtil;
import io.bugsbunny.endpoint.SecurityToken;
import io.bugsbunny.endpoint.SecurityTokenContainer;
import io.bugsbunny.persistence.MongoDBJsonStore;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.Response;
import org.apache.commons.io.IOUtils;
import org.datavec.api.records.reader.RecordReader;
import org.datavec.api.records.reader.impl.csv.CSVRecordReader;
import org.datavec.api.split.FileSplit;
import org.datavec.api.split.InputStreamInputSplit;
import org.deeplearning4j.datasets.datavec.RecordReaderDataSetIterator;
import io.bugsbunny.dataScience.utils.DownloaderUtility;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.nd4j.evaluation.classification.Evaluation;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.learning.config.Nesterovs;
import org.nd4j.linalg.lossfunctions.LossFunctions.LossFunction;
import org.datavec.api.split.StringSplit;

import javax.inject.Inject;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

import static io.restassured.RestAssured.given;

/**
 * "Saturn" Data Classification Example
 *
 * Based on the data from Jason Baldridge:
 * 	https://github.com/jasonbaldridge/try-tf/tree/master/simdata
 *
 * @author Josh Patterson
 * @author Alex Black (added plots)
 *
 */
@QuarkusTest
public class SaturnClassifierTests {

    public static String dataLocalPath;
    public static boolean visualize = true;

    @Inject
    private CSVDataUtil csvDataUtil;

    @Inject
    private MongoDBJsonStore mongoDBJsonStore;

    @Inject
    private SecurityTokenContainer securityTokenContainer;

    @BeforeEach
    public void setUp() throws Exception
    {
        String securityTokenJson = IOUtils.toString(Thread.currentThread().getContextClassLoader().
                        getResourceAsStream("oauthAgent/token.json"),
                StandardCharsets.UTF_8);
        SecurityToken securityToken = SecurityToken.fromJson(securityTokenJson);
        this.securityTokenContainer.getTokenContainer().set(securityToken);
    }

    @Test
    public void testModel() throws Exception {
        int batchSize = 50;
        int seed = 123;
        double learningRate = 0.005;
        //Number of epochs (full passes of the data)
        int nEpochs = 30;

        int numInputs = 2;
        int numOutputs = 2;
        int numHiddenNodes = 20;

        dataLocalPath = DownloaderUtility.CLASSIFICATIONDATA.Download();
        //Load the training data:
        RecordReader rr = new CSVRecordReader();
        rr.initialize(new FileSplit(new File(dataLocalPath, "saturn_data_train.csv")));
        DataSetIterator trainIter = new RecordReaderDataSetIterator(rr, batchSize, 0, 2);

        //final File file = new File(dataLocalPath, "saturn_data_eval.csv");
        //String csvFile = IOUtils.toString(new FileInputStream(file), StandardCharsets.UTF_8);

        //Load the test/evaluation data:
        Response response = given().get("/dataset/readForEval").andReturn();
        JsonObject dataSet = JsonParser.parseString(response.body().asString()).getAsJsonObject();
        String csvData = dataSet.get("data").getAsString();
        RecordReader rrTest = new CSVRecordReader();
        InputStreamInputSplit inputStreamInputSplit = new InputStreamInputSplit(new ByteArrayInputStream(
                csvData.getBytes(StandardCharsets.UTF_8)));
        rrTest.initialize(inputStreamInputSplit);
        //rrTest.initialize(new FileSplit(new File(dataLocalPath, "saturn_data_eval.csv")));
        DataSetIterator testIter = new RecordReaderDataSetIterator(rrTest, batchSize, 0, 2);

        //log.info("Build model....");
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
        model.setListeners(new ScoreIterationListener(10));    //Print score every 10 parameter updates

        model.fit(trainIter, nEpochs);

        System.out.println("Evaluate model....");
        Evaluation eval = model.evaluate(testIter);
        System.out.println(eval.stats());
        System.out.println("\n****************Example finished********************");

        //Training is complete. Code that follows is for plotting the data & predictions only
        generateVisuals(model, trainIter, testIter);
    }

    public static void generateVisuals(MultiLayerNetwork model, DataSetIterator trainIter, DataSetIterator testIter) throws Exception {
        if (visualize) {
            double xMin = -15;
            double xMax = 15;
            double yMin = -15;
            double yMax = 15;

            //Let's evaluate the predictions at every point in the x/y input space, and plot this in the background
            int nPointsPerAxis = 100;

            //Generate x,y points that span the whole range of features
            /*INDArray allXYPoints = PlotUtil.generatePointsOnGraph(xMin, xMax, yMin, yMax, nPointsPerAxis);
            //Get train data and plot with predictions
            PlotUtil.plotTrainingData(model, trainIter, allXYPoints, nPointsPerAxis);
            TimeUnit.SECONDS.sleep(3);
            //Get test data, run the test data through the network to generate predictions, and plot those predictions:
            PlotUtil.plotTestData(model, testIter, allXYPoints, nPointsPerAxis);*/
        }
    }

}
