package com.appgallabs.dataScience.service;

import org.datavec.api.split.InputStreamInputSplit;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;

public class ResettableStreamSplit extends InputStreamInputSplit {
    private String data;

    public ResettableStreamSplit(InputStream is, String path) {
        super(is, path);
    }

    public ResettableStreamSplit(InputStream is, File path) {
        super(is, path);
    }

    public ResettableStreamSplit(InputStream is, URI path) {
        super(is, path);
    }

    public ResettableStreamSplit(InputStream is) {
        super(is);
    }

    public ResettableStreamSplit(String data){
        this(new ByteArrayInputStream(data.getBytes(StandardCharsets.UTF_8)));
        this.data = data;
    }

    @Override
    public boolean resetSupported() {
        return true;
    }

    @Override
    public void reset() {
        this.setIs(new ByteArrayInputStream(this.data.getBytes(StandardCharsets.UTF_8)));
    }
}
