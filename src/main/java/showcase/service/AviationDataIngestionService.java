package showcase.service;

import io.bugsbunny.dataScience.service.ModelDataSetService;
import io.bugsbunny.preprocess.SecurityTokenContainer;
import io.bugsbunny.restClient.OAuthClient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class AviationDataIngestionService {
    private static Logger logger = LoggerFactory.getLogger(AviationDataIngestionService.class);

    @Inject
    private OAuthClient oAuthClient;

    @Inject
    private SecurityTokenContainer securityTokenContainer;

    @Inject
    private ModelDataSetService modelDataSetService;

    private IngestData ingestData;

    private List<String> dataSetIds;

    public AviationDataIngestionService()
    {
        this.dataSetIds = new ArrayList<>();
    }

    public void startIngestion()
    {
        this.ingestData = new IngestData(this.oAuthClient,this.securityTokenContainer, this,
                this.modelDataSetService);
        ingestData.start();
    }

    public void registerDataSetId(String dataSetId)
    {
        logger.info("***********************");
        logger.info("REGISTERING: "+dataSetId);
        logger.info("***********************");
        this.dataSetIds.add(dataSetId);
    }

    public List<String> getDataSetIds()
    {
        return this.dataSetIds;
    }
}
