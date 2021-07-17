package prototypes.largeProcessor;

// #imports
import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import akka.actor.typed.javadsl.ReceiveBuilder;

// #imports

import akka.actor.typed.Terminated;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;


public class ChatRoom {
    private static final class PublishSessionMessage implements IngestionModel.RoomCommand {
        public final String screenName;
        public final String message;

        public PublishSessionMessage(String screenName, String message) {
            this.screenName = screenName;
            this.message = message;
        }
    }

    public static Behavior<IngestionModel.RoomCommand> create() {
        return Behaviors.setup(ChatRoomBehavior::new);
    }

    public static class ChatRoomBehavior extends AbstractBehavior<IngestionModel.RoomCommand> {
        final List<ActorRef<IngestionModel.SessionCommand>> sessions = new ArrayList<>();

        private ChatRoomBehavior(ActorContext<IngestionModel.RoomCommand> context) {
            super(context);
        }

        @Override
        public Receive<IngestionModel.RoomCommand> createReceive() {
            ReceiveBuilder<IngestionModel.RoomCommand> builder = newReceiveBuilder();

            builder.onMessage(IngestionModel.GetSession.class, this::onGetSession);
            builder.onMessage(PublishSessionMessage.class, this::onPublishSessionMessage);

            return builder.build();
        }

        private Behavior<IngestionModel.RoomCommand> onGetSession(IngestionModel.GetSession getSession)
                throws UnsupportedEncodingException {
            ActorRef<IngestionModel.SessionEvent> client = getSession.replyTo;
            ActorRef<IngestionModel.SessionCommand> ses =
                    getContext()
                            .spawn(
                                    SessionBehavior.create(getContext().getSelf(), getSession.screenName, client),
                                    URLEncoder.encode(getSession.screenName, StandardCharsets.UTF_8.name()));
            // narrow to only expose PostMessage
            client.tell(new IngestionModel.SessionGranted(ses.narrow()));
            sessions.add(ses);
            return this;
        }

        private Behavior<IngestionModel.RoomCommand> onPublishSessionMessage(PublishSessionMessage pub) {
            IngestionModel.NotifyClient notification =
                    new IngestionModel.NotifyClient((new IngestionModel.MessagePosted(pub.screenName, pub.message)));
            sessions.forEach(s -> s.tell(notification));
            return this;
        }
    }

    static class SessionBehavior extends AbstractBehavior<IngestionModel.SessionCommand> {
        private final ActorRef<IngestionModel.RoomCommand> room;
        private final String screenName;
        private final ActorRef<IngestionModel.SessionEvent> client;

        public static Behavior<IngestionModel.SessionCommand> create(
                ActorRef<IngestionModel.RoomCommand> room, String screenName, ActorRef<IngestionModel.SessionEvent> client) {
            return Behaviors.setup(context -> new SessionBehavior(context, room, screenName, client));
        }

        private SessionBehavior(
                ActorContext<IngestionModel.SessionCommand> context,
                ActorRef<IngestionModel.RoomCommand> room,
                String screenName,
                ActorRef<IngestionModel.SessionEvent> client) {
            super(context);
            this.room = room;
            this.screenName = screenName;
            this.client = client;
        }

        @Override
        public Receive<IngestionModel.SessionCommand> createReceive() {
            return newReceiveBuilder()
                    .onMessage(IngestionModel.PostMessage.class, this::onPostMessage)
                    .onMessage(IngestionModel.NotifyClient.class, this::onNotifyClient)
                    .build();
        }

        private Behavior<IngestionModel.SessionCommand> onPostMessage(IngestionModel.PostMessage post) {
            // from client, publish to others via the room
            room.tell(new PublishSessionMessage(screenName, post.message));
            return Behaviors.same();
        }

        private Behavior<IngestionModel.SessionCommand> onNotifyClient(IngestionModel.NotifyClient notification) {
            // published from the room
            client.tell(notification.message);
            return Behaviors.same();
        }
    }
}
