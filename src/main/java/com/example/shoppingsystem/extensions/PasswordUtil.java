package com.example.shoppingsystem.extensions;

import org.apache.commons.lang3.RandomStringUtils;

public class PasswordUtil {
    public static String generateRandomPassword() {
        String uppercaseLetters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String lowercaseLetters = "abcdefghijklmnopqrstuvwxyz";
        String numbers = "0123456789";
        String specialChars = "!@#$%^&*()_+-=[]|,./?><";

        return RandomStringUtils.random(1, uppercaseLetters)
                + RandomStringUtils.random(1, lowercaseLetters)
                + RandomStringUtils.random(1, numbers)
                + RandomStringUtils.random(1, specialChars)
                + RandomStringUtils.randomAlphanumeric(6);
    }
}
