package com.abe.e_likizo.service;

import org.springframework.stereotype.Service;

import java.security.SecureRandom;

@Service
public class PasswordGeneratorService {

    private static final String CHARACTERS =
            "ABCDEFGHIJKLMNOPQRSTUVWXYZ" +
                    "abcdefghijklmnopqrstuvwxyz" +
                    "0123456789" +
                    "@#$%&*";

    private final SecureRandom random = new SecureRandom();

    public String generatePassword(int length) {

        StringBuilder password = new StringBuilder(length);

        for (int i = 0; i < length; i++) {

            int index = random.nextInt(CHARACTERS.length());

            password.append(CHARACTERS.charAt(index));
        }

        return password.toString();
    }
}
