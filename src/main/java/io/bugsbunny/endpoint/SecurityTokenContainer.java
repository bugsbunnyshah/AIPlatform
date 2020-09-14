package io.bugsbunny.endpoint;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class SecurityTokenContainer
{
    private ThreadLocal<SecurityToken> tokenContainer = new ThreadLocal<>();

    public SecurityTokenContainer()
    {
    }

    public ThreadLocal<SecurityToken> getTokenContainer()
    {
        return tokenContainer;
    }

    public void setTokenContainer(ThreadLocal<SecurityToken> tokenContainer)
    {
        this.tokenContainer = tokenContainer;
    }
}
