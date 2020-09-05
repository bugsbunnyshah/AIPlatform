package io.bugsbunny.data.streaming;

import akka.actor.typed.ActorSystem;
import java.io.IOException;

import org.junit.Test;

public class AkkaStreamsGetStartedTests {
    @Test
    public void testStart() throws Exception
    {
        //#actor-system
        final ActorSystem<GreeterMain.SayHello> greeterMain = ActorSystem.create(GreeterMain.create(), "helloakka");
        //#actor-system

        //#main-send-messages
        greeterMain.tell(new GreeterMain.SayHello("Charles"));
        //#main-send-messages

        try {
            System.out.println(">>> Press ENTER to exit <<<");
            System.in.read();
        } catch (IOException ignored) {
        } finally {
            greeterMain.terminate();
        }
    }
}
