package banco.domain.clients.notification;

import banco.domain.clients.notification.models.NotificationEvent;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class NotificationService {

    private final List<Consumer<NotificationEvent>> subscribers = new ArrayList<>();
    private Flux<NotificationEvent> flux;
    private FluxSink<NotificationEvent> fluxSink;

    public NotificationService() {
        this.flux = Flux.create(sink -> this.fluxSink = sink, FluxSink.OverflowStrategy.BUFFER);
    }


    public Flux<NotificationEvent> getNotifications() {
        return flux;
    }

    public void sendNotification(NotificationEvent event) {
        if (fluxSink != null) {
            fluxSink.next(event);
        }
    }


    public void subscribe(Consumer<NotificationEvent> subscriber) {
        subscribers.add(subscriber);
    }
}

