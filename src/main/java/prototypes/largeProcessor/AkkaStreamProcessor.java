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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

//import jdocs.AbstractJavaTest;

public class AkkaStreamProcessor {
    private static Logger logger = LoggerFactory.getLogger(AkkaStreamProcessor.class);

    public static void main(String[] args) throws Exception
    {
        /*final ActorSystem system = ActorSystem.create("QuickStart");

        final Source<Integer, NotUsed> source = Source.range(1, 100);
        final Source<BigInteger, NotUsed> factorials =
                source.scan(BigInteger.ONE, (acc, next) -> acc.multiply(BigInteger.valueOf(next)));

        final CompletionStage<IOResult> result =
                factorials
                        .map(num -> ByteString.fromString(num.toString() + "\n"))
                        .runWith(FileIO.toPath(Paths.get("factorials.txt")), system);
        //final CompletableFuture<IOResult> ioResultCompletableFuture = result.toCompletableFuture();


        final CompletionStage<Done> done = source.runForeach(i -> System.out.println(i), system);
        done.thenRun(() -> system.terminate());*/

        final ActorSystem system = ActorSystem.create("reactive-tweets");

        List<Tweet> data = new ArrayList<>();
        for(int i=0; i<10; i++)
        {
            Tweet tweet = new Tweet(new Author(AKKA.name), System.currentTimeMillis(),"blah:"+i);
            data.add(tweet);
        }

        List<Author> authorData = new ArrayList<>();
        for(int i=0; i<10; i++)
        {
            Author author = new Author(AKKA.name);
            authorData.add(author);
        }

        Source<Tweet, NotUsed> tweets = Source.from(data);
        final Source<Author, NotUsed> authors = Source.from(authorData);

        authors.runWith(Sink.foreach(a -> System.out.println(a.handle)), system);
        tweets.runWith(Sink.foreach(t -> System.out.println(t.body)), system);
    }

    public static class Author {
        public final String handle;

        public Author(String handle) {
            this.handle = handle;
        }

        // ...

    }

    public static class Hashtag {
        public final String name;

        public Hashtag(String name) {
            this.name = name;
        }

        // ...
    }

    public static class Tweet {
        public final Author author;
        public final long timestamp;
        public final String body;

        public Tweet(Author author, long timestamp, String body) {
            this.author = author;
            this.timestamp = timestamp;
            this.body = body;
        }

        public Set<Hashtag> hashtags() {
            return Arrays.asList(body.split(" ")).stream()
                    .filter(a -> a.startsWith("#"))
                    .map(a -> new Hashtag(a))
                    .collect(Collectors.toSet());
        }

        // ...
    }

    public static final Hashtag AKKA = new Hashtag("#akka");
}
