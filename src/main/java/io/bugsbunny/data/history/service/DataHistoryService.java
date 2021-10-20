package io.bugsbunny.data.history.service;

import com.google.gson.JsonArray;
import io.bugsbunny.infrastructure.MongoDBJsonStore;
import io.bugsbunny.infrastructure.Tenant;
import io.bugsbunny.preprocess.SecurityTokenContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.time.OffsetDateTime;

@ApplicationScoped
public class DataHistoryService {
    private static Logger logger = LoggerFactory.getLogger(DataHistoryService.class);

    @Inject
    private MongoDBJsonStore mongoDBJsonStore;

    @Inject
    private SecurityTokenContainer securityTokenContainer;

    public JsonArray getDataSnapShot(OffsetDateTime start, OffsetDateTime end){
        Tenant tenant = this.securityTokenContainer.getTenant();
        JsonArray snapShot = this.mongoDBJsonStore.readHistory(tenant,end);
        return snapShot;
    }
}
