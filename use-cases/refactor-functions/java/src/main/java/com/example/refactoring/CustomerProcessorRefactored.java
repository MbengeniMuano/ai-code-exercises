package com.example.refactoring;

import java.util.*;
import java.util.logging.Logger;

/**
 * REFACTORED VERSION: Decomposed customer data processor
 *
 * This refactored version demonstrates how to break down a complex function into
 * smaller, focused functions that each have a single responsibility. The original
 * monolithic processCustomerData() method has been decomposed into:
 *
 * 1. Orchestration logic (processCustomerData) - delegates to specialized handlers
 * 2. Data validation (validateRecord) - validates individual records
 * 3. Deduplication (checkDuplicates) - handles duplicate detection
 * 4. Data transformation (transformRecord) - applies business logic transformations
 * 5. Field-specific validation (validateEmail, validatePhone) - focused validators
 * 6. Report generation (buildProcessingReport) - creates the result summary
 * 7. Database persistence (persistValidRecords) - handles saving to database
 *
 * Benefits of this approach:
 * - Each function has a single, clear responsibility
 * - Functions are independently testable
 * - Code is more readable and maintainable
 * - Changes to one concern don't affect others
 * - Functions are more reusable
 */
public class CustomerProcessorRefactored {
    private static final Logger logger = Logger.getLogger(CustomerProcessorRefactored.class.getName());
    private final CustomerRepository customerRepository;
    private final EmailValidator emailValidator;
    private final PhoneValidator phoneValidator;
    private final RecordTransformer recordTransformer;

    // Constructor with dependency injection
    public CustomerProcessorRefactored(CustomerRepository customerRepository,
                                      EmailValidator emailValidator,
                                      PhoneValidator phoneValidator,
                                      RecordTransformer recordTransformer) {
        this.customerRepository = customerRepository;
        this.emailValidator = emailValidator;
        this.phoneValidator = phoneValidator;
        this.recordTransformer = recordTransformer;
    }

    /**
     * Main orchestration method - coordinates the processing workflow
     * This is much simpler than the original and delegates to specialized handlers
     */
    public Map<String, Object> processCustomerData(List<Map<String, Object>> rawData,
                                                   String source,
                                                   CustomerProcessingOptions options) {
        long startTime = System.currentTimeMillis();

        // Validate input
        if (!isValidInput(rawData)) {
            return createErrorResult("No data provided for processing");
        }

        logger.info("Starting to process " + rawData.size() + " customer records from source: " + source);

        // Load existing customers for deduplication
        Set<String> existingEmails = new HashSet<>();
        Set<String> existingPhones = new HashSet<>();
        if (options.isPerformDeduplication()) {
            try {
                loadExistingCustomersForDeduplication(existingEmails, existingPhones);
            } catch (Exception e) {
                return createErrorResult("Failed to load existing customers for deduplication", e);
            }
        }

        // Process all records
        ProcessingResult processingResult = processAllRecords(rawData, source, options,
                                                             existingEmails, existingPhones);

        // Persist valid records if requested
        if (options.isSaveToDatabase() && !processingResult.getValidRecords().isEmpty()) {
            try {
                persistValidRecords(processingResult.getValidRecords(), source);
            } catch (Exception e) {
                return createErrorResult("Failed to save valid records to database", e);
            }
        }

        // Build and return the final report
        long endTime = System.currentTimeMillis();
        return buildProcessingReport(processingResult, rawData.size(),
                                    endTime - startTime, source, options);
    }

    /**
     * Validates that the input data is acceptable
     */
    private boolean isValidInput(List<Map<String, Object>> rawData) {
        return rawData != null && !rawData.isEmpty();
    }

    /**
     * Loads all existing customers to support deduplication checking
     */
    private void loadExistingCustomersForDeduplication(Set<String> existingEmails,
                                                       Set<String> existingPhones) {
        List<Customer> existingCustomers = customerRepository.findAll();
        for (Customer customer : existingCustomers) {
            if (customer.getEmail() != null && !customer.getEmail().isEmpty()) {
                existingEmails.add(customer.getEmail().toLowerCase());
            }
            if (customer.getPhoneNumber() != null && !customer.getPhoneNumber().isEmpty()) {
                existingPhones.add(PhoneValidator.normalize(customer.getPhoneNumber()));
            }
        }
        logger.info("Loaded " + existingCustomers.size() + " existing customers for deduplication");
    }

    /**
     * Processes all records in the input data
     */
    private ProcessingResult processAllRecords(List<Map<String, Object>> rawData,
                                              String source,
                                              CustomerProcessingOptions options,
                                              Set<String> existingEmails,
                                              Set<String> existingPhones) {
        ProcessingResult result = new ProcessingResult();
        int totalProcessed = 0;
        int totalSkipped = 0;

        for (Map<String, Object> record : rawData) {
            totalProcessed++;

            // Check if we've hit the error limit
            if (shouldStopProcessing(options, result.getTotalErrors())) {
                totalSkipped = rawData.size() - totalProcessed + 1;
                logger.warning("Maximum error threshold reached. Skipping remaining records.");
                break;
            }

            // Process individual record
            processIndividualRecord(record, source, options, result, existingEmails, existingPhones);
        }

        result.setTotalProcessed(totalProcessed);
        result.setTotalSkipped(totalSkipped);
        return result;
    }

    /**
     * Determines if processing should stop based on error limits
     */
    private boolean shouldStopProcessing(CustomerProcessingOptions options, int currentErrors) {
        return options.getMaxErrorCount() > 0 && currentErrors >= options.getMaxErrorCount();
    }

    /**
     * Processes a single record through the validation and transformation pipeline
     */
    private void processIndividualRecord(Map<String, Object> record,
                                        String source,
                                        CustomerProcessingOptions options,
                                        ProcessingResult result,
                                        Set<String> existingEmails,
                                        Set<String> existingPhones) {
        try {
            // Apply source-specific preprocessing
            Map<String, Object> processedRecord = recordTransformer.transform(record, source);

            // Validate the record
            RecordValidationResult validationResult = validateRecord(processedRecord, options,
                                                                    existingEmails, existingPhones,
                                                                    result.getValidRecords());

            // Handle the validation result
            if (validationResult.isValid()) {
                result.addValidRecord(processedRecord);
            } else if (validationResult.isDuplicate() && !options.isDuplicatesAreErrors()) {
                result.addDuplicateRecord(processedRecord);
            } else {
                result.addInvalidRecord(processedRecord, validationResult.getErrors());
            }
        } catch (Exception e) {
            logger.severe("Unexpected error processing record: " + e.getMessage());
            result.addInvalidRecord(record, Collections.singletonList("Processing error: " + e.getMessage()));
        }
    }

    /**
     * Validates a single record against all business rules
     */
    private RecordValidationResult validateRecord(Map<String, Object> record,
                                                  CustomerProcessingOptions options,
                                                  Set<String> existingEmails,
                                                  Set<String> existingPhones,
                                                  List<Map<String, Object>> previousValidRecords) {
        List<String> errors = new ArrayList<>();
        boolean isDuplicate = false;

        // Validate email
        EmailValidationResult emailResult = emailValidator.validate(record, existingEmails, previousValidRecords);
        if (!emailResult.isValid()) {
            errors.addAll(emailResult.getErrors());
        } else if (emailResult.isDuplicate()) {
            isDuplicate = true;
        }

        // Validate phone
        PhoneValidationResult phoneResult = phoneValidator.validate(record, existingPhones, previousValidRecords);
        if (!phoneResult.isValid()) {
            errors.addAll(phoneResult.getErrors());
        } else if (phoneResult.isDuplicate()) {
            isDuplicate = true;
        }

        // Validate names
        validateNames(record, errors);

        // Return result
        boolean isValid = errors.isEmpty() && !(isDuplicate && options.isDuplicatesAreErrors());
        return new RecordValidationResult(isValid, isDuplicate, errors);
    }

    /**
     * Validates name fields
     */
    private void validateNames(Map<String, Object> record, List<String> errors) {
        // First name
        if (!record.containsKey("firstName") || isEmpty(record.get("firstName"))) {
            errors.add("Missing required field: firstName");
        }

        // Last name
        if (!record.containsKey("lastName") || isEmpty(record.get("lastName"))) {
            errors.add("Missing required field: lastName");
        }
    }

    /**
     * Helper to check if a value is empty
     */
    private boolean isEmpty(Object value) {
        return value == null || value.toString().trim().isEmpty();
    }

    /**
     * Persists valid records to the database
     */
    private void persistValidRecords(List<Map<String, Object>> validRecords, String source) {
        List<Customer> customers = new ArrayList<>();
        for (Map<String, Object> record : validRecords) {
            Customer customer = mapToCustomerEntity(record);
            customer.setDataSource(source);
            customer.setCreatedAt(new Date());
            customers.add(customer);
        }

        customerRepository.saveAll(customers);
        logger.info("Successfully saved " + customers.size() + " customer records to database");
    }

    /**
     * Builds the final processing report
     */
    private Map<String, Object> buildProcessingReport(ProcessingResult result,
                                                      int totalRecords,
                                                      long processingTimeMs,
                                                      String source,
                                                      CustomerProcessingOptions options) {
        Map<String, Object> report = new HashMap<>();

        report.put("status", "success");
        report.put("source", source);
        report.put("totalRecords", totalRecords);
        report.put("processedCount", result.getTotalProcessed());
        report.put("successCount", result.getTotalSuccess());
        report.put("errorCount", result.getTotalErrors());
        report.put("skippedCount", result.getTotalSkipped());
        report.put("duplicateCount", result.getDuplicateRecords().size());
        report.put("processingTimeMs", processingTimeMs);
        report.put("errorsByType", result.getErrorCounts());

        // Include detailed records if requested
        if (options.isIncludeRecordsInResponse()) {
            if (options.isIncludeValidRecords()) {
                report.put("validRecords", result.getValidRecords());
            }
            if (options.isIncludeInvalidRecords()) {
                report.put("invalidRecords", result.getInvalidRecords());
            }
            if (options.isIncludeDuplicateRecords()) {
                report.put("duplicateRecords", result.getDuplicateRecords());
            }
        }

        logger.info("Customer data processing completed. Total: " + totalRecords +
                   ", Success: " + result.getTotalSuccess() + ", Errors: " + result.getTotalErrors());

        return report;
    }

    /**
     * Creates an error result response
     */
    private Map<String, Object> createErrorResult(String message) {
        Map<String, Object> result = new HashMap<>();
        result.put("status", "error");
        result.put("message", message);
        return result;
    }

    /**
     * Creates an error result response with exception details
     */
    private Map<String, Object> createErrorResult(String message, Exception e) {
        Map<String, Object> result = new HashMap<>();
        result.put("status", "error");
        result.put("message", message);
        result.put("error", e.getMessage());
        logger.severe(message + ": " + e.getMessage());
        return result;
    }

    /**
     * Maps a record to a Customer entity
     */
    private Customer mapToCustomerEntity(Map<String, Object> record) {
        Customer customer = new Customer();
        if (record.containsKey("firstName")) {
            customer.setFirstName(record.get("firstName").toString());
        }
        if (record.containsKey("lastName")) {
            customer.setLastName(record.get("lastName").toString());
        }
        if (record.containsKey("email")) {
            customer.setEmail(record.get("email").toString());
        }
        if (record.containsKey("phone")) {
            customer.setPhoneNumber(record.get("phone").toString());
        }
        return customer;
    }
}

