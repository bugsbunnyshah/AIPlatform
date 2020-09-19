package io.bugsbunny.dataScience.dl4j;

import org.nd4j.common.loader.Source;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class DataSetSource implements Source
{
    @Override
    public InputStream getInputStream() throws IOException
    {
        AIPlatformDataSetLoader aiPlatformDataSetLoader = new AIPlatformDataSetLoader();
        return new ByteArrayInputStream(aiPlatformDataSetLoader.load(null).toString().getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public String getPath()
    {
        return "AIPlatformDataSetLoader";
    }
}
