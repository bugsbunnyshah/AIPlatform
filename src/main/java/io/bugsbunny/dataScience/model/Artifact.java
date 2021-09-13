package io.bugsbunny.dataScience.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;

public class Artifact implements Serializable {
    private static Logger logger = LoggerFactory.getLogger(Team.class);

    private PortableAIModelInterface aiModel;
    private DataSet dataSet;

    public Artifact() {
    }

    public PortableAIModelInterface getAiModel() {
        return aiModel;
    }

    public void setAiModel(PortableAIModelInterface aiModel) {
        this.aiModel = aiModel;
    }

    public DataSet getDataSet() {
        return dataSet;
    }

    public void setDataSet(DataSet dataSet) {
        this.dataSet = dataSet;
    }
}
