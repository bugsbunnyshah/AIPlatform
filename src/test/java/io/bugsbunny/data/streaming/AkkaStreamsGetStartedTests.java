package io.bugsbunny.data.streaming;

import akka.Done;
import akka.NotUsed;
import akka.actor.*;
import akka.dispatch.Dispatchers;
import akka.dispatch.Mailboxes;
import akka.event.EventStream;
import akka.event.LoggingAdapter;
import akka.stream.javadsl.Source;
import akka.util.ByteString;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.Function0;
import scala.collection.Iterable;
import scala.concurrent.ExecutionContextExecutor;
import scala.concurrent.Future;

import java.nio.file.Paths;
import java.math.BigInteger;
import java.time.Duration;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;


@QuarkusTest
public class AkkaStreamsGetStartedTests {
    private static Logger logger = LoggerFactory.getLogger(AkkaStreamsGetStartedTests.class);

    //@Test
    public void testStart() throws Exception
    {
        final ActorSystem system = new ActorSystem() {
            @Override
            public Future<Terminated> whenTerminated() {
                return null;
            }

            @Override
            public <T> void registerOnTermination(Function0<T> code) {

            }

            @Override
            public void registerOnTermination(Runnable code) {

            }

            @Override
            public boolean hasExtension(ExtensionId<? extends Extension> ext) {
                return false;
            }

            @Override
            public <T extends Extension> T registerExtension(ExtensionId<T> ext) {
                return null;
            }

            @Override
            public Future<Terminated> terminate() {
                return null;
            }

            @Override
            public void logConfiguration() {

            }

            @Override
            public CompletionStage<Terminated> getWhenTerminated() {
                return null;
            }

            @Override
            public LoggingAdapter log() {
                return null;
            }

            @Override
            public EventStream eventStream() {
                return null;
            }

            @Override
            public <T extends Extension> T extension(ExtensionId<T> ext) {
                return null;
            }

            @Override
            public ActorPath $div(String name) {
                return null;
            }

            @Override
            public ActorPath $div(Iterable<String> name) {
                return null;
            }

            @Override
            public Scheduler scheduler() {
                return null;
            }

            @Override
            public ExecutionContextExecutor dispatcher() {
                return null;
            }

            @Override
            public Settings settings() {
                return null;
            }

            @Override
            public ActorRef deadLetters() {
                return null;
            }

            @Override
            public Mailboxes mailboxes() {
                return null;
            }

            @Override
            public String name() {
                return null;
            }

            @Override
            public Dispatchers dispatchers() {
                return null;
            }

            @Override
            public ActorRef actorOf(Props props) {
                return null;
            }

            @Override
            public ActorRef actorOf(Props props, String name) {
                return null;
            }

            @Override
            public ActorSystemImpl systemImpl() {
                return null;
            }

            @Override
            public void stop(ActorRef actor) {

            }

            @Override
            public ActorRefProvider provider() {
                return null;
            }

            @Override
            public InternalActorRef guardian() {
                return null;
            }

            @Override
            public InternalActorRef lookupRoot() {
                return null;
            }

            @Override
            public ActorSystem classicSystem() {
                return null;
            }
        };

        final Source<Integer, NotUsed> source = Source.range(1, 100);
        //source.runForeach(i -> System.out.println(i), system);
    }
}
