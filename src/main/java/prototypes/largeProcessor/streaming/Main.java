package prototypes.largeProcessor.streaming;

import akka.Done;
import akka.NotUsed;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Cancellable;
import akka.dispatch.Futures;
import akka.event.LoggingAdapter;
import akka.japi.function.Procedure;
import akka.stream.*;
import akka.stream.impl.ExtendedActorMaterializer;
import akka.stream.impl.IslandTag;
import akka.stream.impl.Phase;
import akka.stream.javadsl.*;
import akka.util.ByteString;
import scala.collection.immutable.Map;
import scala.concurrent.ExecutionContextExecutor;
import scala.concurrent.duration.FiniteDuration;

import java.nio.file.Paths;
import java.math.BigInteger;
import java.time.Duration;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;

public class Main {
    public static void main(String[] argv) throws ExecutionException, InterruptedException {
        final ActorSystem system = ActorSystem.create("QuickStart");
        /*final Source<Integer, NotUsed> source = Source.range(1, 100);

        final Source<BigInteger, NotUsed> factorials =
                source.scan(BigInteger.ONE, (acc, next) -> acc.multiply(BigInteger.valueOf(next)));

        final CompletionStage<IOResult> result =
                factorials
                        .map(num -> ByteString.fromString(num.toString() + "\n"))
                        .runWith(FileIO.toPath(Paths.get("factorials.txt")), system);


        final CompletionStage<Done> done = source.runForeach(i -> System.out.println(i), system);
        done.thenRun(() -> system.terminate());*/

        /*final Source<Integer, NotUsed> source =
                Source.from(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 11));
        // note that the Future is scala.concurrent.Future
                final Sink<Integer, CompletionStage<Integer>> sink =
                        Sink.<Integer, Integer>fold(0, (aggr, next) -> aggr + next);

        // connect the Source to the Sink, obtaining a RunnableFlow
        final RunnableGraph<CompletionStage<Integer>> runnable = source.toMat(sink, Keep.right());

        // materialize the flow, getting the Sinks materialized value
        final CompletionStage<Integer> sum = source.runWith(sink, system);
        System.out.println(sum.toCompletableFuture().join());*/


        // Create a source from an Iterable
        List<Integer> list = new LinkedList<Integer>();
        for(int i=0; i<100; i++) {
            list.add(i);
        }
        Source<Integer, NotUsed> source = Source.from(list);
        Procedure<Integer> procedure = new Procedure<Integer>() {
            @Override
            public void apply(Integer param) throws Exception{
                System.out.println(Thread.currentThread()+":"+param.toString());
            }
        };


        //source.runForeach(procedure,system);

        // Create a source form a Future
        //Source.future(Futures.successful("Hello Streams!"));

        // Create a source from a single element
        //Source.single("only one element");

        // an empty source
        //Source.empty();

        // Sink that folds over the stream and returns a Future
        // of the final result in the MaterializedMap
        //Sink.fold(0, (Integer aggr, Integer next) -> aggr + next);

        // Sink that returns a Future in the MaterializedMap,
        // containing the first element of the stream
        //Sink.head();

        // A Sink that consumes a stream without doing anything with the elements
        //Sink.ignore();

        // A Sink that executes a side-effecting call for every element of the stream
        //Sink.foreach(System.out::println);


        // Explicitly creating and wiring up a Source, Sink and Flow
        /*Source.from(Arrays.asList(1, 2, 3, 4))
                .via(Flow.of(Integer.class).map(elem -> elem * 2))
                .to(Sink.foreach(System.out::println));

        // Starting from a Source
        final Source<Integer, NotUsed> source =
                Source.from(Arrays.asList(1, 2, 3, 4)).map(elem -> elem * 2);
        source.to(Sink.foreach(System.out::println));

        // Starting from a Sink
        final Sink<Integer, NotUsed> sink =
                Flow.of(Integer.class).map(elem -> elem * 2).to(Sink.foreach(System.out::println));
        Source.from(Arrays.asList(1, 2, 3, 4)).to(sink);*/
    }
}
