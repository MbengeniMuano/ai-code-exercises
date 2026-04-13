package com.example.refactoring;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.*;

/**
 * Tests for the CustomerProcessorRefactored class
 * Demonstrates how decomposing the function makes integration testing more manageable
 */
@DisplayName("Customer Processor Refactored Tests")
public class CustomerProcessorRefactoredTest {
    private CustomerProcessorRefactored processor;
    private CustomerRepository mockRepository;
    private EmailValidator emailValidator;
    private PhoneValidator phoneValidator;
    private RecordTransformer recordTransformer;

    @BeforeEach
    public void setUp() {
        mockRepository = mock(CustomerRepository.class);
        emailValidator = new EmailValidator();
        phoneValidator = new PhoneValidator();
        recordTransformer = new RecordTransformer();

        processor = new CustomerProcessorRefactored(
            mockRepository,
            emailValidator,
            phoneValidator,
            recordTransformer
        );
    }

    @Test
    @DisplayName("Should reject null input data")
    public void testRejectNullInput() {
        CustomerProcessingOptions options = new CustomerProcessingOptions();

        Map<String, Object> result = processor.processCustomerData(null, "csv", options);

        assertEquals("error", result.get("status"));
        assertTrue(result.get("message").toString().contains("No data provided"));
    }

    @Test
    @DisplayName("Should reject empty input data")
    public void testRejectEmptyInput() {
        CustomerProcessingOptions options = new CustomerProcessingOptions();

        Map<String, Object> result = processor.processCustomerData(
            new ArrayList<>(),
            "csv",
            options
        );

        assertEquals("error", result.get("status"));
    }

    @Test
    @DisplayName("Should process valid records successfully")
    public void testProcessValidRecords() {
        List<Map<String, Object>> data = new ArrayList<>();
        Map<String, Object> record = new HashMap<>();
        record.put("firstName", "john");
        record.put("lastName", "doe");
        record.put("email", "john@example.com");
        record.put("phone", "5551234567");
        data.add(record);

        when(mockRepository.findAll()).thenReturn(new ArrayList<>());

        CustomerProcessingOptions options = new CustomerProcessingOptions()
            .withDeduplication(false)
            .withSaveToDatabase(false)
            .withIncludeValidRecords(true);

        Map<String, Object> result = processor.processCustomerData(data, "csv", options);

        assertEquals("success", result.get("status"));
        assertEquals(1, result.get("successCount"));
        assertEquals(0, result.get("errorCount"));
    }

    @Test
    @DisplayName("Should track invalid records")
    public void testTrackInvalidRecords() {
        List<Map<String, Object>> data = new ArrayList<>();

        // Record with missing required field
        Map<String, Object> record = new HashMap<>();
        record.put("email", "john@example.com");
        // Missing firstName and lastName
        data.add(record);

        when(mockRepository.findAll()).thenReturn(new ArrayList<>());

        CustomerProcessingOptions options = new CustomerProcessingOptions()
            .withDeduplication(false)
            .withSaveToDatabase(false)
            .withIncludeInvalidRecords(true);

        Map<String, Object> result = processor.processCustomerData(data, "csv", options);

        assertEquals("success", result.get("status"));
        assertEquals(0, result.get("successCount"));
        assertEquals(1, result.get("errorCount"));
    }

    @Test
    @DisplayName("Should detect duplicate emails")
    public void testDetectDuplicateEmails() {
        List<Map<String, Object>> data = new ArrayList<>();

        Map<String, Object> record1 = new HashMap<>();
        record1.put("firstName", "john");
        record1.put("lastName", "doe");
        record1.put("email", "john@example.com");
        data.add(record1);

        Map<String, Object> record2 = new HashMap<>();
        record2.put("firstName", "jane");
        record2.put("lastName", "smith");
        record2.put("email", "john@example.com"); // Duplicate
        data.add(record2);

        when(mockRepository.findAll()).thenReturn(new ArrayList<>());

        CustomerProcessingOptions options = new CustomerProcessingOptions()
            .withDeduplication(true)
            .withDuplicatesAsErrors(false)
            .withSaveToDatabase(false)
            .withIncludeDuplicateRecords(true);

        Map<String, Object> result = processor.processCustomerData(data, "csv", options);

        assertEquals("success", result.get("status"));
        assertEquals(1, result.get("successCount"));
        assertEquals(1, result.get("duplicateCount"));
    }

    @Test
    @DisplayName("Should save valid records to database")
    public void testSaveValidRecords() {
        List<Map<String, Object>> data = new ArrayList<>();
        Map<String, Object> record = new HashMap<>();
        record.put("firstName", "john");
        record.put("lastName", "doe");
        record.put("email", "john@example.com");
        data.add(record);

        when(mockRepository.findAll()).thenReturn(new ArrayList<>());

        CustomerProcessingOptions options = new CustomerProcessingOptions()
            .withDeduplication(false)
            .withSaveToDatabase(true);

        processor.processCustomerData(data, "csv", options);

        verify(mockRepository).saveAll(any());
    }

    @Test
    @DisplayName("Should handle database save errors gracefully")
    public void testHandleDatabaseError() {
        List<Map<String, Object>> data = new ArrayList<>();
        Map<String, Object> record = new HashMap<>();
        record.put("firstName", "john");
        record.put("lastName", "doe");
        record.put("email", "john@example.com");
        data.add(record);

        when(mockRepository.findAll()).thenReturn(new ArrayList<>());
        doThrow(new RuntimeException("Database error")).when(mockRepository).saveAll(any());

        CustomerProcessingOptions options = new CustomerProcessingOptions()
            .withDeduplication(false)
            .withSaveToDatabase(true);

        Map<String, Object> result = processor.processCustomerData(data, "csv", options);

        assertEquals("error", result.get("status"));
        assertTrue(result.get("message").toString().contains("Failed to save"));
    }

    @Test
    @DisplayName("Should respect max error threshold")
    public void testRespectMaxErrorThreshold() {
        List<Map<String, Object>> data = new ArrayList<>();

        // Add 5 invalid records
        for (int i = 0; i < 5; i++) {
            Map<String, Object> record = new HashMap<>();
            // Missing firstName (invalid)
            record.put("lastName", "doe");
            record.put("email", "john" + i + "@example.com");
            data.add(record);
        }

        when(mockRepository.findAll()).thenReturn(new ArrayList<>());

        // Set max error threshold to 2
        CustomerProcessingOptions options = new CustomerProcessingOptions()
            .withDeduplication(false)
            .withMaxErrorCount(2)
            .withSaveToDatabase(false);

        Map<String, Object> result = processor.processCustomerData(data, "csv", options);

        // Should stop after 2 errors
        assertTrue((int) result.get("skippedCount") > 0);
    }

    @Test
    @DisplayName("Should measure processing time")
    public void testProcessingTimeTracking() {
        List<Map<String, Object>> data = new ArrayList<>();
        Map<String, Object> record = new HashMap<>();
        record.put("firstName", "john");
        record.put("lastName", "doe");
        record.put("email", "john@example.com");
        data.add(record);

        when(mockRepository.findAll()).thenReturn(new ArrayList<>());

        CustomerProcessingOptions options = new CustomerProcessingOptions()
            .withDeduplication(false)
            .withSaveToDatabase(false);

        Map<String, Object> result = processor.processCustomerData(data, "csv", options);

        assertTrue(result.containsKey("processingTimeMs"));
        long processingTime = (long) result.get("processingTimeMs");
        assertTrue(processingTime >= 0);
    }

    @Test
    @DisplayName("Should return proper statistics")
    public void testProcessingStatistics() {
        List<Map<String, Object>> data = new ArrayList<>();

        // 2 valid records
        for (int i = 0; i < 2; i++) {
            Map<String, Object> record = new HashMap<>();
            record.put("firstName", "john");
            record.put("lastName", "doe");
            record.put("email", "john" + i + "@example.com");
            data.add(record);
        }

        // 1 invalid record (missing email)
        Map<String, Object> invalidRecord = new HashMap<>();
        invalidRecord.put("firstName", "jane");
        invalidRecord.put("lastName", "smith");
        data.add(invalidRecord);

        when(mockRepository.findAll()).thenReturn(new ArrayList<>());

        CustomerProcessingOptions options = new CustomerProcessingOptions()
            .withDeduplication(false)
            .withSaveToDatabase(false);

        Map<String, Object> result = processor.processCustomerData(data, "csv", options);

        assertEquals(3, result.get("totalRecords"));
        assertEquals(2, result.get("successCount"));
        assertEquals(1, result.get("errorCount"));
        assertEquals(3, result.get("processedCount"));
    }
}

