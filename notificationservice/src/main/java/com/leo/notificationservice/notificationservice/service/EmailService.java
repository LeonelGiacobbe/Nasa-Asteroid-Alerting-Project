package com.leo.notificationservice.notificationservice.service;

import com.leo.notificationservice.notificationservice.entity.Notification;
import com.leo.notificationservice.notificationservice.repository.NotificationRepository;
import com.leo.notificationservice.notificationservice.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class EmailService {
    @Value("${email.service.from.email}") // in application.properties, address we use to send emails
    private String fromEmail;

    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;
    private final JavaMailSender mailSender;

    @Autowired
    public EmailService(UserRepository userRepository,
                          NotificationRepository notificationRepository,
                          JavaMailSender mailSender) {
        this.userRepository = userRepository;
        this.notificationRepository = notificationRepository;
        this.mailSender = mailSender;
    }

    @Async // run this method asynchronously, not blocking the main thread
    public void sendAsteroidEmail() {
        final String text = createEmailText();

        if (text == null) {
            log.info("No email to send");
            return;
        }

        final List <String> toEmails = userRepository.findAllEnabledUserEmails();
        if (toEmails.isEmpty()) {
            log.info("No users to send email to");
            return;
        }

        toEmails.forEach(email -> sendEmail(email, text));
        log.info("Email sent to: #{} users", toEmails.size());
    }

    public void sendEmail(final String toEmail, final String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("Asteroid Alert - " + LocalDateTime.now());
        message.setText(text);

        try {
            mailSender.send(message);
            log.info("Email sent to {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", toEmail, e.getMessage());
        }
    }


    public String createEmailText() {
        List<Notification> notificationList = notificationRepository.findByEmailSent(false);

        if (notificationList.isEmpty()) {
            log.info("No notifications to send");
            return null;
        }

        StringBuilder emailText = new StringBuilder();

        emailText.append("Asteroid Alert: \n");
        emailText.append("=====================================\n");

        notificationList.forEach(notification -> {
            emailText.append("Asteroid Name: ").append(notification.getAsteroidName()).append("\n");
            emailText.append("Close Approach Date: ").append(notification.getCloseApproachDate()).append("\n");
            emailText.append("Estimated Diameter Avg Meters: ").append(notification.getEstimatedDiameterAvgMeters()).append("\n");
            emailText.append("Miss Distance Kilometers: ").append(notification.getMissDistanceKilometers()).append("\n");
            emailText.append("=====================================\n");
            notification.setEmailSent(true);
            notificationRepository.save(notification);
        });

        return emailText.toString();
    }
}
