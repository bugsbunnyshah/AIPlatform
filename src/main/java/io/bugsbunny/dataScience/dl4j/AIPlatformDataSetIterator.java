package io.bugsbunny.dataScience.dl4j;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.bugsbunny.persistence.MongoDBJsonStore;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.api.DataSetPreProcessor;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class AIPlatformDataSetIterator implements DataSetIterator
{
    @Inject
    private MongoDBJsonStore mongoDBJsonStore;

    @Override
    public DataSet next(int i)
    {
        return null;
    }

    @Override
    public int inputColumns()
    {
        return 0;
    }

    @Override
    public int totalOutcomes()
    {
        return 0;
    }

    @Override
    public boolean resetSupported()
    {
        return false;
    }

    @Override
    public boolean asyncSupported() {
        return false;
    }

    @Override
    public void reset()
    {

    }

    @Override
    public int batch()
    {
        return 0;
    }

    @Override
    public void setPreProcessor(DataSetPreProcessor dataSetPreProcessor)
    {

    }

    @Override
    public DataSetPreProcessor getPreProcessor()
    {
        return null;
    }

    @Override
    public List<String> getLabels()
    {
        JsonObject dataSet = JsonParser.parseString(this.mongoDBJsonStore.readDataSet().toString()).getAsJsonObject();
        String data = dataSet.get("data").getAsString();
        List<String> labels = new ArrayList<>();
        labels.add(data);

        return labels;
    }

    @Override
    public boolean hasNext()
    {
        return false;
    }

    @Override
    public DataSet next()
    {
        return null;
    }
}
