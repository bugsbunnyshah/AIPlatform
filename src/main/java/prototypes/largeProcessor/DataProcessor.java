package prototypes.largeProcessor;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

import java.util.UUID;

public class DataProcessor extends AbstractBehavior<ProcessData> {
    private ActorRef<ProcessData> dataAgent;
    private int max;
    private int dataCounter;

    public DataProcessor(ActorContext<ProcessData> context,int max) {
        super(context);
        this.max = max;
        this.dataCounter = 0;
        this.dataAgent = context.spawn(DataAgent.create(), "DataAgent1");
    }

    public static Behavior<ProcessData> create(int max) {
        return Behaviors.setup(context -> new DataProcessor(context, max));
    }

    @Override
    public Receive<ProcessData> createReceive() {
        return newReceiveBuilder().onMessage(ProcessData.class, this::onStart).build();
    }

    private Behavior<ProcessData> onStart(ProcessData command) {
        System.out.println("DATA_PROCESSOR_STARTED....");
        if(this.dataCounter >= this.max) {
            System.out.println("STOPPING");
            return Behaviors.stopped();
        }

        String actorName = UUID.randomUUID().toString();
        command.setCaller(null);
        this.dataAgent.tell(command);
        this.dataCounter++;
        return this;
    }
}
