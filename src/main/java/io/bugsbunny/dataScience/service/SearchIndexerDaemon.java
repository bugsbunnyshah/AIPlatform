package io.bugsbunny.dataScience.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

@ApplicationScoped
public class SearchIndexerDaemon {
    private static Logger logger = LoggerFactory.getLogger(SearchIndexerDaemon.class);

    private Timer timer;

    @Inject
    private TrainingWorkflow trainingWorkflow;

    public void start()
    {
        this.timer = new Timer();
        this.timer.schedule(new IndexerTask(), 1000, 2000);
    }

    @PreDestroy
    public void stop()
    {
        this.timer.cancel();
        this.timer.purge();
    }

    private class IndexerTask extends TimerTask
    {

        @Override
        public void run()
        {
            try
            {
                SearchIndexerDaemon.this.trainingWorkflow.updateIndex();
            }
            catch(IOException ioe)
            {
                logger.error(ioe.getMessage(), ioe);
            }
        }
    }
}
