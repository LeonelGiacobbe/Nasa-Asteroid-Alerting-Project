package com.leo.notificationservice.notificationservice.service;

import com.leo.notificationservice.asteroidalerting.event.AsteroidCollisionEvent;
import com.leo.notificationservice.notificationservice.entity.Notification;
import com.leo.notificationservice.notificationservice.repository.NotificationRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
@Slf4j
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final EmailService emailService;

    @Autowired
    public NotificationService(NotificationRepository notificationRepository, EmailService emailService) {
        this.notificationRepository = notificationRepository;
        this.emailService = emailService;
    }


    @KafkaListener(topics = "asteroid-alert", groupId = "notification-service")
    public void alertEvent(AsteroidCollisionEvent notificationEvent) {
        log.info("Received AsteroidCollisionEvent {}", notificationEvent);

        // create entity for notification
        final Notification notification = Notification.builder()
                .asteroidName(notificationEvent.getAsteroidName())
                .closeApproachDate(LocalDate.parse(notificationEvent.getCloseApproachDate()))
                .missDistanceKilometers(new BigDecimal(notificationEvent.getMissDistanceKilometers()))
                .estimatedDiameterAvgMeters(notificationEvent.getEstimatedDiameterAvgMeters())
                .emailSent(false)
                .build();

        // save to db
        final Notification savedNotification = notificationRepository.saveAndFlush(notification);
        log.info("Saved Notification {}", savedNotification);
    }

    @Scheduled(fixedRate = 10000) // every 10 seconds
    public void sendAlertingEmail() {
        log.info("Sending mail alerting email");
        emailService.sendAsteroidEmail();
    }
}
