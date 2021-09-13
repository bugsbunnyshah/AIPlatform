package io.bugsbunny.dataScience.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.List;

public class DataSet implements Serializable {
    private static Logger logger = LoggerFactory.getLogger(Team.class);

    List<DataItem> data;

    public DataSet() {
    }

    public List<DataItem> getData() {
        return data;
    }

    public void setData(List<DataItem> data) {
        this.data = data;
    }
}
