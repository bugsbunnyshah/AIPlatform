package io.bugsbunny.dataScience.service;

import org.python.util.PythonInterpreter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@ApplicationScoped
public class CloudMLService {
    private static Logger logger = LoggerFactory.getLogger(CloudMLService.class);

    public void executeScript(String script){
        try(PythonInterpreter pyInterp = new PythonInterpreter()) {
            InputStream scriptStream = new ByteArrayInputStream(
                    script.getBytes(StandardCharsets.UTF_8));
            pyInterp.execfile(scriptStream);
        }
    }
}
