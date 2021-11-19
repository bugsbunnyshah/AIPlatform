package com.appgallabs.dataScience.service;

public class ArtifactNotFoundException extends Exception
{
    public ArtifactNotFoundException(String message)
    {
        super(message);
    }

    public ArtifactNotFoundException(Exception source)
    {
        super(source);
    }
}
