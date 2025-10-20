package com.example.test_order_service.utils;

import java.time.LocalDate;

public class DateUtils {
    private static int calculateAge(LocalDate dateOfBirth) {
        LocalDate currentDate = LocalDate.now();
        return currentDate.getYear() - dateOfBirth.getYear();
    }
}
