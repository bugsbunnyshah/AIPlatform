package prototypes.largeProcessor;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.SupervisorStrategy;
import akka.actor.typed.javadsl.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public class DataProcessor extends AbstractBehavior<ProcessData> {
    private static Logger logger = LoggerFactory.getLogger(DataProcessor.class);
    private ActorRef<ProcessData> router;

    public DataProcessor(ActorContext<ProcessData> context,int max) {
        super(context);
    }

    public static Behavior<ProcessData> create(int max) {
        return Behaviors.setup(context -> {
            DataProcessor dataProcessor = new DataProcessor(context,max);
            int poolSize = 4;
            PoolRouter<ProcessData> pool =
                    Routers.pool(
                            poolSize,
                            // make sure the workers are restarted if they fail
                            Behaviors.supervise(DataAgent.create()).onFailure(SupervisorStrategy.restart()));

            dataProcessor.router = context.spawn(pool, "worker-pool");
            return dataProcessor;
        });
    }

    @Override
    public Receive<ProcessData> createReceive() {
        return newReceiveBuilder().onMessage(ProcessData.class, this::onStart).build();
    }

    private Behavior<ProcessData> onStart(ProcessData command) {
        String actorName = UUID.randomUUID().toString();
        command.setCaller(null);

        this.router.tell(command);
        return this;
    }
}
