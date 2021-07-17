package io.bugsbunny.dataIngestion.service;

import java.io.Serializable;

public class StreamObject implements Serializable {
    private String data;
    private String dataLakeId;

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getDataLakeId() {
        return dataLakeId;
    }

    public void setDataLakeId(String dataLakeId) {
        this.dataLakeId = dataLakeId;
    }
}
