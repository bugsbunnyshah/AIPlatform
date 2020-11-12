package io.bugsbunny.python;

import jep.Interpreter;
import jep.JepException;
import jep.MainInterpreter;
import jep.SharedInterpreter;

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
        MainInterpreter.setJepLibraryPath("/Users/babyboy/opt/anaconda3/lib/python3.8/site-packages/jep/jep.cpython-38-darwin.so");
        StringBuilder script = new StringBuilder();
        script.append("a = 'Passed'\n");
        script.append("b = 'Failed'\n");
        script.append("result = max(a,b)\n");
        script.append("print('hello world')\n");
        try (Interpreter interp = new SharedInterpreter()) {
            interp.exec(script.toString());
            String result = interp.getValue("result", String.class);
            if (!"Passed".equals(result)) {
                throw new IllegalStateException(
                        "multi-line exec returned " + result);
            }
        }
    }

}
