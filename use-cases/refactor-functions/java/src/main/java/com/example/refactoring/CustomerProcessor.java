package com.example.refactoring;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Logger;

/**
 * ORIGINAL VERSION: Complex monolithic customer data processor
 * This function attempts to do too much in a single method, making it hard to test,
 * maintain, and understand.
 */
public class CustomerProcessor {
    private static final Logger logger = Logger.getLogger(CustomerProcessor.class.getName());
    private CustomerRepository customerRepository;

    public CustomerProcessor(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    /**
     * Processes raw customer data, validates it, transforms it, and loads it into the database.
     * Handles different data sources, performs deduplication, and tracks processing errors.
     *
     * WARNING: This is a complex function that does too much. See CustomerProcessorRefactored
     * for the improved version.
     */
    public Map<String, Object> processCustomerData(List<Map<String, Object>> rawData,
                                                 String source,
                                                 CustomerProcessingOptions options) {
        long startTime = System.currentTimeMillis();
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> validRecords = new ArrayList<>();
        List<Map<String, Object>> invalidRecords = new ArrayList<>();
        List<Map<String, Object>> duplicateRecords = new ArrayList<>();
        List<Map<String, Object>> processedRecords = new ArrayList<>();
        Set<String> existingEmails = new HashSet<>();
        Set<String> existingPhones = new HashSet<>();
        Map<String, Integer> errorCounts = new HashMap<>();
        int totalProcessed = 0;
        int totalSuccess = 0;
        int totalSkipped = 0;
        int totalErrors = 0;

        if (rawData == null || rawData.isEmpty()) {
            result.put("status", "error");
            result.put("message", "No data provided for processing");
            return result;
        }

        logger.info("Starting to process " + rawData.size() + " customer records from source: " + source);

        // Load existing records for deduplication if needed
        if (options.isPerformDeduplication()) {
            try {
                List<Customer> existingCustomers = customerRepository.findAll();
                for (Customer customer : existingCustomers) {
                    if (customer.getEmail() != null && !customer.getEmail().isEmpty()) {
                        existingEmails.add(customer.getEmail().toLowerCase());
                    }
                    if (customer.getPhoneNumber() != null && !customer.getPhoneNumber().isEmpty()) {
                        existingPhones.add(normalizePhoneNumber(customer.getPhoneNumber()));
                    }
                }
                logger.info("Loaded " + existingCustomers.size() + " existing customers for deduplication");
            } catch (Exception e) {
                logger.severe("Error loading existing customers for deduplication: " + e.getMessage());
                result.put("status", "error");
                result.put("message", "Failed to load existing customers for deduplication");
                result.put("error", e.getMessage());
                return result;
            }
        }

        // Process each record
        for (Map<String, Object> record : rawData) {
            totalProcessed++;
            Map<String, Object> processedRecord = new HashMap<>(record);
            List<String> recordErrors = new ArrayList<>();
            boolean isValid = true;

            // Skip processing if max error threshold has been reached
            if (options.getMaxErrorCount() > 0 && totalErrors >= options.getMaxErrorCount()) {
                logger.warning("Maximum error threshold reached. Skipping remaining records.");
                totalSkipped = rawData.size() - totalProcessed + 1;
                break;
            }

            try {
                // Source-specific preprocessing
                if (source.equals("csv")) {
                    processedRecord = preprocessCsvRecord(processedRecord);
                } else if (source.equals("api")) {
                    processedRecord = preprocessApiRecord(processedRecord);
                } else if (source.equals("manual")) {
                    processedRecord = preprocessManualRecord(processedRecord);
                }

                // Validate required fields
                if (!processedRecord.containsKey("email") ||
                    processedRecord.get("email") == null ||
                    processedRecord.get("email").toString().trim().isEmpty()) {
                    recordErrors.add("Missing required field: email");
                    isValid = false;
                } else {
                    String email = processedRecord.get("email").toString().trim().toLowerCase();
                    processedRecord.put("email", email);

                    if (!isValidEmail(email)) {
                        recordErrors.add("Invalid email format: " + email);
                        isValid = false;
                    }

                    if (options.isPerformDeduplication()) {
                        boolean isDuplicate = false;
                        for (Map<String, Object> validRecord : validRecords) {
                            if (email.equals(validRecord.get("email").toString().toLowerCase())) {
                                isDuplicate = true;
                                break;
                            }
                        }

                        if (!isDuplicate && existingEmails.contains(email)) {
                            isDuplicate = true;
                        }

                        if (isDuplicate) {
                            recordErrors.add("Duplicate email: " + email);
                            if (options.isDuplicatesAreErrors()) {
                                isValid = false;
                            } else {
                                duplicateRecords.add(processedRecord);
                                totalSkipped++;
                                continue;
                            }
                        }
                    }
                }

                // Validate and process name fields
                if (processedRecord.containsKey("firstName") && processedRecord.get("firstName") != null) {
                    String firstName = processedRecord.get("firstName").toString().trim();
                    if (!firstName.isEmpty()) {
                        firstName = firstName.substring(0, 1).toUpperCase() +
                                   (firstName.length() > 1 ? firstName.substring(1) : "");
                    }
                    processedRecord.put("firstName", firstName);
                } else {
                    recordErrors.add("Missing required field: firstName");
                    isValid = false;
                }

                if (processedRecord.containsKey("lastName") && processedRecord.get("lastName") != null) {
                    String lastName = processedRecord.get("lastName").toString().trim();
                    if (!lastName.isEmpty()) {
                        lastName = lastName.substring(0, 1).toUpperCase() +
                                  (lastName.length() > 1 ? lastName.substring(1) : "");
                    }
                    processedRecord.put("lastName", lastName);
                } else {
                    recordErrors.add("Missing required field: lastName");
                    isValid = false;
                }

                // Process and validate phone number if present
                if (processedRecord.containsKey("phone") &&
                    processedRecord.get("phone") != null &&
                    !processedRecord.get("phone").toString().trim().isEmpty()) {

                    String phone = processedRecord.get("phone").toString().trim();
                    String normalizedPhone = normalizePhoneNumber(phone);

                    if (!isValidPhoneNumber(normalizedPhone)) {
                        recordErrors.add("Invalid phone number format: " + phone);
                        isValid = false;
                    } else {
                        processedRecord.put("phone", formatPhoneNumber(normalizedPhone));

                        if (options.isPerformDeduplication()) {
                            boolean isDuplicate = false;
                            for (Map<String, Object> validRecord : validRecords) {
                                if (validRecord.containsKey("phone") &&
                                    normalizedPhone.equals(
                                        normalizePhoneNumber(validRecord.get("phone").toString()))) {
                                    isDuplicate = true;
                                    break;
                                }
                            }

                            if (!isDuplicate && existingPhones.contains(normalizedPhone)) {
                                isDuplicate = true;
                            }

                            if (isDuplicate) {
                                recordErrors.add("Duplicate phone number: " + phone);
                                if (options.isDuplicatesAreErrors()) {
                                    isValid = false;
                                }
                            }
                        }
                    }
                }

                // Final decision on record validity
                if (isValid) {
                    validRecords.add(processedRecord);
                    totalSuccess++;
                } else {
                    processedRecord.put("errors", recordErrors);
                    invalidRecords.add(processedRecord);
                    totalErrors++;

                    for (String error : recordErrors) {
                        String errorType = error.split(":")[0].trim();
                        errorCounts.put(errorType, errorCounts.getOrDefault(errorType, 0) + 1);
                    }
                }

            } catch (Exception e) {
                logger.severe("Unexpected error processing record " + totalProcessed + ": " + e.getMessage());
                e.printStackTrace();

                processedRecord.put("errors", Collections.singletonList("Processing error: " + e.getMessage()));
                invalidRecords.add(processedRecord);
                totalErrors++;

                String errorType = "Processing error";
                errorCounts.put(errorType, errorCounts.getOrDefault(errorType, 0) + 1);
            }
        }

        // Save valid records to database if requested
        if (options.isSaveToDatabase() && !validRecords.isEmpty()) {
            try {
                List<Customer> customers = new ArrayList<>();
                for (Map<String, Object> record : validRecords) {
                    Customer customer = mapToCustomerEntity(record);
                    customer.setDataSource(source);
                    customer.setCreatedAt(new Date());
                    customers.add(customer);
                }

                customerRepository.saveAll(customers);
                logger.info("Successfully saved " + customers.size() + " customer records to database");

                processedRecords.addAll(validRecords);

            } catch (Exception e) {
                logger.severe("Error saving records to database: " + e.getMessage());
                result.put("status", "error");
                result.put("message", "Failed to save valid records to database");
                result.put("error", e.getMessage());
                return result;
            }
        } else {
            processedRecords.addAll(validRecords);
        }

        // Generate processing report
        long endTime = System.currentTimeMillis();
        long processingTime = endTime - startTime;

        result.put("status", "success");
        result.put("source", source);
        result.put("totalRecords", rawData.size());
        result.put("processedCount", totalProcessed);
        result.put("successCount", totalSuccess);
        result.put("errorCount", totalErrors);
        result.put("skippedCount", totalSkipped);
        result.put("duplicateCount", duplicateRecords.size());
        result.put("processingTimeMs", processingTime);
        result.put("errorsByType", errorCounts);

        if (options.isIncludeRecordsInResponse()) {
            if (options.isIncludeValidRecords()) {
                result.put("validRecords", validRecords);
            }
            if (options.isIncludeInvalidRecords()) {
                result.put("invalidRecords", invalidRecords);
            }
            if (options.isIncludeDuplicateRecords()) {
                result.put("duplicateRecords", duplicateRecords);
            }
        }

        logger.info("Customer data processing completed. Total: " + rawData.size() +
                   ", Success: " + totalSuccess + ", Error: " + totalErrors);

        return result;
    }

    // Helper methods
    private Map<String, Object> preprocessCsvRecord(Map<String, Object> record) {
        return record;
    }

    private Map<String, Object> preprocessApiRecord(Map<String, Object> record) {
        return record;
    }

    private Map<String, Object> preprocessManualRecord(Map<String, Object> record) {
        return record;
    }

    protected boolean isValidEmail(String email) {
        return email.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$");
    }

    protected String normalizePhoneNumber(String phone) {
        return phone.replaceAll("[^0-9+]", "");
    }

    protected boolean isValidPhoneNumber(String phone) {
        return phone.matches("^\\+?[0-9]{10,15}$");
    }

    protected String formatPhoneNumber(String phone) {
        return phone;
    }

    protected Customer mapToCustomerEntity(Map<String, Object> record) {
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

