package com.appgallabs.infrastructure;

import com.appgallabs.util.JsonUtil;
import com.google.gson.JsonObject;
import jep.ClassList;
import jep.JepConfig;
import jep.MainInterpreter;
import jep.SharedInterpreter;
import org.eclipse.microprofile.config.ConfigProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Singleton;
import java.util.Arrays;

@Singleton
public class PythonEnvironment {
    private static Logger logger = LoggerFactory.getLogger(PythonEnvironment.class);

    private Thread execThread;
    private String script;
    private boolean isPythonDetected;


    @PostConstruct
    public void onStart(){
        try {
            String jepLibraryPath = ConfigProvider.getConfig().getValue("jepLibraryPath", String.class);
            MainInterpreter.setJepLibraryPath(jepLibraryPath);

            JepConfig jepConfig = new JepConfig();
            jepConfig.setClassLoader(Thread.currentThread().getContextClassLoader());
            SharedInterpreter.setConfig(jepConfig);

            System.out.println(Arrays.asList(ClassList.getInstance().getSubPackages("com")));

            this.isPythonDetected = true;

            logger.info("*******************************************");
            logger.info("PYTHON_RUNTIME_SUCCESSFULLY_DETECTED");
            logger.info("*******************************************");

            execThread = new Thread(new Runnable() {

                @Override
                public void run() {
                    while (true) {
                        if (script != null) {
                            SharedInterpreter interpreter = new SharedInterpreter();
                            interpreter.exec(script);
                            String df = interpreter.getValue("df", String.class);
                            JsonObject model = interpreter.getValue("model", JsonObject.class);
                            System.out.println(df);
                            JsonUtil.print(model);
                            script = null;
                            interpreter.close();
                        }
                        try {
                            Thread.sleep(5000);
                            if(script == null) {
                                System.out.println("SCRIPT: " + script);
                            }
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
            execThread.start();
        }
        catch (Exception | UnsatisfiedLinkError e)
        {
            e.printStackTrace();
            isPythonDetected = false;
            logger.info("*******************************************");
            logger.info("PYTHON_RUNTIME_WAS_NOT_DETECTED");
            logger.info("PYTHON_AIMODELS_CANNOT_BE_SUPPORTED_FOR_NOW");
            logger.info("*******************************************");
        }
    }

    public void executeScript(String script){
        this.script = script;
    }

    public boolean isPythonDetected() {
        return isPythonDetected;
    }
}
