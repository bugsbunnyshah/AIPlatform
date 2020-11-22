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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

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
        int batchSize = 64; // Test batch size
        int nEpochs = 2;   // Number of training epochs
        int seed = 123;

        //Lambda defines the relative strength of the center loss component.
        //lambda = 0.0 is equivalent to training with standard softmax only
        double lambda = 1.0;

        //Alpha can be thought of as the learning rate for the centers for each class
        double alpha = 0.1;

        logger.info("Load data....");
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

        //logger.info(JsonFlattener.flattenAsMap(data).toString());


        /*String[] columns = data.split(",");
        for(String column:columns)
        {
            logger.info(column);
        }*/

        /*RecordReader rrTest = new CSVRecordReader();
        File testFile = new File("tmp"+"/"+"flights.csv");
        FileUtils.writeStringToFile(testFile, data);
        rrTest.initialize(new FileSplit(testFile));
        DataSetIterator testIter = new RecordReaderDataSetIterator(rrTest, batchSize, 0, 2);
        //logger.info(testIter.inputColumns()+"");

        logger.info("Build model....");
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

        MultiLayerNetwork model = new MultiLayerNetwork(conf);
        model.init();
        model.fit(testIter, nEpochs);*/
    }
}
