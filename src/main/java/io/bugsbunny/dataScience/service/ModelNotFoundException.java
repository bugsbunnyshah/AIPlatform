package io.bugsbunny.dataScience.service;

public class ModelNotFoundException extends Exception
{
    public ModelNotFoundException(String message)
    {
        super(message);
    }

    public ModelNotFoundException(Exception source)
    {
        super(source);
    }
}
