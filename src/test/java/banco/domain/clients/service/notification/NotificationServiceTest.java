package banco.domain.clients.service.notification;

import banco.domain.clients.model.Client;
import banco.domain.clients.model.notification.NotificationEvent;
import banco.domain.clients.model.notification.NotificationType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class NotificationServiceTest {

    private NotificationService notificationService;
    Client client = new Client(1L, "John Doe", "12345678A", "example.com");

    @BeforeEach
    void setUp() {
        notificationService = new NotificationService();
    }

    @Test
    void testSendNotification() {
        NotificationEvent event = new NotificationEvent(NotificationType.CREATE, client);

        // Envía una notificación
        notificationService.sendNotification(event);

        // Verifica que la notificación se recibe correctamente y luego se cancela el flujo
        StepVerifier.create(notificationService.getNotifications().take(1))  // Limitar a 1 evento
                .expectNext(event)
                .verifyComplete();  // Asegura que el flujo se complete
    }

    @Test
    void testSubscribe() {
        Consumer<NotificationEvent> subscriber = mock(Consumer.class);
        NotificationEvent event = new NotificationEvent(NotificationType.CREATE, client);

        // Suscribe el consumidor
        notificationService.subscribe(subscriber);

        // Envía la notificación
        notificationService.sendNotification(event);

        // Verifica que el subscriber haya sido llamado
        verify(subscriber, times(1)).accept(event);
    }

    @Test
    void testAutoSubscribeToConsole() {
        NotificationEvent event = new NotificationEvent(NotificationType.CREATE, client);

        // Usa un stub para la salida en consola
        notificationService.autoSubscribeToConsole();

        // Envía una notificación y verifica el flujo
        notificationService.sendNotification(event);

        StepVerifier.create(notificationService.getNotifications().take(1))
                .expectNext(event)
                .verifyComplete();  // Verifica que se complete después de 1 evento
    }

    @Test
    void testFluxSinkIsNullSafe() {
        // Verifica que no explote cuando fluxSink es null
        NotificationEvent event = new NotificationEvent(NotificationType.CREATE, client);
        NotificationService service = new NotificationService();
        service.sendNotification(event); // No debe lanzar una NullPointerException

        StepVerifier.create(service.getNotifications().take(1))
                .expectSubscription()
                .thenCancel()  // Cancela la suscripción para que no sea infinita
                .verify();
    }
}
