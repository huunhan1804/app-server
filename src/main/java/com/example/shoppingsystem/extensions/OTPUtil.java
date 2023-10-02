package com.example.shoppingsystem.extensions;

import org.apache.commons.lang3.RandomStringUtils;

public class OTPUtil {
    public static String generateOTP(int length) {
        return RandomStringUtils.randomNumeric(length);
    }
}
