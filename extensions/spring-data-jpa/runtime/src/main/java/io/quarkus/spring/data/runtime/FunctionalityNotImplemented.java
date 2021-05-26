package io.quarkus.spring.data.runtime;

public class FunctionalityNotImplemented extends RuntimeException {

    public FunctionalityNotImplemented(String className, String methodName) {
        super("Method " + methodName + " of class " + className + " from Spring GraphData has not yet been implemented");
    }
}
