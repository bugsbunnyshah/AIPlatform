//
package io.bugsbunny.dataScience.dl4j;

import io.bugsbunny.endpoint.SecurityToken;

import org.nd4j.common.loader.Source;
import org.nd4j.common.loader.SourceFactory;

public class AIPlatformDataSetSourceFactory implements SourceFactory
{
    private SecurityToken securityToken;

    public AIPlatformDataSetSourceFactory(SecurityToken securityToken)
    {
        this.securityToken = securityToken;
    }

    @Override
    public Source getSource(String s)
    {
        return new AIPlatformDataSetSource(this.securityToken,s);
    }
}
