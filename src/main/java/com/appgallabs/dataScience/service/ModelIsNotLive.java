package com.appgallabs.dataScience.service;

public class ModelIsNotLive extends Exception
{
    public ModelIsNotLive(String message)
    {
        super(message);
    }

    public ModelIsNotLive(Exception source)
    {
        super(source);
    }
}
