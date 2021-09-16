//
package io.bugsbunny.dataScience.dl4j;

import io.bugsbunny.preprocess.SecurityToken;
import org.nd4j.common.loader.Source;
import org.nd4j.common.loader.SourceFactory;

public class AIPlatformDataLakeSourceFactory implements SourceFactory
{
    private SecurityToken securityToken;

    public AIPlatformDataLakeSourceFactory(SecurityToken securityToken)
    {
        this.securityToken = securityToken;
    }

    @Override
    public Source getSource(String s)
    {
        return new AIPlatformDataLakeSource(this.securityToken,s);
    }
}
