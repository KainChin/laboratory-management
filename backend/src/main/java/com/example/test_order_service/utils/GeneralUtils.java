package com.example.test_order_service.utils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class GeneralUtils {
    public static int calculateAge(LocalDate dateOfBirth) {
        LocalDate currentDate = LocalDate.now();
        return currentDate.getYear() - dateOfBirth.getYear();
    }

    public static String generateBloodCollectionId(long testOrderCount) {
        //Took the last count and add 1
        long count =  + 1;
        String datePart = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        return String.format("BCT-%s-%07d", datePart, count);
    }
}
