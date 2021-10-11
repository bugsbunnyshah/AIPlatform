package io.bugsbunny.dataScience.dl4j;

import com.google.gson.JsonArray;
import io.bugsbunny.preprocess.SecurityToken;
import io.bugsbunny.preprocess.SecurityTokenContainer;
import org.datavec.api.records.reader.RecordReader;
import org.datavec.api.records.reader.impl.csv.CSVRecordReader;
import org.datavec.api.split.FileSplit;
import org.deeplearning4j.datasets.datavec.RecordReaderDataSetIterator;
import org.deeplearning4j.datasets.iterator.impl.ListDataSetIterator;
import org.deeplearning4j.datasets.iterator.loader.DataSetLoaderIterator;
import org.nd4j.linalg.dataset.DataSet;
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

    public DataSetIterator getInstance(String[] dataSetIds)
    {
        SecurityToken securityToken = this.securityTokenContainer.getSecurityToken();
        AIPlatformDataSetSourceFactory sourceFactory = new AIPlatformDataSetSourceFactory(securityToken);

        JsonArray array = new JsonArray();
        for(String dataSetId:dataSetIds)
        {
            array.add(dataSetId);
        }
        Collection<String> paths = Arrays.asList(array.toString());

        //int batchSize = 50;
        //RecordReader rrTest = new CSVRecordReader();
        //rrTest.initialize(this.aiPlatformDataSetLoader);
        //final DataSetLoaderIterator dataSetLoaderIterator = new RecordReaderDataSetIterator(
        //        rrTest,
        //        batchSize, 0, 2);
        //new RecordReaderDataSetIterator(rrTest, batchSize, 0, 2);
        //DataSet dataSet = this.aiPlatformDataSetLoader.load()
        //final DataSetLoaderIterator dataSetLoaderIterator = new ListDataSetIterator<DataSet>()

        final DataSetLoaderIterator dataSetLoaderIterator = new DataSetLoaderIterator(paths,
                this.aiPlatformDataSetLoader, sourceFactory);

        return dataSetLoaderIterator;
    }
}
