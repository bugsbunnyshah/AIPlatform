//
package io.bugsbunny.dataScience.dl4j;

import org.nd4j.common.loader.Source;
import org.nd4j.common.loader.SourceFactory;

public class DataSetSourceFactory implements SourceFactory
{
    @Override
    public Source getSource(String s)
    {
        return new DataSetSource();
    }
}
