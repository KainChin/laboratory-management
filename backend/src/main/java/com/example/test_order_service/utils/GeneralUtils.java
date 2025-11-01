package com.example.test_order_service.utils;

import java.time.LocalDate;

public class GeneralUtils {
    public static int calculateAge(LocalDate dateOfBirth) {
        LocalDate currentDate = LocalDate.now();
        return currentDate.getYear() - dateOfBirth.getYear();
    }

    public static String generateBloodCollectionId(long testOrderCount) {
        //Took the last count and add 1
        long count =  + 1;
        return String.format("ACC%07d", count);
    }
}
