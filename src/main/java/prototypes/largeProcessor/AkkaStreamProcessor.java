package prototypes.largeProcessor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import akka.stream.*;
import akka.stream.javadsl.*;

import akka.Done;
import akka.NotUsed;
import akka.actor.ActorSystem;
import akka.util.ByteString;

import java.nio.file.Paths;
import java.math.BigInteger;
import java.time.Duration;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;

//import jdocs.AbstractJavaTest;

public class AkkaStreamProcessor {
    private static Logger logger = LoggerFactory.getLogger(AkkaStreamProcessor.class);

    public static void main(String[] args) throws Exception
    {
        final ActorSystem system = ActorSystem.create("QuickStart");

        final Source<Integer, NotUsed> source = Source.range(1, 100);
        source.runForeach(i -> System.out.println(i), system);

        final CompletionStage<Done> done = source.runForeach(i -> System.out.println(i), system);

        done.thenRun(() -> system.terminate());
    }
}
