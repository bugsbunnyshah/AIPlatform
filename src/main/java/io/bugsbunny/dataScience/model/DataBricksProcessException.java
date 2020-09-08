package io.bugsbunny.dataScience.model;

public class DataBricksProcessException extends Exception
{
    public DataBricksProcessException(String message)
    {
        super(message);
    }

    public DataBricksProcessException(Exception source)
    {
        super(source);
    }
}
