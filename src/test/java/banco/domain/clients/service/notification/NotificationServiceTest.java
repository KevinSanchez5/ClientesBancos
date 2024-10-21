package banco.domain.clients.service.notification;

import banco.domain.clients.model.Client;
import banco.domain.clients.model.notification.NotificationEvent;
import banco.domain.clients.model.notification.NotificationType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;


import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class NotificationServiceTest {
    private NotificationService notificationService;

    @BeforeEach
    void setUp() {
        notificationService = new NotificationService();
    }

//    @Test
//    void testSendNotificationWithCompletion() {
//        Client client = new Client(1L, "John Doe", "jdoe", "jdoe@example.com", null, null, null);
//        NotificationEvent event = new NotificationEvent(NotificationType.CREATE, client);
//
//        Flux<NotificationEvent> notifications = notificationService.getNotifications().take(1);
//
//        notificationService.sendNotification(event);
//
//        StepVerifier.create(notifications)
//                .expectNext(event)
//                .expectComplete()
//                .verify(Duration.ofSeconds(1));
//
//        notificationService.complete();
//    }

    @Test
    void testAutoSubscribeToConsole() {
        Client client = new Client(1L, "John Doe", "jdoe", "jdoe@example.com", null, null, null);
        NotificationEvent event = new NotificationEvent(NotificationType.CREATE, client);

        notificationService.autoSubscribeToConsole();

        notificationService.sendNotification(event);

        assertDoesNotThrow(() -> notificationService.sendNotification(event));
        notificationService.complete();
    }

//    @Test
//    void testMultipleNotifications() {
//        Client client1 = new Client(1L, "John Doe", "jdoe", "jdoe@example.com", null, null, null);
//        Client client2 = new Client(2L, "Jane Doe", "jadoe", "jadoe@example.com", null, null, null);
//        NotificationEvent event1 = new NotificationEvent(NotificationType.CREATE, client1);
//        NotificationEvent event2 = new NotificationEvent(NotificationType.UPDATE, client2);
//
//        Flux<NotificationEvent> notifications = notificationService.getNotifications().take(2);
//
//        notificationService.sendNotification(event1);
//        notificationService.sendNotification(event2);
//
//        StepVerifier.create(notifications)
//                .expectNext(event1)
//                .expectNext(event2)
//                .expectComplete()
//                .verify(Duration.ofSeconds(1));
//
//        notificationService.complete();
//    }
}

