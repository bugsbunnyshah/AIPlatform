package com.appgallabs.dataScience.scripting;

import jep.Interpreter;
import jep.MainInterpreter;
import jep.SharedInterpreter;
import org.junit.jupiter.api.Test;

import org.python.util.PythonInterpreter;

import javax.net.ssl.*;
import java.io.InputStream;
import java.security.cert.X509Certificate;

//@QuarkusTest
public class JythonTests {

    @Test
    public void helloWorld() throws Exception {
        try(PythonInterpreter pyInterp = new PythonInterpreter()) {
            InputStream scriptStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(
                    "scripting/helloWorld.py");
            pyInterp.execfile(scriptStream);
        }
    }

    //@Test
    public void restInvocation() throws Exception {
        try {
            //java.security.Security.addProvider(new MyProvider());
            //java.security.Security.setProperty("ssl.TrustManagerFactory.algorithm",
            //        "TrustManagerFactory.TrustAllCertificates");
            this.trustAllCerts();
            try (PythonInterpreter pyInterp = new PythonInterpreter()) {
                InputStream scriptStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(
                        "scripting/restInvocation.py");
                pyInterp.execfile(scriptStream);
            }
        }
        catch(Exception e){
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Test
    public void restInvocationNative() throws Exception {
        try {
            //java.security.Security.addProvider(new MyProvider());
            //java.security.Security.setProperty("ssl.TrustManagerFactory.algorithm",
            //        "TrustManagerFactory.TrustAllCertificates");
            //this.trustAllCerts();
            try (PythonInterpreter pyInterp = new PythonInterpreter()) {
                InputStream scriptStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(
                        "scripting/restInvocationNative.py");
                pyInterp.execfile(scriptStream);
            }
        }
        catch(Exception e){
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private void trustAllCerts() throws Exception {

        TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {

            @Override
            public java.security.cert.X509Certificate[] getAcceptedIssuers() {

                return null;
            }

            @Override
            public void checkClientTrusted(X509Certificate[] certs,
                                           String authType) {

            }

            @Override
            public void checkServerTrusted(X509Certificate[] certs,
                                           String authType) {

            }
        } };

        // Install the all-trusting trust manager
        SSLContext sc = SSLContext.getInstance("SSL");
        sc.init(null, trustAllCerts, new java.security.SecureRandom());
        HttpsURLConnection
                .setDefaultSSLSocketFactory(sc.getSocketFactory());

        // Create all-trusting host name verifier
        HostnameVerifier allHostsValid = new HostnameVerifier() {

            @Override
            public boolean verify(String hostname, SSLSession session) {

                return true;
            }
        };

        // Install the all-trusting host verifier
        HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);

    }

    @Test
    public void testJPype() throws Exception{
        MainInterpreter.setJepLibraryPath("/usr/local/lib/python3.9/site-packages/jep/jep.cpython-39-darwin.so");
        try (Interpreter interp = new SharedInterpreter())
        {
            String pythonScript = "# Boiler plate stuff to start the module\n" +
                    "import jpype\n" +
                    "import jpype.imports\n" +
                    "from jpype.types import *\n" +
                    "import pandas as pd\n" +
                    "from java.util import Random\n" +
                    "from io.bugsbunny.dataScience.function.model import CreateModel\n" +
                    "from io.bugsbunny.dataScience.function.model import InputLayer\n" +
                    "from io.bugsbunny.dataScience.function.model import OutputLayer\n" +
                    "from org.nd4j.linalg.activations import Activation\n" +
                    "from org.nd4j.linalg.lossfunctions import LossFunctions\n" +
                    "from org.deeplearning4j.nn.weights import WeightInit\n" +
                    "\n" +
                    "df = pd.DataFrame()\n" +
                    "print(df)";
            interp.exec(pythonScript);
            String df = interp.getValue("df", String.class);
            System.out.println(df);
        }
    }
}
