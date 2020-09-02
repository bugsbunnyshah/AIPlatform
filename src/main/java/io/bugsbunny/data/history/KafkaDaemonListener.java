package io.bugsbunny.data.history;

public interface KafkaDaemonListener {
    public void receiveNotifications(MessageWindow messageWindow);
}
