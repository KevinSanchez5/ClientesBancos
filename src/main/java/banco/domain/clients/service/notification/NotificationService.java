package banco.domain.clients.service.notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import banco.domain.clients.model.notification.NotificationEvent;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Servicio de notificaciones
 */
public class NotificationService {

    private final List<Consumer<NotificationEvent>> subscribers = new ArrayList<>();
    private Flux<NotificationEvent> flux;
    private FluxSink<NotificationEvent> fluxSink;
    private Logger log = LoggerFactory.getLogger(NotificationService.class);

    public NotificationService() {
        this.flux = Flux.create(sink -> this.fluxSink = sink, FluxSink.OverflowStrategy.BUFFER);
    }

    /**
     * Obtiene las notificaciones
     * @return Flux de notificaciones
     */
    public Flux<NotificationEvent> getNotifications() {
        return flux;
    }

    /**
     * Envía una notificación
     * @param event Evento de notificación
     */
    public void sendNotification(NotificationEvent event) {
        if (fluxSink != null) {
            fluxSink.next(event);
        }
    }

    /**
     * Suscribe un consumidor a las notificaciones
     * @param subscriber Consumidor
     */
    public void subscribe(Consumer<NotificationEvent> subscriber) {
        subscribers.add(subscriber);
    }

    /**
     * Suscribe automáticamente
     * por cada notificacion se muestra en consola
     */
    public void autoSubscribeToConsole() {
        this.flux.subscribe(event -> {
            log.info("✉ Notificacion: " + event.toString());
        });
    }

    /**
     * Metodo para finalizar el flujo de notificaciones
     */
    public void complete() {
        if (fluxSink != null) {
            fluxSink.complete();
        }
    }
}

