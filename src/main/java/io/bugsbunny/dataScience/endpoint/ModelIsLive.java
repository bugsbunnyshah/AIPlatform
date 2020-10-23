package io.bugsbunny.dataScience.endpoint;

public class ModelIsLive extends Exception
{
    public ModelIsLive(String message)
    {
        super(message);
    }

    public ModelIsLive(Exception source)
    {
        super(source);
    }
}
