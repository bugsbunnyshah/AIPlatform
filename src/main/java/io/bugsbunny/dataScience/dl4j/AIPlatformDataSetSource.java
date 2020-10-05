//
package io.bugsbunny.dataScience.dl4j;

import io.bugsbunny.endpoint.SecurityToken;
import org.nd4j.common.loader.Source;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;

public class AIPlatformDataSetSource implements Source
{
    private static Logger logger = LoggerFactory.getLogger(AIPlatformDataSetSource.class);

    private String dataSetIds;
    private SecurityToken securityToken;

    public AIPlatformDataSetSource(SecurityToken securityToken, String dataSetIds)
    {
        this.securityToken = securityToken;
        this.dataSetIds = dataSetIds;
    }

    public SecurityToken getSecurityToken()
    {
        return securityToken;
    }

    @Override
    public InputStream getInputStream() throws IOException
    {
        throw new RuntimeException("OP_NOT_SUPPORTED");
    }

    @Override
    public String getPath()
    {
        return this.dataSetIds;
    }
}
