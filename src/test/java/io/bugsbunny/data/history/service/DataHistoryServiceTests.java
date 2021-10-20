package io.bugsbunny.data.history.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.bugsbunny.infrastructure.MongoDBJsonStore;
import io.bugsbunny.infrastructure.Tenant;
import io.bugsbunny.preprocess.SecurityTokenContainer;
import io.bugsbunny.test.components.BaseTest;
import io.bugsbunny.util.JsonUtil;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.security.NoSuchAlgorithmException;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Iterator;
import java.util.UUID;

@QuarkusTest
public class DataHistoryServiceTests extends BaseTest {
    private static Logger logger = LoggerFactory.getLogger(DataHistoryServiceTests.class);

    @Inject
    private DataHistoryService service;

    @Inject
    private MongoDBJsonStore mongoDBJsonStore;

    @Inject
    private SecurityTokenContainer securityTokenContainer;

    @Test
    public void getDataSnapShot() throws Exception {
        //ingestion0
        OffsetDateTime ingestion0Time = OffsetDateTime.now(ZoneOffset.UTC);
        JsonArray ingestion0 = this.mockIngestion(ingestion0Time,2);
        this.performIngestion(ingestion0);

        //ingestion1
        OffsetDateTime ingestion1Time = OffsetDateTime.now(ZoneOffset.UTC);
        ingestion1Time = ingestion1Time.plus(5, ChronoUnit.MINUTES);
        JsonArray ingestion1 = this.mockIngestion(ingestion1Time,3);
        this.performIngestion(ingestion1);

        //ingestion2
        OffsetDateTime ingestion2Time = OffsetDateTime.now(ZoneOffset.UTC);
        ingestion2Time = ingestion2Time.plus(10, ChronoUnit.MINUTES);
        JsonArray ingestion2 = this.mockIngestion(ingestion2Time,2);
        this.performIngestion(ingestion2);

        //ingestion3
        OffsetDateTime ingestion3Time = OffsetDateTime.now(ZoneOffset.UTC);
        ingestion3Time = ingestion3Time.plus(15, ChronoUnit.MINUTES);
        JsonArray ingestion3 = this.mockIngestion(ingestion3Time,3);
        this.performIngestion(ingestion3);

        //Generate State
        OffsetDateTime start = ingestion0Time;
        OffsetDateTime end = ingestion1Time;
        JsonArray snapShot = this.service.getDataSnapShot(start,end);
        JsonUtil.print(snapShot);
    }

    private JsonArray mockIngestion(OffsetDateTime ingestionTime,int size) throws NoSuchAlgorithmException {
        JsonArray ingestion = new JsonArray();
        for(int i=0; i<size; i++){
            JsonObject data = new JsonObject();
            ingestion.add(data);
            data.addProperty("oid", UUID.randomUUID().toString());
            data.addProperty("1", UUID.randomUUID().toString());
            data.addProperty("2",UUID.randomUUID().toString());
            data.addProperty("3", UUID.randomUUID().toString());
            String objectHash = JsonUtil.getJsonHash(data);
            data.addProperty("timestamp",ingestionTime.toEpochSecond());
            data.addProperty("objectHash",objectHash);
        }
        return ingestion;
    }

    private void performIngestion(JsonArray ingestion){
        Tenant tenant = this.securityTokenContainer.getTenant();

        Iterator<JsonElement> iterator = ingestion.iterator();
        while(iterator.hasNext()){
            this.mongoDBJsonStore.storeHistoryObject(tenant,iterator.next().getAsJsonObject());
        }
    }
}
