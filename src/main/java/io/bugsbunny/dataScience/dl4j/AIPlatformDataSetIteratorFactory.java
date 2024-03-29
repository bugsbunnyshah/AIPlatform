package io.bugsbunny.dataScience.dl4j;

import com.google.gson.JsonArray;
import io.bugsbunny.preprocess.SecurityToken;
import io.bugsbunny.preprocess.SecurityTokenContainer;
import org.deeplearning4j.datasets.iterator.loader.DataSetLoaderIterator;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Arrays;
import java.util.Collection;

@ApplicationScoped
public class AIPlatformDataSetIteratorFactory
{
    @Inject
    private AIPlatformDataSetLoader aiPlatformDataSetLoader;

    @Inject
    private SecurityTokenContainer securityTokenContainer;

    public SecurityTokenContainer getSecurityTokenContainer() {
        return securityTokenContainer;
    }

    public void setSecurityTokenContainer(SecurityTokenContainer securityTokenContainer) {
        this.securityTokenContainer = securityTokenContainer;
    }

    public AIPlatformDataSetLoader getAiPlatformDataSetLoader() {
        return aiPlatformDataSetLoader;
    }

    public void setAiPlatformDataSetLoader(AIPlatformDataSetLoader aiPlatformDataSetLoader) {
        this.aiPlatformDataSetLoader = aiPlatformDataSetLoader;
    }

    public DataSetIterator getInstance(long[] dataSetIds)
    {
        SecurityToken securityToken = this.securityTokenContainer.getSecurityToken();
        AIPlatformDataSetSourceFactory sourceFactory = new AIPlatformDataSetSourceFactory(securityToken);

        JsonArray array = new JsonArray();
        for(long dataSetId:dataSetIds)
        {
            array.add(dataSetId);
        }
        Collection<String> paths = Arrays.asList(array.toString());

        final DataSetLoaderIterator dataSetLoaderIterator = new DataSetLoaderIterator(paths,
                this.aiPlatformDataSetLoader, sourceFactory);

        return dataSetLoaderIterator;
    }
}
