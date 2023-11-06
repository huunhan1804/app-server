package com.example.shoppingsystem.constants;

import com.example.shoppingsystem.dtos.AddressInfoDTO;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.Month;
import java.util.Date;
import java.util.Locale;
import java.sql.Timestamp;
import java.time.LocalDateTime;

public class Regex {
    public static final String SECRET_KEY = "68576D5A7134743777217A25432A462D4A614E645267556A586E327235753878";
    public static final long ACCESS_TOKEN_EXPIRATION_TIME = 3600000000000L; // 1 giờ (60 phút)
    public static final long REFRESH_TOKEN_EXPIRATION_TIME = 604800000000000L; // 7 ngày
    public static final String DATE_SQL_PATTERN = "yyyy-MM-dd";
    public static final String BASE_URL_AUTHENTICATE_WITH_GOOGLE = "https://oauth2.googleapis.com/tokeninfo?id_token=";
    public static final String URL_IMAGE_DEFAULT = "https://cdn1.vectorstock.com/i/1000x1000/01/35/candidate-person-icon-cartoon-employee-job-vector-39260135.jpg";
    public static final int DELAY_BETWEEN_REQUEST_SEND_OTP = 1;
    public static final int LENGTH_CODE_OTP = 6;
    public static final String SUBJECT_EMAIL_PASSWORD = "Password Recovery";
    public static final LocalDate BIRTHDATE_DEFAULT = LocalDate.of(1999, Month.DECEMBER, 31);
    public static final String GENDER_MALE = "Male";
    public static final String GENDER_FEMALE = "Female";
    public static final String SUBJECT_EMAIL_OTP = "OTP Verification";

    public static boolean isValidEmail(String email) {
        String emailRegex = "^([a-zA-Z0-9._%-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,})$";
        return email.matches(emailRegex);
    }

    public static boolean isValidPhoneNumber(String phoneNumber) {
        String phoneRegex = "^[0-9]{10}$";
        return phoneNumber.matches(phoneRegex);
    }

    public static String formatPriceToVND(BigDecimal price) {
        NumberFormat numberFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        return numberFormat.format(price);
    }

    public static BigDecimal parseVNDToBigDecimal(String price) {
        try {
            String normalized = price.replaceAll("[^\\d]", ""); // Remove all non-digit characters
            return new BigDecimal(normalized);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid VND format");
        }
    }


    public static Date convertLocalDateTimeToDate(LocalDateTime localDateTime) {
        return Timestamp.valueOf(localDateTime);
    }

    public static AddressInfoDTO parseShippingInfo(String shippingInfo) {
        AddressInfoDTO addressInfoDTO = new AddressInfoDTO();
        String[] lines = shippingInfo.split("\n");
        for (String line : lines) {
            if (line.startsWith("Name: ")) {
                addressInfoDTO.setFullname(line.replace("Name: ", "").trim());
            } else if (line.startsWith("Phone: ")) {
                addressInfoDTO.setPhone(line.replace("Phone: ", "").trim());
            } else if (line.startsWith("Address: ")) {
                addressInfoDTO.setAddress_detail(line.replace("Address: ", "").trim());
            }
        }
        addressInfoDTO.setAddress_id(0L);
        addressInfoDTO.set_default(false);
        return addressInfoDTO;
    }



}
