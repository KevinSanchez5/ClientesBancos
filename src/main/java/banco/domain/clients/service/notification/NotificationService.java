package banco.domain.clients.service.notification;

import banco.domain.clients.notification.models.NotificationEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import banco.domain.clients.model.notification.NotificationEvent;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class NotificationService {

    private final List<Consumer<NotificationEvent>> subscribers = new ArrayList<>();
    private Flux<NotificationEvent> flux;
    private FluxSink<NotificationEvent> fluxSink;
    private Logger log = LoggerFactory.getLogger(NotificationService.class);

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

    public void autoSubscribeToConsole() {
        this.flux.subscribe(event -> {
            log.info("✉ Notificacion: " + event.toString());
        });
    }
}

