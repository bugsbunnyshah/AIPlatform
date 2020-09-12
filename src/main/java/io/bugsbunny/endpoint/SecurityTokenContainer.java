package io.bugsbunny.endpoint;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class SecurityTokenContainer
{
    private ThreadLocal<String> tokenContainer = new ThreadLocal<>();

    public SecurityTokenContainer()
    {
    }

    public ThreadLocal<String> getTokenContainer()
    {
        return tokenContainer;
    }

    public void setTokenContainer(ThreadLocal<String> tokenContainer)
    {
        this.tokenContainer = tokenContainer;
    }
}
