package io.bugsbunny.dataIngestion.service;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.Queue;

public class StreamIngesterQueue implements Serializable {
    private Queue<StreamObject> queue;

    public StreamIngesterQueue(){
        this.queue = new LinkedList<>();
    }

    public void add(StreamObject streamObject)
    {
        this.queue.add(streamObject);
    }

    public StreamObject latest()
    {
        return this.queue.poll();
    }
}
