package com.kirylliuss.telegram.bot.transportBot.mail;

import jakarta.annotation.PostConstruct;
import lombok.Data;
import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Properties;

@Data
@Component
public class SendEmail {

    @Value("${mail.to}")
    String to;

    @Value("${mail.from}")
    String from;

    @Value("${mail.username}")
    String username;

    @Value("${mail.pass}")
    String password;

    @Value("${mail.smtp.host}")
    String host;

    @Value("${mail.smtp.port}")
    String port;

    private Properties props;

    private Session session;

    @PostConstruct
    private void init() {
        props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", port);
    }

    private void startSession(){
        Session session = Session.getInstance(props,
                new Authenticator() {
                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(username, password);
                    }
                });
        this.session = session;
    }

    public void sendMessage(String text){
        init();
        startSession();
        try{
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(from));
            message.setRecipient(Message.RecipientType.TO, new InternetAddress(to));
            message.setSubject("Отзыв");
            message.setText(text);

            Transport.send(message);

            System.out.println("Message sent.");
        } catch (Exception ex){
            ex.printStackTrace();
        }
    }
}
