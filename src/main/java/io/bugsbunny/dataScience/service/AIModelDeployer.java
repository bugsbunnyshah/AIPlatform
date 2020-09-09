package io.bugsbunny.dataScience.service;

import io.bugsbunny.restClient.DataBricksClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class AIModelDeployer
{
    private static Logger logger = LoggerFactory.getLogger(AIModelDeployer.class);

    @Inject
    private DataBricksClient dataBricksClient;
}
