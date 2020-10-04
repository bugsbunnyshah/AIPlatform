package io.bugsbunny.restClient;

public class OAuthException extends Exception
{
    public OAuthException(String message)
    {
        super(message);
    }

    public OAuthException(Exception source)
    {
        super(source);
    }
}
