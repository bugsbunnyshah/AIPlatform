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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;

//import jdocs.AbstractJavaTest;

public class AkkaStreamProcessor {
    private static Logger logger = LoggerFactory.getLogger(AkkaStreamProcessor.class);

    public static void main(String[] args) throws Exception
    {
        final ActorSystem system = ActorSystem.create("QuickStart");

        final Source<Integer, NotUsed> source = Source.range(1, 100);
        final Source<BigInteger, NotUsed> factorials =
                source.scan(BigInteger.ONE, (acc, next) -> acc.multiply(BigInteger.valueOf(next)));

        final CompletionStage<IOResult> result =
                factorials
                        .map(num -> ByteString.fromString(num.toString() + "\n"))
                        .runWith(FileIO.toPath(Paths.get("factorials.txt")), system);
        //final CompletableFuture<IOResult> ioResultCompletableFuture = result.toCompletableFuture();


        final CompletionStage<Done> done = source.runForeach(i -> System.out.println(i), system);
        done.thenRun(() -> system.terminate());
    }
}
