package io.bugsbunny.dataScience.dl4j;

import org.deeplearning4j.datasets.iterator.loader.DataSetLoaderIterator;
import org.nd4j.common.loader.Loader;
import org.nd4j.common.loader.SourceFactory;
import org.nd4j.linalg.dataset.DataSet;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class AIPlatformDataSetLoaderIterator extends DataSetLoaderIterator
{
    public AIPlatformDataSetLoaderIterator(Collection<String> paths, Loader<DataSet> loader, SourceFactory sourceFactory) {
        super(paths, loader, sourceFactory);
    }

    @Override
    public int totalOutcomes() {
        return 2;
    }

    @Override
    public List<String> getLabels() {
        return Arrays.asList("0","1");
    }
}
