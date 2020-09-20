package io.bugsbunny.dataScience.dl4j;

import org.deeplearning4j.datasets.iterator.loader.DataSetLoaderIterator;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.dataset.api.iterator.DataSetIteratorFactory;

import java.util.ArrayList;
import java.util.Collection;

public class AIPlatformDataSetIteratorFactory {

    public static DataSetIterator getInstance()
    {
        Collection<String> paths = new ArrayList<>();
        paths.add("AIPlatformDataSetLoader");
        AIPlatformDataSetLoader loader = new AIPlatformDataSetLoader();
        DataSetSourceFactory sourceFactory = new DataSetSourceFactory();
        return new DataSetLoaderIterator(paths,loader,sourceFactory);
    }
}
