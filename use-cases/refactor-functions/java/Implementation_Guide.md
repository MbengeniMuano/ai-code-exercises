# Function Decomposition Challenge - Implementation Guide

## Quick Start Guide

This guide provides step-by-step instructions for understanding and implementing the Function Decomposition Challenge solution.

---

## Part 1: Understanding the Original Problem

### Problem: Monolithic Function

The original `CustomerProcessor.processCustomerData()` method tried to do everything:

```
400+ lines
├── Input validation
├── Data preprocessing (CSV/API/Manual)
├── Email validation
├── Email deduplication
├── Phone validation  
├── Phone deduplication
├── Name validation
├── Error tracking
├── Database persistence
└── Report generation
```

**Issues:**
- ❌ Hard to test individual components
- ❌ Changes in one area could break others
- ❌ Impossible to reuse logic elsewhere
- ❌ New developers struggle to understand
- ❌ Adding features is error-prone

---

## Part 2: Decomposition Strategy

### Step 1: Identify Distinct Responsibilities

**Email Handling:**
```java
- Check if email exists
- Validate email format
- Detect duplicates (in batch and database)
```
→ Extract to `EmailValidator`

**Phone Handling:**
```java
- Check if phone exists
- Validate phone format
- Detect duplicates
- Normalize format
```
→ Extract to `PhoneValidator`

**Data Transformation:**
```java
- CSV preprocessing
- API preprocessing
- Manual entry preprocessing
```
→ Extract to `RecordTransformer`

**Result Aggregation:**
```java
- Track valid records
- Track invalid records
- Track duplicates
- Aggregate statistics
```
→ Create `ProcessingResult`

### Step 2: Create Validator Classes

#### EmailValidator

**Before:**
```java
// Mixed with other logic in main function
if (!processedRecord.containsKey("email") || ...) {
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
        // ... more deduplication logic
    }
}
```

**After:**
```java
public class EmailValidator {
    public EmailValidationResult validate(Map<String, Object> record,
                                         Set<String> existingEmails,
                                         List<Map<String, Object>> previousValidRecords) {
        List<String> errors = new ArrayList<>();
        boolean isDuplicate = false;

        // Check if email exists
        if (!record.containsKey("email") || isEmpty(record.get("email"))) {
            errors.add("Missing required field: email");
            return new EmailValidationResult(false, false, errors);
        }

        String email = record.get("email").toString().trim().toLowerCase();
        record.put("email", email);

        // Validate email format
        if (!isValidFormat(email)) {
            errors.add("Invalid email format: " + email);
            return new EmailValidationResult(false, false, errors);
        }

        // Check for duplicates
        if (isDuplicateEmail(email, existingEmails, previousValidRecords)) {
            isDuplicate = true;
        }

        boolean isValid = errors.isEmpty();
        return new EmailValidationResult(isValid, isDuplicate, errors);
    }
}
```

**Benefits:**
- ✅ Email logic is isolated
- ✅ Can be tested independently
- ✅ Can be reused in other contexts
- ✅ Changes to email don't affect other validation

#### PhoneValidator

Similar pattern to EmailValidator:
- Isolated phone-specific logic
- Returns `PhoneValidationResult`
- Can be tested independently
- Handles phone normalization

### Step 3: Create Transformer Class

**Purpose:** Handle source-specific preprocessing

```java
public class RecordTransformer {
    public Map<String, Object> transform(Map<String, Object> record, String source) {
        switch (source.toLowerCase()) {
            case "csv":
                return transformCsvRecord(record);    // CSV-specific logic
            case "api":
                return transformApiRecord(record);    // API-specific logic
            case "manual":
                return transformManualRecord(record); // Manual entry logic
            default:
                return record;
        }
    }
}
```

**Benefits:**
- ✅ Source logic is centralized
- ✅ Easy to add new sources
- ✅ Preprocessing separated from validation

### Step 4: Refactor Main Function

**Before:**
```java
public Map<String, Object> processCustomerData(...) {
    // 400+ lines of mixed logic
}
```

**After:**
```java
public Map<String, Object> processCustomerData(...) {
    // 1. Validate input
    if (!isValidInput(rawData)) {
        return createErrorResult("No data provided for processing");
    }

    // 2. Load existing customers for deduplication
    Set<String> existingEmails = new HashSet<>();
    Set<String> existingPhones = new HashSet<>();
    if (options.isPerformDeduplication()) {
        loadExistingCustomersForDeduplication(existingEmails, existingPhones);
    }

    // 3. Process all records
    ProcessingResult result = processAllRecords(rawData, source, options, 
                                              existingEmails, existingPhones);

    // 4. Persist if needed
    if (options.isSaveToDatabase() && !result.getValidRecords().isEmpty()) {
        persistValidRecords(result.getValidRecords(), source);
    }

    // 5. Generate report
    return buildProcessingReport(result, rawData.size(), 
                                endTime - startTime, source, options);
}
```

**Benefits:**
- ✅ Clear workflow
- ✅ Each step is a function call (delegation)
- ✅ Easy to understand high-level logic
- ✅ Can test each step independently

---

## Part 3: Testing the Refactored Code

### Unit Testing Individual Validators

#### Test EmailValidator

```java
@Test
public void testValidEmailFormat() {
    Map<String, Object> record = new HashMap<>();
    record.put("email", "user@example.com");

    EmailValidationResult result = emailValidator.validate(
        record,
        new HashSet<>(),
        new ArrayList<>()
    );

    assertTrue(result.isValid());
    assertFalse(result.isDuplicate());
    assertTrue(result.getErrors().isEmpty());
}

@Test
public void testInvalidEmailFormat() {
    Map<String, Object> record = new HashMap<>();
    record.put("email", "invalid-email");

    EmailValidationResult result = emailValidator.validate(
        record,
        new HashSet<>(),
        new ArrayList<>()
    );

    assertFalse(result.isValid());
    assertTrue(result.getErrors().get(0).contains("Invalid email format"));
}

@Test
public void testDuplicateEmailDetection() {
    Map<String, Object> record = new HashMap<>();
    record.put("email", "user@example.com");
    
    Set<String> existingEmails = new HashSet<>();
    existingEmails.add("user@example.com");

    EmailValidationResult result = emailValidator.validate(
        record,
        existingEmails,
        new ArrayList<>()
    );

    assertTrue(result.isDuplicate());
}
```

### Integration Testing Main Processor

```java
@Test
public void testProcessValidRecords() {
    List<Map<String, Object>> data = new ArrayList<>();
    Map<String, Object> record = new HashMap<>();
    record.put("firstName", "john");
    record.put("lastName", "doe");
    record.put("email", "john@example.com");
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
```

---

## Part 4: Practical Tips and Tricks

### Tip 1: Use Result Objects

**Bad:**
```java
// Hard to track what's being returned
public boolean validate(...) {
    // Returns true/false but loses error details
}
```

**Good:**
```java
// Clear what's returned and why
public EmailValidationResult validate(...) {
    return new EmailValidationResult(
        isValid,      // Was validation successful?
        isDuplicate,  // Is it a duplicate?
        errors        // What went wrong?
    );
}
```

### Tip 2: Dependency Injection

**Bad:**
```java
// Creating dependencies inside the class
public class CustomerProcessorRefactored {
    private EmailValidator emailValidator = new EmailValidator();
    // Hard to test with different implementations
}
```

**Good:**
```java
// Dependencies injected via constructor
public class CustomerProcessorRefactored {
    private final EmailValidator emailValidator;
    
    public CustomerProcessorRefactored(EmailValidator emailValidator, ...) {
        this.emailValidator = emailValidator;
        // Easy to test with mocks
    }
}
```

### Tip 3: Encapsulate Related Data

**Bad:**
```java
// Data scattered across multiple variables
List<Map<String, Object>> validRecords = new ArrayList<>();
List<Map<String, Object>> invalidRecords = new ArrayList<>();
List<Map<String, Object>> duplicateRecords = new ArrayList<>();
Map<String, Integer> errorCounts = new HashMap<>();
int totalProcessed = 0;
int totalSkipped = 0;
// Hard to pass around
```

**Good:**
```java
// Data encapsulated in a result object
public class ProcessingResult {
    private List<Map<String, Object>> validRecords;
    private List<Map<String, Object>> invalidRecords;
    private List<Map<String, Object>> duplicateRecords;
    private Map<String, Integer> errorCounts;
    private int totalProcessed;
    private int totalSkipped;
    // Easy to pass and extend
}
```

### Tip 4: Clear Naming Conventions

**Bad:**
```java
public void validate(Map<String, Object> r, Set<String> e, List<Map<String, Object>> p) {
    // What are r, e, and p?
}
```

**Good:**
```java
public EmailValidationResult validate(Map<String, Object> record,
                                     Set<String> existingEmails,
                                     List<Map<String, Object>> previousValidRecords) {
    // Clear what each parameter means
}
```

### Tip 5: Use Factory Methods for Complex Construction

**Bad:**
```java
Map<String, Object> result = new HashMap<>();
result.put("status", "error");
result.put("message", message);
result.put("error", e.getMessage());
// Repeated in multiple places
```

**Good:**
```java
private Map<String, Object> createErrorResult(String message, Exception e) {
    Map<String, Object> result = new HashMap<>();
    result.put("status", "error");
    result.put("message", message);
    result.put("error", e.getMessage());
    return result;
}
// Used consistently
```

---

## Part 5: Common Refactoring Mistakes

### ❌ Mistake 1: Extracting Too Much

**Problem:**
```java
// Don't create a class for every single function
public class EmailFormatValidator {
    public boolean isValidFormat(String email) { ... }
}

public class EmailDuplicateChecker {
    public boolean isDuplicate(String email) { ... }
}

public class EmailNormalizer {
    public String normalize(String email) { ... }
}
// Too many classes, gets confusing
```

**Solution:**
```java
// Group related logic in one class
public class EmailValidator {
    public EmailValidationResult validate(...) {
        // Handles format, duplicates, and normalization
    }
}
```

### ❌ Mistake 2: Breaking Encapsulation

**Problem:**
```java
// Exposing internal state
public class ProcessingResult {
    public List<Map<String, Object>> validRecords;  // Public field
    public void setValidRecords(List<...> records) { ... }
    public List<Map<String, Object>> getValidRecords() { ... }
    // Direct access undermines encapsulation
}
```

**Solution:**
```java
// Proper encapsulation with getters
public class ProcessingResult {
    private List<Map<String, Object>> validRecords;  // Private
    
    public void addValidRecord(Map<String, Object> record) {
        validRecords.add(record);
    }
    
    public List<Map<String, Object>> getValidRecords() {
        return new ArrayList<>(validRecords);  // Return copy
    }
}
```

### ❌ Mistake 3: Not Testing During Refactoring

**Problem:**
```java
// Refactor without tests
// End up with subtle behavioral differences
// Hard to debug what changed
```

**Solution:**
```java
// Write tests before refactoring
@Test public void testOriginalBehavior() { ... }

// Refactor
// Verify tests still pass
// Refactored code behaves identically
```

### ❌ Mistake 4: Over-Engineering

**Problem:**
```java
// Create interfaces for everything
public interface EmailValidator {
    EmailValidationResult validate(...);
}

// But only have one implementation
public class StandardEmailValidator implements EmailValidator {
    // Why the interface?
}
```

**Solution:**
```java
// Use concrete classes unless you have multiple implementations
public class EmailValidator {
    // Simple, clear, no unnecessary abstraction
}

// When you need multiple implementations, then add interface
public interface Validator {
    ValidationResult validate(...);
}
```

---

## Part 6: Extending the Solution

### Adding a New Data Source

**Step 1:** Add transformation method to `RecordTransformer`

```java
public Map<String, Object> transform(Map<String, Object> record, String source) {
    switch (source.toLowerCase()) {
        // ... existing cases ...
        case "json":
            return transformJsonRecord(record);
        default:
            return record;
    }
}

private Map<String, Object> transformJsonRecord(Map<String, Object> record) {
    // JSON-specific preprocessing
    return record;
}
```

**Step 2:** Test the new transformation

```java
@Test
public void testJsonRecordTransformation() {
    Map<String, Object> record = new HashMap<>();
    record.put("email", "  user@example.com  "); // JSON has whitespace
    
    Map<String, Object> transformed = transformer.transform(record, "json");
    
    assertEquals("user@example.com", transformed.get("email"));
}
```

### Adding a New Validation Rule

**Step 1:** Create new validator class

```java
public class DateOfBirthValidator {
    public DateValidationResult validate(Map<String, Object> record) {
        // Validate date of birth
    }
}
```

**Step 2:** Update main processor

```java
private void processIndividualRecord(...) {
    // ... existing validation ...
    
    DateValidator dateValidator = new DateValidator();
    DateValidationResult dateResult = dateValidator.validate(processedRecord);
    if (!dateResult.isValid()) {
        errors.addAll(dateResult.getErrors());
    }
}
```

**Step 3:** Test the new validator

```java
@Test
public void testDateValidation() {
    // Test valid date
    // Test invalid date
    // Test future date
}
```

---

## Part 7: Performance Considerations

### Tip 1: Avoid Redundant Lookups

**Bad:**
```java
// Checking duplicates multiple times
for (Map<String, Object> record : previousValidRecords) {
    if (email.equals(record.get("email"))) { ... }
}
// Called for each record
```

**Good:**
```java
// Load once, lookup in Set
Set<String> previousEmails = previousValidRecords.stream()
    .map(r -> r.get("email").toString())
    .collect(Collectors.toSet());

if (previousEmails.contains(email)) { ... }
```

### Tip 2: Lazy Loading

**Bad:**
```java
// Always load all existing customers
List<Customer> allCustomers = customerRepository.findAll();
// Could be millions of records
```

**Good:**
```java
// Load only if needed
if (options.isPerformDeduplication()) {
    loadExistingCustomersForDeduplication(...);
}
```

### Tip 3: Stream Processing for Large Data

**For large datasets:**
```java
// Instead of collecting all in memory
List<Map<String, Object>> data = loadAllData(); // Could be huge

// Consider streaming
try (Stream<Map<String, Object>> stream = loadDataAsStream()) {
    stream.forEach(record -> processRecord(record));
}
```

---

## Summary Checklist

### Before Refactoring
- [ ] Function has multiple responsibilities ✓
- [ ] Function is difficult to test ✓
- [ ] Function is longer than 100 lines ✓
- [ ] Function logic is hard to understand ✓

### During Refactoring
- [ ] Identified distinct responsibilities
- [ ] Created appropriate classes/methods
- [ ] Used dependency injection
- [ ] Wrote tests as you go
- [ ] Verified behavior hasn't changed

### After Refactoring
- [ ] All tests pass
- [ ] Code is more readable
- [ ] Each function has one responsibility
- [ ] Functions are testable in isolation
- [ ] Documentation is updated

---

**Next Steps:** Review the complete solution code and run the tests!

