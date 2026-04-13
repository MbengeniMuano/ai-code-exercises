package com.example.refactoring;

/**
 * Options for customizing customer data processing behavior
 */
public class CustomerProcessingOptions {
    private boolean performDeduplication = true;
    private boolean duplicatesAreErrors = false;
    private int maxErrorCount = -1; // -1 means no limit
    private boolean saveToDatabase = true;
    private boolean includeRecordsInResponse = false;
    private boolean includeValidRecords = false;
    private boolean includeInvalidRecords = true;
    private boolean includeDuplicateRecords = false;

    // Constructors
    public CustomerProcessingOptions() {
    }

    public CustomerProcessingOptions(boolean performDeduplication,
                                    boolean saveToDatabase,
                                    int maxErrorCount) {
        this.performDeduplication = performDeduplication;
        this.saveToDatabase = saveToDatabase;
        this.maxErrorCount = maxErrorCount;
    }

    // Builder pattern for easier configuration
    public static CustomerProcessingOptions builder() {
        return new CustomerProcessingOptions();
    }

    public CustomerProcessingOptions withDeduplication(boolean performDeduplication) {
        this.performDeduplication = performDeduplication;
        return this;
    }

    public CustomerProcessingOptions withDuplicatesAsErrors(boolean duplicatesAreErrors) {
        this.duplicatesAreErrors = duplicatesAreErrors;
        return this;
    }

    public CustomerProcessingOptions withMaxErrorCount(int maxErrorCount) {
        this.maxErrorCount = maxErrorCount;
        return this;
    }

    public CustomerProcessingOptions withSaveToDatabase(boolean saveToDatabase) {
        this.saveToDatabase = saveToDatabase;
        return this;
    }

    public CustomerProcessingOptions withIncludeRecordsInResponse(boolean includeRecordsInResponse) {
        this.includeRecordsInResponse = includeRecordsInResponse;
        return this;
    }

    public CustomerProcessingOptions withIncludeValidRecords(boolean includeValidRecords) {
        this.includeValidRecords = includeValidRecords;
        return this;
    }

    public CustomerProcessingOptions withIncludeInvalidRecords(boolean includeInvalidRecords) {
        this.includeInvalidRecords = includeInvalidRecords;
        return this;
    }

    public CustomerProcessingOptions withIncludeDuplicateRecords(boolean includeDuplicateRecords) {
        this.includeDuplicateRecords = includeDuplicateRecords;
        return this;
    }

    // Getters
    public boolean isPerformDeduplication() {
        return performDeduplication;
    }

    public boolean isDuplicatesAreErrors() {
        return duplicatesAreErrors;
    }

    public int getMaxErrorCount() {
        return maxErrorCount;
    }

    public boolean isSaveToDatabase() {
        return saveToDatabase;
    }

    public boolean isIncludeRecordsInResponse() {
        return includeRecordsInResponse;
    }

    public boolean isIncludeValidRecords() {
        return includeValidRecords;
    }

    public boolean isIncludeInvalidRecords() {
        return includeInvalidRecords;
    }

    public boolean isIncludeDuplicateRecords() {
        return includeDuplicateRecords;
    }
}

