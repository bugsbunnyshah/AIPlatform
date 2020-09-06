package io.bugsbunny.pipeline;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import java.io.IOException;

@ApplicationScoped
public class ModelDeployer {
    private static Logger logger = LoggerFactory.getLogger(ModelDeployer.class);

    //mlflow models serve
    public int deploy()
    {
        try {
            String serveModelsCommand = "mlflow models serve";
            final Process process = Runtime.getRuntime().exec(serveModelsCommand);
            logger.info("**********");
            logger.info("ISALIVE: " + process.isAlive());
            logger.info("**********");

            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        logger.info("**********");
                        logger.info("ISALIVE: " + process.isAlive());
                        logger.info("**********");
                        while (process.waitFor() != 0);
                    }
                    catch(InterruptedException iexception)
                    {
                        throw new RuntimeException(iexception);
                    }
                }
            });
            t.start();

            t.join();

            return process.exitValue();
        }
        catch(IOException ioe)
        {
            throw new RuntimeException(ioe.getMessage());
        }
        catch(InterruptedException iexception)
        {
            throw new RuntimeException(iexception);
        }
    }

    public int deployPythonTraining()
    {
        try {
            String serveModelsCommand = "jep /Users/babyboy/mamasboy/appgallabsForProfit/genomics_research/machineLearningPipelinePrototype/src/main/java/io/bugsbunny/dataScience/model/trainModel.py";
            final Process process = Runtime.getRuntime().exec(serveModelsCommand);
            logger.info("**********");
            logger.info("ISALIVE:" + process.isAlive());
            logger.info("**********");

            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        logger.info("**********");
                        logger.info("ISALIVE: " + process.isAlive());
                        logger.info("**********");
                        while (process.waitFor() != 0);
                    }
                    catch(InterruptedException iexception)
                    {
                        throw new RuntimeException(iexception);
                    }
                }
            });
            t.start();

            t.join();

            return process.exitValue();
        }
        catch(IOException ioe)
        {
            throw new RuntimeException(ioe.getMessage());
        }
        catch(InterruptedException iexception)
        {
            throw new RuntimeException(iexception);
        }
    }

}
