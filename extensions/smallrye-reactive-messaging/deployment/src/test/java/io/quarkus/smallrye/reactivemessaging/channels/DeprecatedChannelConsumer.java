package io.quarkus.smallrye.reactivemessaging.channels;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.eclipse.microprofile.reactive.messaging.Message;

import io.reactivex.Flowable;
import io.smallrye.reactive.messaging.annotations.Channel;

@ApplicationScoped
public class DeprecatedChannelConsumer {

    @Inject
    @Channel("source")
    Flowable<Message<String>> sourceStream;

    public List<String> consume() {
        return Flowable.fromPublisher(sourceStream)
                .map(Message::getPayload)
                .toList()
                .blockingGet();
    }

}
