//
package io.bugsbunny.dataScience.dl4j;

import io.bugsbunny.dataScience.model.Artifact;
import io.bugsbunny.preprocess.SecurityToken;
import org.nd4j.common.loader.Source;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;

public class AIPlatformDataLakeSource implements Source
{
    private static Logger logger = LoggerFactory.getLogger(AIPlatformDataLakeSource.class);

    private String dataLakeIds;
    private Artifact artifact;
    private SecurityToken securityToken;

    public AIPlatformDataLakeSource(SecurityToken securityToken,Artifact artifact, String dataLakeIds)
    {
        this.securityToken = securityToken;
        this.artifact = artifact;
        this.dataLakeIds = dataLakeIds;
    }

    public SecurityToken getSecurityToken()
    {
        return securityToken;
    }

    public Artifact getArtifact() {
        return artifact;
    }

    @Override
    public InputStream getInputStream() throws IOException
    {
        throw new RuntimeException("OP_NOT_SUPPORTED");
    }

    @Override
    public String getPath()
    {
        return this.dataLakeIds;
    }
}
