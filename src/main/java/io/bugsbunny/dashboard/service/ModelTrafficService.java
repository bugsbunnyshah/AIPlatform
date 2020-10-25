package io.bugsbunny.dashboard.service;

import com.google.gson.JsonObject;
import io.bugsbunny.data.history.service.PayloadReplayService;
import io.bugsbunny.preprocess.SecurityToken;
import io.bugsbunny.preprocess.SecurityTokenContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class ModelTrafficService
{
    private static Logger logger = LoggerFactory.getLogger(ModelTrafficService.class);

    @Inject
    private PayloadReplayService payloadReplayService;

    @Inject
    private SecurityTokenContainer securityTokenContainer;

    public Map<String,List<JsonObject>> getModelTraffic()
    {
        SecurityToken securityToken = this.securityTokenContainer.getSecurityToken();
        return this.payloadReplayService.replayDiffChainByPrincipal(securityToken.getPrincipal());
    }
}
