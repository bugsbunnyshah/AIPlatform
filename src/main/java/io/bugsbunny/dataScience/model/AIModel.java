package io.bugsbunny.dataScience.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AIModel implements PortableAIModelInterface{
    private static Logger logger = LoggerFactory.getLogger(Team.class);

    public AIModel() {
    }

    @Override
    public void load(String encodedModelString) {

    }

    @Override
    public void unload() {

    }

    @Override
    public double calculate() {
        return 0;
    }
}
