package prototypes.largeProcessor;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

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
        getContext().getLog().info("JSON: "+command.getJsonData());
        //command.replyTo.tell(new HelloWorld.Greeted(command.whom, getContext().getSelf()));
        return this;
    }
}
