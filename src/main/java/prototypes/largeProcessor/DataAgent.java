package prototypes.largeProcessor;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

public class DataAgent extends AbstractBehavior<ProcessData> {

    public DataAgent(ActorContext<ProcessData> context) {
        super(context);
    }

    public static Behavior<ProcessData> create() {
        return Behaviors.setup(DataAgent::new);
    }

    @Override
    public Receive<ProcessData> createReceive() {
        return newReceiveBuilder().onMessage(ProcessData.class, this::onStart).build();
    }

    private Behavior<ProcessData> onStart(ProcessData command) {
        String jsonData = command.getJsonData();
        jsonData += OffsetDateTime.now(ZoneOffset.UTC).toEpochSecond();
        getContext().getLog().info(jsonData);
        //command.replyTo.tell(new HelloWorld.Greeted(command.whom, getContext().getSelf()));
        return this;
    }
}
