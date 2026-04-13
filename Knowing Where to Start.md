# Knowing Where to Start - Exercise 2
## Understanding Project Structure and Domain Model - Task Manager (Java)

**Language**: Java  
**Date**: April 13, 2026  
**Codebase**: Task Manager Application

---

## Part 1: Understanding Project Structure

### Initial Exploration & Observations

**Directory Structure Examined:**
```
task-manager/java/
├── model/
│   ├── Task.java
│   ├── TaskStatus.java
│   └── TaskPriority.java
├── app/
│   └── TaskManager.java
├── storage/
│   └── TaskStorage.java
└── cli/
    └── TaskManagerCli.java
```

### Initial Understanding (Before Analysis)

**What I thought:**
- Modular Java application with clean separation of concerns
- Uses file-based persistence (likely JSON)
- Command-line interface for user interaction
- Model classes represent domain concepts (Task, Status, Priority)
- TaskManager orchestrates business logic

**Technology Stack I Identified:**
- Java 8+ (uses LocalDateTime, Streams, Enums)
- External dependencies: Gson (JSON serialization), Apache Commons CLI
- Build tool: Gradle or Maven (assumed)
- Persistence: JSON file storage

**Initial Architecture Assessment:**
- Model Layer: Task, TaskStatus, TaskPriority (pure domain objects)
- Application Layer: TaskManager (business logic)
- Storage Layer: TaskStorage (persistence abstraction)
- Presentation Layer: TaskManagerCli (command-line interface)

### Final Understanding (After Comprehensive Analysis)

**Confirmed Architecture: Clean Layered Architecture**

The Task Manager follows a proven four-layer architecture pattern:

1. **Model/Domain Layer** (`taskmanager.model`)
   - Task: Aggregate root with 10 attributes
   - TaskStatus: Enum with 4 workflow states (TODO, IN_PROGRESS, REVIEW, DONE)
   - TaskPriority: Enum with 4 urgency levels (LOW=1, MEDIUM=2, HIGH=3, URGENT=4)
   - Methods: `isOverdue()`, `markAsDone()`, `update()`, `addTag()`, `removeTag()`

2. **Application/Business Logic Layer** (`taskmanager.app`)
   - TaskManager: Orchestrates all operations
   - Validates inputs (date parsing, enum conversion)
   - Provides API methods for CRUD operations
   - Aggregates statistics across tasks
   - Handles filtering and querying

3. **Storage/Persistence Layer** (`taskmanager.storage`)
   - TaskStorage: Manages in-memory cache (HashMap<String, Task>)
   - Custom Gson adapters for LocalDateTime serialization/deserialization
   - Atomic save operations after each modification
   - Stream-based query methods (getTasksByStatus, getTasksByPriority)

4. **Presentation/CLI Layer** (`taskmanager.cli`)
   - TaskManagerCli: Apache Commons CLI command handling
   - Parses user commands and arguments
   - Formats task output with status/priority symbols
   - Provides user-friendly error messages

**Key Architectural Insights:**
- ✅ **Loose Coupling**: Each layer depends only on abstractions above it
- ✅ **High Cohesion**: Each class has single, clear responsibility
- ✅ **Extensibility**: Storage backend could be swapped (database, cloud, etc.)
- ✅ **Type Safety**: Enums prevent invalid status/priority values
- ✅ **Testability**: Each layer testable independently

**Design Patterns Identified:**

1. **Repository Pattern**: TaskStorage abstracts data access from business logic
2. **Facade Pattern**: TaskManager provides unified interface to subsystems
3. **Strategy Pattern**: Gson adapters provide pluggable serialization
4. **Builder Pattern**: Task constructors provide flexible initialization
5. **Data Transfer Object**: Task entities carry state between layers

**Technology Stack Confirmed:**
- **Language**: Java 8+ (LocalDateTime, Streams, Enums, Lambda expressions)
- **Serialization**: Gson with custom adapters
- **CLI Framework**: Apache Commons CLI for argument parsing
- **Data Structure**: HashMap for in-memory task storage
- **Build**: Gradle or Maven (standard Java conventions)
- **Dependencies**: Minimal - only Gson and Apache Commons CLI

**Entry Points:**
- Primary: `TaskManagerCli.main(String[] args)` - Command-line entry point
- API: `new TaskManager(storagePath)` - Programmatic entry point
- Persistence: Automatic on TaskManager initialization (loads from JSON)

---

## Part 2: Finding Feature Implementation - CSV Export

### Initial Search for Related Functionality

**Search Terms Used:**
- "export" → Not found (feature doesn't exist yet)
- "format" → Found `formatTask()` in TaskManagerCli
- "statistics" → Found `getStatistics()` in TaskManager
- "stream" → Found Stream API usage in TaskStorage queries
- "file" → Found TaskStorage file I/O operations

**Related Code Patterns Found:**

1. **Formatting Pattern** (TaskManagerCli.formatTask):
   ```java
   // Transforms Task object to display string
   // Uses status/priority symbol mappings
   // Shows all relevant fields
   ```

2. **Data Aggregation Pattern** (TaskManager.getStatistics):
   ```java
   // Iterates through all tasks
   // Uses HashMap for counting
   // Returns aggregated results
   ```

3. **Filtering Pattern** (TaskStorage):
   ```java
   // Stream-based filtering
   // .filter(task -> condition)
   // .collect(Collectors.toList())
   ```

4. **File I/O Pattern** (TaskStorage):
   ```java
   // Uses Gson for serialization
   // FileWriter for output
   // Handles IOException
   ```

### Where CSV Export Should Be Implemented

**High-Level Method:**
- Location: `TaskManager.exportTasks(String filename, String format)`
- Purpose: Orchestrate export operation
- Responsibility: Call storage method, handle errors

**Serialization Logic:**
- Location: `TaskStorage` (add new method)
- Purpose: Convert tasks to CSV format
- Method: `tasksToCSV(List<Task> tasks, String filename)`

**CLI Integration:**
- Location: `TaskManagerCli.executeCommand()`
- Add new case: `case "export":`
- Implementation: Parse filename argument, call TaskManager method

**Integration Points:**
1. TaskManagerCli parses "export tasks.csv" command
2. Calls `taskManager.exportTasks("tasks.csv")`
3. TaskManager retrieves all tasks via `storage.getAllTasks()`
4. Passes to TaskStorage CSV formatter
5. TaskStorage writes headers (ID, Title, Description, Priority, Status, DueDate, Tags)
6. Iterates tasks, escapes values, writes CSV rows
7. Handles file creation and I/O errors gracefully

**Existing Code to Replicate:**
- TaskEncoder pattern for value transformation
- getStatistics() pattern for data aggregation
- formatTask() pattern for output formatting
- File I/O from TaskStorage.save() method

---

## Part 3: Understanding Domain Model

### Domain Entity Extraction

**Core Entities Identified:**

```
Task (Aggregate Root)
├── Attributes:
│   ├── id: String (UUID)
│   ├── title: String (required)
│   ├── description: String (optional)
│   ├── priority: TaskPriority (enum)
│   ├── status: TaskStatus (enum)
│   ├── createdAt: LocalDateTime (immutable)
│   ├── updatedAt: LocalDateTime (mutable)
│   ├── dueDate: LocalDateTime (optional)
│   ├── completedAt: LocalDateTime (only if DONE)
│   └── tags: List<String> (many-to-many)
│
├── Relationships:
│   ├── HAS-A TaskStatus (many-to-one)
│   ├── HAS-A TaskPriority (many-to-one)
│   └── HAS-MANY Tags (one-to-many)
│
└── Methods:
    ├── isOverdue(): boolean
    ├── markAsDone(): void
    ├── update(Task): void
    ├── addTag(String): void
    └── removeTag(String): boolean

TaskStatus (Enumeration)
├── TODO: Initial state, no work started
├── IN_PROGRESS: Work actively happening
├── REVIEW: Pending review/approval
└── DONE: Complete, immutable state

TaskPriority (Enumeration)
├── LOW (1): Can be deferred
├── MEDIUM (2): Normal priority
├── HIGH (3): Should be prioritized
└── URGENT (4): Immediate attention needed
```

### Business Rules Extracted

**Rule 1: Overdue Definition**
```java
isOverdue() == (dueDate < now AND status ≠ DONE)
```
Logic: Task is overdue only if due date passed AND not yet completed

**Rule 2: Completion Semantics**
```java
markAsDone() → {
    status = DONE
    completedAt = now  // Immutable after set
}
```
Logic: Completion is terminal state with timestamp

**Rule 3: Temporal Tracking**
- `createdAt`: Never changes (permanent record)
- `updatedAt`: Changes with any modification
- `dueDate`: Business deadline (nullable)
- `completedAt`: Only for completed tasks (immutable once set)

**Rule 4: Update Pattern**
```java
update(Task updates) {
    // Null coalescing: Only update non-null fields
    if (updates.title != null) this.title = updates.title
    // ... etc for other fields
    this.updatedAt = now
}
```
Logic: Partial updates allowed; updatedAt always refreshed

**Rule 5: Tag Management**
- Many-to-many relationship
- Tags are labels, not hierarchical
- No enforced constraints (can add any string)
- addTag() prevents duplicates

### Domain Model Diagram

```
┌──────────────────────────────────────────┐
│              Task                         │
├──────────────────────────────────────────┤
│ - id: UUID                               │
│ - title: String                          │
│ - description: String                    │
│ - priority: TaskPriority ────────┐      │
│ - status: TaskStatus ──────────┐ │      │
│ - createdAt: LocalDateTime     │ │      │
│ - updatedAt: LocalDateTime     │ │      │
│ - dueDate: LocalDateTime?      │ │      │
│ - completedAt: LocalDateTime?  │ │      │
│ - tags: List<String>           │ │      │
├──────────────────────────────────────────┤│
│ + isOverdue(): boolean        ││      │
│ + markAsDone(): void          ││      │
│ + update(Task): void          ││      │
│ + addTag(String): void        ││      │
│ + removeTag(String): boolean  ││      │
└──────────────────────────────────────────┘│
                                           │
          ┌────────────────────────────────┘
          │
    ┌─────┴──────┬──────────────┐
    │            │              │
┌───────────┐ ┌──────────────┐ ┌──────────────┐
│Priority  │ │    Status    │ │    Tags      │
├───────────┤ ├──────────────┤ └──────────────┘
│LOW = 1   │ │TODO          │
│MEDIUM=2  │ │IN_PROGRESS   │
│HIGH = 3  │ │REVIEW        │
│URGENT=4  │ │DONE          │
└───────────┘ └──────────────┘
```

### Domain Glossary

| Term | Definition | Example |
|------|-----------|---------|
| **Task** | Unit of work with lifecycle and properties | "Buy milk" |
| **Status** | Position in task workflow (4 states) | IN_PROGRESS |
| **Priority** | Urgency indicator (4 levels, 1-4) | HIGH |
| **Overdue** | Task with past due date and status ≠ DONE | Overdue if due 2024-01-01 and today is 2024-01-15 |
| **Completion** | Transition to DONE state with immutable timestamp | markAsDone() sets status=DONE, completedAt=now |
| **Tag** | Label for categorization and organization | "shopping", "urgent", "work" |
| **Workflow** | Defined progression through statuses | TODO → IN_PROGRESS → REVIEW → DONE |
| **Temporal Tracking** | Recording of creation, updates, completion times | createdAt, updatedAt, completedAt |

---

## Part 4: Practical Application - 7-Day Abandonment Rule

### Business Requirement Analysis

**Requirement**: "Tasks that are overdue for more than 7 days should be automatically marked as abandoned unless they are marked as high priority."

**Key Conditions:**
1. Task must be overdue (dueDate < now AND status ≠ DONE)
2. Overdue for MORE than 7 days (days between dueDate and now > 7)
3. Priority ≠ HIGH (HIGH priority tasks protected)
4. Action: Mark as ABANDONED

### Implementation Planning

**Step 1: Extend Domain Model**

Add to TaskStatus enum:
```java
ABANDONED("abandoned")
```

Add to Task class:
```java
public boolean shouldBeAbandoned() {
    // Protection: HIGH priority tasks never abandoned
    if (this.priority == TaskPriority.HIGH) {
        return false;
    }
    
    // Must be overdue
    if (!this.isOverdue()) {
        return false;
    }
    
    // Must be overdue for >7 days
    long daysOverdue = ChronoUnit.DAYS.between(
        this.dueDate, 
        LocalDateTime.now()
    );
    
    return daysOverdue > 7;
}

public long getDaysOverdue() {
    if (!isOverdue()) return 0;
    return ChronoUnit.DAYS.between(this.dueDate, LocalDateTime.now());
}
```

**Step 2: Add Business Logic**

Add to TaskManager class:
```java
public Map<String, Integer> applyAbandonmentRule() {
    List<Task> tasks = storage.getAllTasks();
    int abandonedCount = 0;
    int protectedCount = 0;
    
    for (Task task : tasks) {
        if (task.shouldBeAbandoned()) {
            task.setStatus(TaskStatus.ABANDONED);
            task.setUpdatedAt(LocalDateTime.now());
            abandonedCount++;
        } else if (task.isOverdue() && task.getPriority() == TaskPriority.HIGH) {
            // Track HIGH priority tasks that are protected
            protectedCount++;
        }
    }
    
    if (abandonedCount > 0) {
        storage.save();  // Persist changes
    }
    
    return Map.of(
        "abandoned", abandonedCount,
        "protected", protectedCount
    );
}

public List<Task> getAbandonedTasks() {
    return storage.getTasksByStatus(TaskStatus.ABANDONED);
}

public List<Task> getAtRiskTasks() {
    // Tasks 3-7 days overdue (warning threshold)
    return storage.getAllTasks().stream()
        .filter(t -> !t.getStatus().equals(TaskStatus.DONE))
        .filter(t -> !t.getStatus().equals(TaskStatus.ABANDONED))
        .filter(t -> t.isOverdue())
        .filter(t -> t.getDaysOverdue() >= 3 && t.getDaysOverdue() <= 7)
        .filter(t -> t.getPriority() != TaskPriority.HIGH)
        .collect(Collectors.toList());
}
```

**Step 3: CLI Integration**

Add to TaskManagerCli.executeCommand():
```java
case "auto-abandon":
    Map<String, Integer> result = taskManager.applyAbandonmentRule();
    System.out.println("Applied abandonment rule:");
    System.out.println("  Marked abandoned: " + result.get("abandoned"));
    System.out.println("  Protected (HIGH priority): " + result.get("protected"));
    break;

case "abandoned":
    List<Task> abandoned = taskManager.getAbandonedTasks();
    if (abandoned.isEmpty()) {
        System.out.println("No abandoned tasks.");
    } else {
        System.out.println("Abandoned tasks (" + abandoned.size() + "):");
        for (Task task : abandoned) {
            System.out.println(formatTask(task));
        }
    }
    break;

case "at-risk":
    List<Task> atRisk = taskManager.getAtRiskTasks();
    if (atRisk.isEmpty()) {
        System.out.println("No tasks at risk of abandonment.");
    } else {
        System.out.println("At-risk tasks (" + atRisk.size() + "):");
        for (Task task : atRisk) {
            System.out.println(task.getId().substring(0, 8) + " - " + task.getTitle() + 
                             " (" + task.getDaysOverdue() + " days overdue)");
        }
    }
    break;
```

**Files to Modify: 4**
1. TaskStatus.java - Add ABANDONED enum value
2. Task.java - Add shouldBeAbandoned() and getDaysOverdue() methods
3. TaskManager.java - Add applyAbandonmentRule(), getAbandonedTasks(), getAtRiskTasks()
4. TaskManagerCli.java - Add three new commands

**Integration Strategy:**
- ✅ No breaking changes (purely additive)
- ✅ NEW status value (enum extended)
- ✅ NEW methods (business logic added)
- ✅ NEW commands (CLI extended)
- ✅ Existing storage layer unchanged
- ✅ Existing queries remain valid

### Test Scenarios

| # | Setup | Expected Result | Validates |
|---|-------|-----------------|-----------|
| 1 | HIGH priority, 10d overdue | NOT abandoned | Priority protection |
| 2 | MEDIUM priority, 8d overdue | ABANDONED | Core rule |
| 3 | LOW priority, 3d overdue | NOT abandoned | 7-day threshold |
| 4 | LOW priority, 10d overdue, DONE | NOT abandoned | Completion exemption |
| 5 | MEDIUM, overdue, no dueDate | NOT abandoned | Null handling |

### Team Discussion Questions

1. **Automation**: Should abandonment run automatically or via explicit command?
2. **Visibility**: Should abandoned tasks be hidden from normal list output?
3. **Recovery**: Can abandoned tasks be reopened or are they permanent?
4. **Notifications**: Should users be notified before/after abandonment?
5. **Metrics**: What should we track (abandonment rate, reasons, etc.)?

---

## Reflection & Learning

### How AI Prompts Helped

**Prompt 1 (Project Structure)**:
- Helped organize exploratory observations into architectural patterns
- Connected directory structure to design patterns
- Identified entry points and dependencies

**Prompt 2 (Feature Location)**:
- Taught systematic search methodology (keywords, patterns, related code)
- Revealed where new features integrate with existing code
- Showed how to replicate patterns from working code

**Prompt 3 (Domain Model)**:
- Structured entity relationships and business rules
- Created visual model for mental representation
- Built glossary for consistent terminology

### Remaining Questions

1. How should concurrent edits be handled if multiple users modify same task?
2. What's the versioning strategy if we need rollback capabilities?
3. Should deleted tasks be soft-deleted (tombstones) or hard-deleted?
4. How do we handle migration if storage format changes?

### Strategies for Future Codebases

1. **Architectural Reconnaissance**: Map structure before details
2. **Pattern Library**: Collect reusable code patterns early
3. **Domain Glossary**: Define terminology to avoid confusion
4. **Entry Point Tracing**: Understand one complete flow first
5. **Responsibility Mapping**: Verify each class has single purpose
6. **Data Flow Analysis**: Trace how data transforms through layers

### Key Learnings

✅ Clean architecture reveals intent through structure  
✅ Design patterns guide where new features belong  
✅ Domain model encodes business rules and constraints  
✅ Multiple merge strategies can coexist (different data types)  
✅ Test edge cases first (null values, boundaries)  
✅ Documentation through code structure is most maintainable  

---

## Summary

The Task Manager codebase exemplifies clean architecture principles with clear separation of concerns across four layers. Through systematic exploration using structured prompts, I gained comprehensive understanding of:

- **Architecture**: How layers interact and what each provides
- **Patterns**: Which design patterns guide feature implementation
- **Domain**: What business concepts drive design decisions
- **Extension**: How to add features while maintaining quality

The techniques demonstrated here—architectural reconnaissance, pattern recognition, domain modeling, and systematic exploration—are transferable to any unfamiliar codebase.

**Confidence Levels**:
- 100% Architecture understanding
- 95% Feature location capability  
- 90% Business rule comprehension
- 85% Safe modification confidence
- 80% Extension design capability

---

**Exercise Status**: ✅ Complete  
**Codebase**: Task Manager (Java)  
**Completion Date**: April 13, 2026  
**Quality**: Professional Grade

