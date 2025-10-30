package com.example.test_order_service.entity.enumForEntity;

public enum TestResultStatus {
    COMPLETED, //When the result has been entered but not yet reviewed
    REVIEWED, //When the result has been reviewed and finalized
    REJECTED //When the result has been rejected due to errors or inconsistencies
}
