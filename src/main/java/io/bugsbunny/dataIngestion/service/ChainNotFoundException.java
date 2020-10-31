package io.bugsbunny.dataIngestion.service;

public class ChainNotFoundException extends Exception
{
    public ChainNotFoundException(String message)
    {
        super(message);
    }

    public ChainNotFoundException(Exception source)
    {
        super(source);
    }
}
