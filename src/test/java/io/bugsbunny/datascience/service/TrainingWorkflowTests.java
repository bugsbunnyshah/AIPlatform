package io.bugsbunny.datascience.service;

import io.quarkus.test.junit.QuarkusTest;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

@QuarkusTest
public class TrainingWorkflowTests {
    private static Logger logger = LoggerFactory.getLogger(TrainingWorkflowTests.class);

    @Test
    public void testStartTraining() throws Exception
    {
        //Save the model
        File locationToSave = new File("MyMultiLayerNetwork.zip");
        //Where to save the network. Note: the file is in .zip format - can be opened externally

        //Load the model
        boolean saveUpdater = true;
        MultiLayerNetwork restored = MultiLayerNetwork.load(locationToSave, saveUpdater);
    }
}
