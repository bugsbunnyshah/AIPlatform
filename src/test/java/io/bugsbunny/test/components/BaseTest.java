package io.bugsbunny.test.components;

import io.bugsbunny.dataIngestion.service.StreamIngesterContext;
import io.bugsbunny.infrastructure.MongoDBJsonStore;
import io.bugsbunny.preprocess.SecurityToken;
import io.bugsbunny.preprocess.SecurityTokenContainer;
import io.bugsbunny.util.BackgroundProcessListener;
import io.bugsbunny.util.JsonUtil;
import io.bugsbunny.util.ObjectUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

public abstract class BaseTest
{
    private static Logger logger = LoggerFactory.getLogger(BaseTest.class);

    @Inject
    private MongoDBJsonStore mongoDBJsonStore;

    @Inject
    private SecurityTokenMockComponent securityTokenMockComponent;

    @Inject
    private SecurityTokenContainer securityTokenContainer;

    @BeforeEach
    public void setUp() throws Exception
    {
        this.securityTokenMockComponent.start();
    }

    @AfterEach
    void tearDown() {
        BackgroundProcessListener.getInstance().clear();

        try {
            if (this.mongoDBJsonStore == null) {
                this.mongoDBJsonStore = new MongoDBJsonStore();
            }
            this.mongoDBJsonStore.start();
            String principal = this.securityTokenContainer.getTenant().getPrincipal();
            String databaseName = principal + "_" + "aiplatform";
            this.mongoDBJsonStore.getMongoClient().getDatabase(databaseName).drop();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    protected void startIngester()
    {
        BackgroundProcessListener.getInstance().clear();

        /*try {
            Thread.sleep(5000);
        }catch (Exception e){}*/

        StreamIngesterContext.getStreamIngester().start();
    }

    protected void stopIngester()
    {
        try {
            StreamIngesterContext.getStreamIngester().stop();
        }catch (Exception e)
        {
            logger.error(e.getMessage(),e);
        }
        finally {
            BackgroundProcessListener.getInstance().clear();
        }
    }
}
