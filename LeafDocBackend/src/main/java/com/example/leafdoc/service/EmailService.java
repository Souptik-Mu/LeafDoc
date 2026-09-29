package com.example.leafdoc.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

@Service
@RequiredArgsConstructor
public class EmailService {
    private final JavaMailSender mailSender;
    private final SpringTemplateEngine templateEngine; //thymleaf

//    public EmailService(JavaMailSender mailSender, SpringTemplateEngine templateEngine) {
//        this.mailSender = mailSender;
//        this.templateEngine =  templateEngine;
//        /*todo: i can just do @RequiredArgsConstructor for auto constructor injection */
//    }

    @Async("emailExecutor")
    public void sendVarificationEmail(String to, String user, String link) {
        Context ctx = new Context();
        ctx.setVariable("name", user);
        ctx.setVariable("verificationLink", link);

        String html = templateEngine.process("emails/verification-email", ctx);
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(to);
            helper.setSubject("Verify Email");
            helper.setText(html, true);

            mailSender.send(message);

        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }

    @Async("emailExecutor")
    public void sendPasswordResetEmail(String to){
        try {

            MimeMessage message = mailSender.createMimeMessage();

            MimeMessageHelper helper =
                    new MimeMessageHelper(message, true);
            helper.setTo(to);
            helper.setSubject("Some test random Email");
            helper.setText(
                    "<html> put html here</html>",
                    true);

            mailSender.send(message);
        }
        catch (MessagingException e) {

            throw new RuntimeException(e);

        }
    }
    @Async("emailExecutor")
    public void sendDummyEmail(String to){
        Context ctx = new Context();
        ctx.setVariable("name", "Random value name");

        String html = templateEngine.process("emails/dummy", ctx); // leading dummy.html, and ctx holds dynamic parts
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(to);
            helper.setSubject("Some test random Email");
            helper.setText(html, true);

            mailSender.send(message);

        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }


}
