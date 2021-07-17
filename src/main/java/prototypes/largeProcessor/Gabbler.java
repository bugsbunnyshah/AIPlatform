package prototypes.largeProcessor;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.*;

public class Gabbler extends AbstractBehavior<IngestionModel.SessionEvent> {
    public static Behavior<IngestionModel.SessionEvent> create() {
        return Behaviors.setup(Gabbler::new);
    }

    private Gabbler(ActorContext<IngestionModel.SessionEvent> context) {
        super(context);
    }

    @Override
    public Receive<IngestionModel.SessionEvent> createReceive() {
        ReceiveBuilder<IngestionModel.SessionEvent> builder = newReceiveBuilder();
        return builder
                .onMessage(IngestionModel.SessionDenied.class, this::onSessionDenied)
                .onMessage(IngestionModel.SessionGranted.class, this::onSessionGranted)
                .onMessage(IngestionModel.MessagePosted.class, this::onMessagePosted)
                .build();
    }

    private Behavior<IngestionModel.SessionEvent> onSessionDenied(IngestionModel.SessionDenied message) {
        getContext().getLog().info("cannot start chat room session: {}", message.reason);
        return Behaviors.stopped();
    }

    private Behavior<IngestionModel.SessionEvent> onSessionGranted(IngestionModel.SessionGranted message) {
        message.handle.tell(new IngestionModel.PostMessage("Hello World!"));
        return Behaviors.same();
    }

    private Behavior<IngestionModel.SessionEvent> onMessagePosted(IngestionModel.MessagePosted message) {
        getContext()
                .getLog()
                .info("message has been posted by '{}': {}", message.screenName, message.message);
        return Behaviors.stopped();
    }
}
