package io.bugsbunny.preprocess;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;

@Singleton
public class SecurityTokenContainer
{
    private static Logger logger = LoggerFactory.getLogger(SecurityTokenContainer.class);

    private static ThreadLocal<SecurityToken> tokenContainer;

    public SecurityTokenContainer()
    {
        tokenContainer = new ThreadLocal<>();
    }

    public void setSecurityToken(SecurityToken securityToken)
    {
        tokenContainer.set(securityToken);
    }

    public SecurityToken getSecurityToken()
    {
        return tokenContainer.get();
    }
}
