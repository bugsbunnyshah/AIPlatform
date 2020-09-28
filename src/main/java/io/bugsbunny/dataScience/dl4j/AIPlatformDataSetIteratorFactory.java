package io.bugsbunny.dataScience.dl4j;

import io.bugsbunny.endpoint.SecurityToken;
import io.bugsbunny.endpoint.SecurityTokenContainer;
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

    public DataSetIterator getInstance(long dataSetId)
    {
        SecurityToken securityToken = this.securityTokenContainer.getSecurityToken();
        AIPlatformDataSetSourceFactory sourceFactory = new AIPlatformDataSetSourceFactory(securityToken);

        Collection<String> paths = Arrays.asList(""+dataSetId);

        final DataSetLoaderIterator dataSetLoaderIterator = new DataSetLoaderIterator(paths, this.aiPlatformDataSetLoader, sourceFactory);

        return dataSetLoaderIterator;
    }
}
