package com.abe.e_likizo.service;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    public void sendLoginCredentials(
            String email,
            String fullName,
            String password
    ) {

        SimpleMailMessage message = new SimpleMailMessage();

        message.setTo(email);
        message.setSubject("E-Likizo Account Credentials");

        message.setText(
                "Dear " + fullName + ",\n\n" +

                        "Your E-Likizo account has been created successfully.\n\n" +

                        "Username: " + email + "\n" +
                        "Temporary Password: " + password + "\n\n" +

                        "Please use these credentials to login to the E-Likizo system.\n" +
                        "You can change your password after logging in.\n\n" +

                        "Regards,\n" +
                        "E-Likizo Administration"
        );

        mailSender.send(message);
    }


    public void sendPasswordResetLink(
            String email,
            String fullName,
            String resetLink) {

        SimpleMailMessage message = new SimpleMailMessage();

        message.setTo(email);
        message.setSubject("e-Likizo - Password Recovery");

        message.setText(
                "Habari " + fullName + ",\n\n"
                        + "Tumepokea ombi la kubadilisha password yako ya e-Likizo.\n\n"
                        + "Fungua link ifuatayo ili kuweka password mpya:\n"
                        + resetLink + "\n\n"
                        + "Link hii itaisha baada ya dakika 15 na inaweza kutumika mara moja tu.\n"
                        + "Ikiwa hukuomba kubadilisha password, puuza ujumbe huu.\n\n"
                        + "e-Likizo System"
        );

        mailSender.send(message);
    }

}
