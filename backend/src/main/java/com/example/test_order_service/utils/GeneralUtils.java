package com.example.test_order_service.utils;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class GeneralUtils {
    public static int calculateAge(LocalDate dateOfBirth) {
        LocalDate currentDate = LocalDate.now();
        return currentDate.getYear() - dateOfBirth.getYear();
    }

    public static String generateBloodCollectionId(long testOrderCount) {
        //Took the last count and add 1
        long count = testOrderCount + 1;
        String datePart = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        return String.format("BCT-%s-%05d", datePart, count);
    }

    public static String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.getPrincipal() instanceof Jwt jwt) {
            String userName = jwt.getClaim("userName");
            if (userName != null) {
                return userName;
            }
        }

        //Fallback to "System" if username is not found
        return "System";
    }
}
