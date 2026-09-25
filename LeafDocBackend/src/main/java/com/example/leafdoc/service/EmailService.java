package com.example.leafdoc.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

@Service
public class EmailService {
    private final JavaMailSender mailSender;
    private final SpringTemplateEngine templateEngine; //thymleaf

    public EmailService(JavaMailSender mailSender, SpringTemplateEngine templateEngine) {
        this.mailSender = mailSender;
        this.templateEngine =  templateEngine;
    }

    private void tutorial() throws MessagingException {
        new SimpleMailMessage();  // only simple text emails.

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper =
                new MimeMessageHelper(message, true);
        // it throws exception
        // with thymleaf ir can read html emails from resources folder and send them.

        ///////////////////
        //todo: Use spring events
        /// direct call of email service, the mail services may face delay because of smtp.


    }

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
