package prototypes.largeProcessor;

import akka.actor.typed.ActorRef;


public class IngestionModel {

    static interface RoomCommand {}

    static interface SessionEvent {}

    static interface SessionCommand {}

    public static final class GetSession implements RoomCommand {
        public final String screenName;
        public final ActorRef<SessionEvent> replyTo;

        public GetSession(String screenName, ActorRef<SessionEvent> replyTo) {
            this.screenName = screenName;
            this.replyTo = replyTo;
        }
    }

    public static final class PostMessage implements SessionCommand {
        public final String message;

        public PostMessage(String message) {
            this.message = message;
        }
    }

    public static final class SessionGranted implements SessionEvent {
        public final ActorRef<PostMessage> handle;

        public SessionGranted(ActorRef<PostMessage> handle) {
            this.handle = handle;
        }
    }

    public static final class SessionDenied implements SessionEvent {
        public final String reason;

        public SessionDenied(String reason) {
            this.reason = reason;
        }
    }

    public static final class MessagePosted implements SessionEvent {
        public final String screenName;
        public final String message;

        public MessagePosted(String screenName, String message) {
            this.screenName = screenName;
            this.message = message;
        }
    }

    public static final class NotifyClient implements SessionCommand {
        final MessagePosted message;

        NotifyClient(MessagePosted message) {
            this.message = message;
        }
    }
}
