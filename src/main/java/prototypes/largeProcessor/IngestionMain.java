package prototypes.largeProcessor;

import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.Behavior;
import akka.actor.typed.Terminated;
import akka.actor.typed.javadsl.Behaviors;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class IngestionMain {
    public static Behavior<Void> create() {
        return Behaviors.setup(
                context -> {
                    ActorRef<IngestionModel.RoomCommand> chatRoom = context.spawn(ChatRoom.create(), "chatRoom");
                    String name = UUID.randomUUID().toString();
                    ActorRef<IngestionModel.SessionEvent> gabbler = context.spawn(Gabbler.create(), name);
                    context.watch(gabbler);
                    chatRoom.tell(new IngestionModel.GetSession(name, gabbler));


                    return Behaviors.receive(Void.class)
                            .onSignal(Terminated.class, sig -> Behaviors.stopped())
                            .build();
                });
    }

    public static void main(String[] args) {
        ActorSystem.create(IngestionMain.create(), "ChatRoomDemo");
    }
}
