package io.bugsbunny.dataScience.model;

public interface PortableAIModelInterface
{
    public void load(String encodedModelString);
    public void unload();
    public double calculate();
}