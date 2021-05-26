package io.bugsbunny.query;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.bugsbunny.util.JsonUtil;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

@QuarkusTest
public class ObjectGraphQueryServiceTests {
    private static Logger logger = LoggerFactory.getLogger(ObjectGraphQueryServiceTests.class);

    @Inject
    private ObjectGraphQueryService service;

    @Test
    public void queryByCriteria() throws Exception
    {
        JsonObject criteria = new JsonObject();
        criteria.addProperty("size", 100);
        //criteria.addProperty("code", "aus");

        JsonArray array = service.queryByCriteria("airport", criteria);
        JsonUtil.print(array);
    }

    @Test
    public void navigateByCriteria() throws Exception
    {
        JsonObject departureCriteria = new JsonObject();
        departureCriteria.addProperty("code","aus");
        JsonArray array = this.service.navigateByCriteria("airport","flight",
                "departure",departureCriteria);

        JsonUtil.print(array);

        JsonObject arrivalCriteria = new JsonObject();
        arrivalCriteria.addProperty("code","lax");
        array = this.service.navigateByCriteria("airport","flight",
                "arrival",arrivalCriteria);
        JsonUtil.print(array);
    }
}
