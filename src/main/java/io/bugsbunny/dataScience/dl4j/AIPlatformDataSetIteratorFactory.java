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
        ThreadLocal<SecurityToken> tokenContainer = this.securityTokenContainer.getTokenContainer();
        SecurityToken securityToken = tokenContainer.get();
        AIPlatformDataSetSourceFactory sourceFactory = new AIPlatformDataSetSourceFactory(securityToken);

        Collection<String> paths = Arrays.asList(""+dataSetId);
        return new DataSetLoaderIterator(paths,this.aiPlatformDataSetLoader,sourceFactory);
    }
}
