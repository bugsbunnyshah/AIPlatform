package io.bugsbunny.dataIngestion.service;

import java.io.Serializable;

public class StreamIngesterQueue implements Serializable {
    private String data;

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }
}
