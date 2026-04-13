# Quick Reference Guide: Testing Best Practices

**Based on Complete Testing Exercise (April 13, 2026)**

---

## Testing Checklist

### Before Writing Tests

- [ ] Understand what the function does
- [ ] Identify all behaviors to test
- [ ] List potential edge cases
- [ ] Prioritize test cases
- [ ] Design test data

### While Writing Tests

- [ ] One behavior per test
- [ ] Clear, descriptive test name
- [ ] Use Arrange-Act-Assert (AAA) pattern
- [ ] Verify exact values (not just "greater than")
- [ ] Use fixed reference data (not dynamic time)
- [ ] Make assertions meaningful with messages

### After Writing Tests

- [ ] Run tests - verify they fail first (for new code)
- [ ] Run tests - verify they pass (for fixed code)
- [ ] Check code coverage
- [ ] Review test readability
- [ ] Ensure tests are maintainable

---

## Test Naming Convention

```
testMethodName_Condition_ExpectedResult()
```

**Examples**:
- ✅ `testCalculateTaskScore_HighPriorityNoOtherFactors_Equals30`
- ✅ `testSortByImportance_EmptyList_ReturnsEmpty`
- ✅ `testGetTopTasks_LimitExceedsListSize_ReturnsAllTasks`

---

## Arrange-Act-Assert Pattern

```java
@Test
void testSomething() {
    // ARRANGE: Set up test conditions
    Task task = new Task("Title", "Description", TaskPriority.HIGH, null, List.of());
    task.setStatus(TaskStatus.TODO);
    task.setUpdatedAt(LocalDateTime.now().minusDays(10));
    
    // ACT: Execute the code being tested
    int score = TaskPriorityManager.calculateTaskScore(task);
    
    // ASSERT: Verify the result
    assertEquals(30, score, "HIGH priority should score 30");
}
```

---

## Common Testing Mistakes & Fixes

### Mistake 1: Vague Assertions

```java
// ❌ Bad
assertTrue(score > 0);

// ✅ Good
assertEquals(30, score, "HIGH priority should be 3 * 10 = 30");
```

### Mistake 2: Time-Dependent Tests

```java
// ❌ Bad - Fails at midnight or next day
LocalDateTime tomorrow = LocalDateTime.now().plusDays(1);
assertEquals(15, calculateTaskScore(task));

// ✅ Good - Deterministic
LocalDateTime referenceTime = LocalDateTime.of(2026, 4, 13, 10, 0);
assertEquals(15, calculateTaskScore(task, referenceTime));
```

### Mistake 3: Multiple Behaviors in One Test

```java
// ❌ Bad - Tests multiple things
@Test void testTask() {
    assertEquals(30, score);  // Priority?
    assertTrue(score > 0);    // Positive?
    assertFalse(score < 0);   // Not negative?
}

// ✅ Good - Tests one behavior
@Test void testHighPriority_Equals30() {
    assertEquals(30, score);
}
```

### Mistake 4: No Edge Cases

```java
// ❌ Bad - Only happy path
@Test void testSort() {
    List<Task> tasks = List.of(task1, task2);
    List<Task> sorted = TaskPriorityManager.sortTasksByImportance(tasks);
    assertEquals(2, sorted.size());
}

// ✅ Good - Includes edge cases
@Test void testSort_EmptyList_ReturnsEmpty() {
    List<Task> sorted = TaskPriorityManager.sortTasksByImportance(new ArrayList<>());
    assertTrue(sorted.isEmpty());
}

@Test void testSort_SingleTask_ReturnsSingleTask() {
    List<Task> sorted = TaskPriorityManager.sortTasksByImportance(List.of(task1));
    assertEquals(1, sorted.size());
}
```

### Mistake 5: Over-Mocking

```java
// ❌ Bad - Mocks too much
@Mock Task mockTask;
@Mock LocalDateTime mockTime;
// ... 8 more mocks

// ✅ Good - Test real objects
Task task = new Task("Real Task", "", TaskPriority.HIGH, null, List.of());
int score = TaskPriorityManager.calculateTaskScore(task);
```

---

## Test Categories

### Unit Tests
- Test **one function** in isolation
- Test **one behavior** per test
- **Fast** to execute (< 1 second)
- **Deterministic** (same result every time)
- Can run **in any order**

```java
@Test
void testCalculateTaskScore_LowPriority_Equals10() {
    Task task = new Task("Title", "", TaskPriority.LOW, null, List.of());
    task.setStatus(TaskStatus.TODO);
    task.setUpdatedAt(LocalDateTime.now().minusDays(10));
    assertEquals(10, TaskPriorityManager.calculateTaskScore(task));
}
```

### Integration Tests
- Test **multiple functions** together
- Verify **interactions** between components
- Can be **slower** (acceptable)
- Use **realistic data**
- Test **real-world scenarios**

```java
@Test
void testFullWorkflow_CalculateSortAndGetTop() {
    List<Task> tasks = createRealisticTaskSet();
    List<Task> sorted = TaskPriorityManager.sortTasksByImportance(tasks);
    List<Task> top3 = TaskPriorityManager.getTopPriorityTasks(tasks, 3);
    
    assertEquals(tasks.size(), sorted.size());
    assertEquals(3, top3.size());
}
```

### Edge Case Tests
- Test **boundaries** and **special conditions**
- Test **null and empty** inputs
- Test **unusual but valid** inputs
- Test **error conditions**

```java
@Test void testEmptyList() { /* ... */ }
@Test void testNullValue() { /* ... */ }
@Test void testBoundaryValue() { /* ... */ }
@Test void testNegativeScore() { /* ... */ }
```

---

## Test Organization with Nested Classes

```java
@DisplayName("TaskPriorityManager Tests")
class TaskPriorityManagerTest {
    
    @Nested
    @DisplayName("Priority Weight Tests")
    class PriorityWeightTests {
        @Test void testLowPriority() { }
        @Test void testHighPriority() { }
    }
    
    @Nested
    @DisplayName("Due Date Tests")
    class DueDateTests {
        @Test void testOverdueTask() { }
        @Test void testTaskDueToday() { }
    }
}
```

---

## Coverage Target

```
Minimum Requirements:
- Critical Functions: 90%+
- Complex Logic: 85%+
- General Code: 70%+

Ideal State:
- Overall: 80%+
- Most Functions: 85%+
- Critical Path: 95%+

Not Recommended:
- 100% Coverage (often false confidence)
- Chasing Coverage (write good tests, not test counts)
```

---

## Test Data Strategies

### Strategy 1: Builder Pattern

```java
Task task = new TaskBuilder()
    .withTitle("Test Task")
    .withPriority(TaskPriority.HIGH)
    .withDueDate(now.plusDays(1))
    .withStatus(TaskStatus.TODO)
    .build();
```

### Strategy 2: Factory Methods

```java
private Task createHighPriorityTask() {
    Task task = new Task("", "", TaskPriority.HIGH, null, List.of());
    task.setUpdatedAt(LocalDateTime.now().minusDays(10));
    return task;
}
```

### Strategy 3: Test Fixtures

```java
@BeforeEach
void setUp() {
    task = new Task(...);
    task.setStatus(TaskStatus.TODO);
    // ... common setup
}
```

---

## Assertion Messages

**Good practice**: Always include assertion messages

```java
// ❌ Bad - No message
assertEquals(30, score);

// ✅ Good - Clear message
assertEquals(30, score, 
    "HIGH priority (3) * 10 should equal 30 points");

// ✅ Better - Explains calculation
assertEquals(30, score, 
    "HIGH priority calculation: priority_value(3) * base_weight(10) = 30");
```

---

## Using @DisplayName for Readability

```java
@DisplayName("Task Priority Calculations")
class TaskPriorityTests {
    
    @Test
    @DisplayName("LOW priority multiplied by 10 equals 10")
    void testLowPriority() {
        assertEquals(10, score);
    }
    
    @Test
    @DisplayName("HIGH priority task due tomorrow includes due-soon bonus")
    void testHighPriorityDueTomorrow() {
        assertEquals(30 + 15, score);
    }
}
```

Output in test report:
```
Task Priority Calculations
  ✓ LOW priority multiplied by 10 equals 10
  ✓ HIGH priority task due tomorrow includes due-soon bonus
```

---

## When to Use Each Testing Tool

| Scenario | Tool/Approach |
|----------|---------------|
| Testing pure function | Unit test with fixed inputs |
| Testing date logic | Fixed reference time parameter |
| Testing list operations | Multiple test cases with different sizes |
| Testing error handling | `assertThrows()` for exceptions |
| Testing multiple components | Integration test |
| Testing time-dependent code | Refactor to accept time parameter |
| Testing UI (if applicable) | Integration test with test fixtures |

---

## Red-Green-Refactor Cycle

### 1. RED: Write Failing Test
```java
@Test
void testNewFeature() {
    // Test new functionality that doesn't exist yet
    assertEquals(expectedValue, actualValue);  // FAILS
}
```

### 2. GREEN: Implement Minimal Code
```java
public int calculateTaskScore(Task task) {
    // Add ONLY what's needed to make test pass
    // Don't over-engineer
}
```

### 3. REFACTOR: Improve Code
```java
// After test passes:
// - Remove duplication
// - Improve readability
// - Optimize if needed
// Run tests again to verify refactoring didn't break anything
```

---

## Quick Assessment Checklist

Before submitting tests, verify:

- [ ] Test names are descriptive
- [ ] Each test tests one behavior
- [ ] Assertions have meaningful messages
- [ ] No hardcoded magic numbers (or explained)
- [ ] Tests don't depend on execution order
- [ ] No time-dependent assertions (unless using fixed time)
- [ ] Edge cases are covered
- [ ] Empty/null cases are handled
- [ ] Tests are readable and maintainable
- [ ] All tests pass
- [ ] No unnecessary mocking

---

## Resources

1. **Main Document**: `Using AI to help with testing.md` (1,575 lines)
   - Complete exercise with all 4 parts
   - Detailed explanations and AI conversations
   - Reflection on learning outcomes

2. **Test Code**: `TaskPriorityManagerTest.java`
   - 44 comprehensive test cases
   - Ready to implement in project
   - Organized by behavior type

3. **This Guide**: Quick reference for best practices
   - Checklists and patterns
   - Common mistakes and fixes
   - Quick lookup while writing tests

---

**Last Updated**: April 13, 2026  
**Exercise Status**: ✅ Complete  
**Confidence Level**: 77.5%  
**Recommended Reading Time**: 30 minutes for quick reference  

