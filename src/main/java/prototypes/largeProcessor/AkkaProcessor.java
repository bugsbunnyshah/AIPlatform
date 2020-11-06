package prototypes.largeProcessor;

import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AkkaProcessor {
    private static Logger logger = LoggerFactory.getLogger(AkkaProcessor.class);

    public static void main(String[] args) throws Exception
    {
        logger.info("******AKKAPROCESSOR********");
        ActorRef<String> testSystem = ActorSystem.create(Main.create(), "testSystem");
        testSystem.tell("start");
    }

    static class PrintMyActorRefActor extends AbstractBehavior<String> {

        static Behavior<String> create() {
            return Behaviors.setup(PrintMyActorRefActor::new);
        }

        private PrintMyActorRefActor(ActorContext<String> context) {
            super(context);
        }

        @Override
        public Receive<String> createReceive() {
            return newReceiveBuilder().onMessageEquals("printit", this::printIt).build();
        }

        private Behavior<String> printIt() {
            ActorRef<String> secondRef = getContext().spawn(Behaviors.empty(), "second-actor");
            System.out.println("Second: " + secondRef);
            return this;
        }
    }

    static class Main extends AbstractBehavior<String> {

        static Behavior<String> create() {
            return Behaviors.setup(Main::new);
        }

        private Main(ActorContext<String> context) {
            super(context);
        }

        @Override
        public Receive<String> createReceive() {
            return newReceiveBuilder().onMessageEquals("start", this::start).build();
        }

        private Behavior<String> start() {
            ActorRef<String> firstRef = getContext().spawn(PrintMyActorRefActor.create(), "first-actor");

            System.out.println("First: " + firstRef);
            firstRef.tell("printit");
            return Behaviors.same();
        }
    }
}
