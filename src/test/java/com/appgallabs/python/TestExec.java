package com.appgallabs.python;

import jep.*;
import org.python.util.PythonInterpreter;

import java.io.InputStream;
import java.util.Arrays;

/**
 * Tests ability to execute multiple lines of python using exec()
 * 
 * Created: August 2019
 * 
 * @author Ben Steffensmeier
 */
public class TestExec
{

    public static void main(String[] args) throws JepException {
        MainInterpreter.setJepLibraryPath("/usr/local/lib/python3.9/site-packages/jep/jep.cpython-39-darwin.so");
        JepConfig jepConfig = new JepConfig();
        jepConfig.setClassLoader(Thread.currentThread().getContextClassLoader());
        SharedInterpreter.setConfig(jepConfig);
        try (Interpreter interp = new SharedInterpreter()) {
            String pythonScript = "# Boiler plate stuff to start the module\n" +
                    "import jpype\n" +
                    "import jpype.imports\n" +
                    "from jpype.types import *\n" +
                    "import pandas as pd\n" +
                    "from java.util import Random\n" +
                    "from org.nd4j.linalg.activations import Activation\n" +
                    "from org.nd4j.linalg.lossfunctions import LossFunctions\n" +
                    "from org.deeplearning4j.nn.weights import WeightInit\n" +
                    "from com.appgallabs.dataScience.function.model import CreateModel\n" +
                    "from com.appgallabs.dataScience.function.model import InputLayer\n" +
                    "from com.appgallabs.dataScience.function.model import OutputLayer\n" +
                    "\n" +
                    "df = pd.DataFrame()\n" +
                    "print(df)";
            interp.exec(pythonScript);
            String df = interp.getValue("df", String.class);
            System.out.println(df);
        }


        /*StringBuilder script = new StringBuilder();
        script.append("a = 'Hello'\n");
        script.append("b = 'Failed'\n");
        script.append("result = max(a,b)\n");
        script.append("print('hello world')\n");
        try (Interpreter interp = new SharedInterpreter()) {
            interp.exec(script.toString());
            String result = interp.getValue("result", String.class);
            if (!"Hello".equals(result)) {
                throw new IllegalStateException(
                        "multi-line exec returned " + result);
            }
            System.out.println(result);
        }*/



        /*try (PythonInterpreter pyInterp = new PythonInterpreter()) {
            InputStream scriptStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(
                    "cloudml/createModel.py");
            pyInterp.execfile(scriptStream);
        }*/

        /*try (Interpreter interp = new SharedInterpreter())
        {
            String pythonScript = "from io.bugsbunny.dataScience.function.model import CreateModel\n" +
                    "from io.bugsbunny.dataScience.function.model import InputLayer\n" +
                    "from io.bugsbunny.dataScience.function.model import OutputLayer\n" +
                    "from org.nd4j.linalg.activations import Activation\n" +
                    "from org.nd4j.linalg.lossfunctions import LossFunctions\n" +
                    "from org.deeplearning4j.nn.weights import WeightInit\n" +
                    "\n" +
                    "inputLayer = InputLayer(Activation.RELU.name());\n" +
                    "outputLayer = OutputLayer(Activation.SOFTMAX.name(),\n" +
                    "                                          LossFunctions.LossFunction.SQUARED_LOSS.name());\n" +
                    "inputLayers = [inputLayer]\n" +
                    "\n" +
                    "outputLayers = [outputLayer]\n" +
                    "\n" +
                    "cloudMlFunction = CreateModel(123,\n" +
                    "                             0.008,\n" +
                    "                             1,WeightInit.XAVIER.name(),\n" +
                    "                             inputLayers,\n" +
                    "                             outputLayers\n" +
                    ")\n" +
                    "print(cloudMlFunction.execute())";
            interp.exec(pythonScript);
            //String output = interp.getValue("output", String.class);
            //System.out.println(output);
        }*/
    }

}
