package za.co.wethinkcode.taskmanager.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import za.co.wethinkcode.taskmanager.model.Task;
import za.co.wethinkcode.taskmanager.model.TaskPriority;
import za.co.wethinkcode.taskmanager.model.TaskStatus;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive test suite for TaskPriorityManager.
 *
 * This test suite was developed using AI-guided learning principles:
 * 1. Understanding What to Test - Behavior analysis and edge cases
 * 2. Improving Tests - Focus on quality and clarity
 * 3. Test-Driven Development - Red-Green-Refactor cycle
 * 4. Integration Testing - Full workflow verification
 *
 * Test Organization:
 * - Unit tests for calculateTaskScore (by factor type)
 * - Unit tests for sortTasksByImportance
 * - Unit tests for getTopPriorityTasks
 * - Integration tests for full workflow
 * - Edge case tests
 */
@DisplayName("TaskPriorityManager Tests")
class TaskPriorityManagerTest {

    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        // Use fixed time for deterministic tests
        now = LocalDateTime.of(2026, 4, 13, 10, 0, 0);
    }

    // ============================================================================
    // PART 1: Unit Tests for calculateTaskScore - Priority Weights
    // ============================================================================

    @Nested
    @DisplayName("Priority Weight Tests")
    class PriorityWeightTests {

        @Test
        @DisplayName("LOW priority task scores 10")
        void testLowPriorityCalculation() {
            Task task = new Task("Low Priority Task", "", TaskPriority.LOW, null, List.of());
            task.setStatus(TaskStatus.TODO);
            task.setUpdatedAt(now.minusDays(10));

            int score = TaskPriorityManager.calculateTaskScore(task);

            assertEquals(10, score,
                    "LOW priority (1) * 10 = 10 points");
        }

        @Test
        @DisplayName("MEDIUM priority task scores 20")
        void testMediumPriorityCalculation() {
            Task task = new Task("Medium Priority Task", "", TaskPriority.MEDIUM, null, List.of());
            task.setStatus(TaskStatus.TODO);
            task.setUpdatedAt(now.minusDays(10));

            int score = TaskPriorityManager.calculateTaskScore(task);

            assertEquals(20, score,
                    "MEDIUM priority (2) * 10 = 20 points");
        }

        @Test
        @DisplayName("HIGH priority task scores 30")
        void testHighPriorityCalculation() {
            Task task = new Task("High Priority Task", "", TaskPriority.HIGH, null, List.of());
            task.setStatus(TaskStatus.TODO);
            task.setUpdatedAt(now.minusDays(10));

            int score = TaskPriorityManager.calculateTaskScore(task);

            assertEquals(30, score,
                    "HIGH priority (3) * 10 = 30 points");
        }

        @Test
        @DisplayName("URGENT priority task scores 40")
        void testUrgentPriorityCalculation() {
            Task task = new Task("Urgent Task", "", TaskPriority.URGENT, null, List.of());
            task.setStatus(TaskStatus.TODO);
            task.setUpdatedAt(now.minusDays(10));

            int score = TaskPriorityManager.calculateTaskScore(task);

            assertEquals(40, score,
                    "URGENT priority (4) * 10 = 40 points");
        }
    }

    // ============================================================================
    // PART 2: Unit Tests for calculateTaskScore - Due Date Factors
    // ============================================================================

    @Nested
    @DisplayName("Due Date Factor Tests")
    class DueDateFactorTests {

        @Test
        @DisplayName("Overdue task gets +30 bonus")
        void testOverdueTaskBonus() {
            LocalDateTime overdueDate = now.minusDays(5);
            Task task = new Task("Overdue Task", "", TaskPriority.MEDIUM, overdueDate, List.of());
            task.setStatus(TaskStatus.TODO);
            task.setUpdatedAt(now.minusDays(10));

            int score = TaskPriorityManager.calculateTaskScore(task);

            // MEDIUM (20) + overdue (30) = 50
            // Note: May vary by seconds due to now() in function
            assertTrue(score >= 48 && score <= 52,
                    "Overdue task should score ~50 (MEDIUM + overdue bonus)");
        }

        @Test
        @DisplayName("Task due today gets +20 bonus")
        void testDueTodayBonus() {
            LocalDateTime dueToday = now;
            Task task = new Task("Due Today", "", TaskPriority.MEDIUM, dueToday, List.of());
            task.setStatus(TaskStatus.TODO);
            task.setUpdatedAt(now.minusDays(10));

            int score = TaskPriorityManager.calculateTaskScore(task);

            // MEDIUM (20) + due today (20) = 40
            assertTrue(score >= 38 && score <= 42,
                    "Task due today should score ~40");
        }

        @Test
        @DisplayName("Task due within 2 days gets +15 bonus")
        void testDueWithinTwoDaysBonus() {
            LocalDateTime dueSoon = now.plusDays(1);
            Task task = new Task("Due Soon", "", TaskPriority.LOW, dueSoon, List.of());
            task.setStatus(TaskStatus.TODO);
            task.setUpdatedAt(now.minusDays(10));

            int score = TaskPriorityManager.calculateTaskScore(task);

            // LOW (10) + within 2 days (15) = 25
            assertTrue(score >= 23 && score <= 27,
                    "Task due within 2 days should score ~25");
        }

        @Test
        @DisplayName("Task due within week gets +10 bonus")
        void testDueWithinWeekBonus() {
            LocalDateTime dueWeek = now.plusDays(5);
            Task task = new Task("Due This Week", "", TaskPriority.MEDIUM, dueWeek, List.of());
            task.setStatus(TaskStatus.TODO);
            task.setUpdatedAt(now.minusDays(10));

            int score = TaskPriorityManager.calculateTaskScore(task);

            // MEDIUM (20) + within week (10) = 30
            assertTrue(score >= 28 && score <= 32,
                    "Task due within week should score ~30");
        }

        @Test
        @DisplayName("Task with no due date gets no date bonus")
        void testNoDueDateNoBonus() {
            Task task = new Task("No Deadline", "", TaskPriority.HIGH, null, List.of());
            task.setStatus(TaskStatus.TODO);
            task.setUpdatedAt(now.minusDays(10));

            int score = TaskPriorityManager.calculateTaskScore(task);

            // HIGH (30) + no date bonus (0) = 30
            assertEquals(30, score,
                    "Task without due date should only have priority score");
        }
    }

    // ============================================================================
    // PART 3: Unit Tests for calculateTaskScore - Status Penalties
    // ============================================================================

    @Nested
    @DisplayName("Status Penalty Tests")
    class StatusPenaltyTests {

        @Test
        @DisplayName("TODO status has no penalty")
        void testTodoStatusNoPenalty() {
            Task task = new Task("Todo Task", "", TaskPriority.MEDIUM, null, List.of());
            task.setStatus(TaskStatus.TODO);
            task.setUpdatedAt(now.minusDays(10));

            int score = TaskPriorityManager.calculateTaskScore(task);

            assertEquals(20, score,
                    "TODO status should not affect score");
        }

        @Test
        @DisplayName("IN_PROGRESS status has no penalty")
        void testInProgressStatusNoPenalty() {
            Task task = new Task("In Progress Task", "", TaskPriority.MEDIUM, null, List.of());
            task.setStatus(TaskStatus.IN_PROGRESS);
            task.setUpdatedAt(now.minusDays(10));

            int score = TaskPriorityManager.calculateTaskScore(task);

            assertEquals(20, score,
                    "IN_PROGRESS status should not affect score");
        }

        @Test
        @DisplayName("REVIEW status reduces score by 15")
        void testReviewStatusPenalty() {
            Task task = new Task("Under Review", "", TaskPriority.MEDIUM, null, List.of());
            task.setStatus(TaskStatus.REVIEW);
            task.setUpdatedAt(now.minusDays(10));

            int score = TaskPriorityManager.calculateTaskScore(task);

            // MEDIUM (20) - review penalty (15) = 5
            assertEquals(5, score,
                    "REVIEW status should reduce score by 15");
        }

        @Test
        @DisplayName("DONE status reduces score by 50")
        void testDoneStatusPenalty() {
            Task task = new Task("Completed Task", "", TaskPriority.MEDIUM, null, List.of());
            task.setStatus(TaskStatus.DONE);
            task.setUpdatedAt(now.minusDays(10));

            int score = TaskPriorityManager.calculateTaskScore(task);

            // MEDIUM (20) - done penalty (50) = -30
            assertEquals(-30, score,
                    "DONE status should reduce score by 50");
        }
    }

    // ============================================================================
    // PART 4: Unit Tests for calculateTaskScore - Tag Boosts
    // ============================================================================

    @Nested
    @DisplayName("Tag Boost Tests")
    class TagBoostTests {

        @Test
        @DisplayName("Task with no tags gets no boost")
        void testNoTagsNoBooast() {
            Task task = new Task("No Tags", "", TaskPriority.MEDIUM, null, List.of());
            task.setStatus(TaskStatus.TODO);
            task.setUpdatedAt(now.minusDays(10));

            int score = TaskPriorityManager.calculateTaskScore(task);

            assertEquals(20, score,
                    "Task without tags should have no tag boost");
        }

        @Test
        @DisplayName("Task with blocker tag gets +8 boost")
        void testBlockerTagBoost() {
            Task task = new Task("Blocker Bug", "", TaskPriority.MEDIUM, null, List.of("blocker"));
            task.setStatus(TaskStatus.TODO);
            task.setUpdatedAt(now.minusDays(10));

            int score = TaskPriorityManager.calculateTaskScore(task);

            // MEDIUM (20) + blocker (8) = 28
            assertEquals(28, score,
                    "Task with blocker tag should get +8 boost");
        }

        @Test
        @DisplayName("Task with critical tag gets +8 boost")
        void testCriticalTagBoost() {
            Task task = new Task("Critical Issue", "", TaskPriority.MEDIUM, null, List.of("critical"));
            task.setStatus(TaskStatus.TODO);
            task.setUpdatedAt(now.minusDays(10));

            int score = TaskPriorityManager.calculateTaskScore(task);

            // MEDIUM (20) + critical (8) = 28
            assertEquals(28, score,
                    "Task with critical tag should get +8 boost");
        }

        @Test
        @DisplayName("Task with urgent tag gets +8 boost")
        void testUrgentTagBoost() {
            Task task = new Task("Urgent Request", "", TaskPriority.MEDIUM, null, List.of("urgent"));
            task.setStatus(TaskStatus.TODO);
            task.setUpdatedAt(now.minusDays(10));

            int score = TaskPriorityManager.calculateTaskScore(task);

            // MEDIUM (20) + urgent (8) = 28
            assertEquals(28, score,
                    "Task with urgent tag should get +8 boost");
        }

        @Test
        @DisplayName("Task with multiple tags including critical gets +8 (not multiplied)")
        void testMultipleTagsWithCritical() {
            Task task = new Task("Complex Task", "", TaskPriority.MEDIUM,
                    null, List.of("feature", "critical", "enhancement"));
            task.setStatus(TaskStatus.TODO);
            task.setUpdatedAt(now.minusDays(10));

            int score = TaskPriorityManager.calculateTaskScore(task);

            // MEDIUM (20) + critical (8) = 28 (not 8*3)
            assertEquals(28, score,
                    "Boost should apply once if ANY critical tag present");
        }

        @Test
        @DisplayName("Task with non-critical tags gets no boost")
        void testNonCriticalTagsNoBoost() {
            Task task = new Task("Feature Work", "", TaskPriority.MEDIUM,
                    null, List.of("feature", "enhancement"));
            task.setStatus(TaskStatus.TODO);
            task.setUpdatedAt(now.minusDays(10));

            int score = TaskPriorityManager.calculateTaskScore(task);

            // MEDIUM (20) + no critical tags = 20
            assertEquals(20, score,
                    "Task without critical tags should have no boost");
        }
    }

    // ============================================================================
    // PART 5: Unit Tests for calculateTaskScore - Update Recency
    // ============================================================================

    @Nested
    @DisplayName("Update Recency Tests")
    class UpdateRecencyTests {

        @Test
        @DisplayName("Task updated less than 1 day ago gets +5 bonus")
        void testRecentUpdateBonus() {
            LocalDateTime recentUpdate = now.minusHours(2);
            Task task = new Task("Recently Updated", "", TaskPriority.MEDIUM, null, List.of());
            task.setStatus(TaskStatus.TODO);
            task.setUpdatedAt(recentUpdate);

            int score = TaskPriorityManager.calculateTaskScore(task);

            // MEDIUM (20) + recent (5) = 25
            assertTrue(score >= 23 && score <= 27,
                    "Task updated less than 24 hours ago should get +5 bonus");
        }

        @Test
        @DisplayName("Task updated 1+ days ago gets no recency bonus")
        void testOldUpdateNoBonus() {
            LocalDateTime oldUpdate = now.minusDays(2);
            Task task = new Task("Old Update", "", TaskPriority.MEDIUM, null, List.of());
            task.setStatus(TaskStatus.TODO);
            task.setUpdatedAt(oldUpdate);

            int score = TaskPriorityManager.calculateTaskScore(task);

            // MEDIUM (20) + no bonus = 20
            assertEquals(20, score,
                    "Task updated more than 24 hours ago should have no recency bonus");
        }

        @Test
        @DisplayName("Task updated just now gets +5 bonus")
        void testJustUpdatedBonus() {
            LocalDateTime justNow = now;
            Task task = new Task("Just Updated", "", TaskPriority.MEDIUM, null, List.of());
            task.setStatus(TaskStatus.TODO);
            task.setUpdatedAt(justNow);

            int score = TaskPriorityManager.calculateTaskScore(task);

            assertTrue(score >= 23 && score <= 27,
                    "Task updated just now should get +5 bonus");
        }
    }

    // ============================================================================
    // PART 6: Unit Tests for calculateTaskScore - Combined Factors
    // ============================================================================

    @Nested
    @DisplayName("Combined Factor Tests")
    class CombinedFactorTests {

        @Test
        @DisplayName("Overdue + Blocker + High Priority combines bonuses")
        void testCombinedMaxBonus() {
            LocalDateTime overdueDate = now.minusDays(1);
            Task task = new Task("Critical Blocker", "", TaskPriority.HIGH,
                    overdueDate, List.of("blocker"));
            task.setStatus(TaskStatus.TODO);
            task.setUpdatedAt(now);

            int score = TaskPriorityManager.calculateTaskScore(task);

            // HIGH (30) + overdue (30) + blocker (8) + recent (5) = 73
            assertTrue(score >= 70 && score <= 76,
                    "Should combine all bonuses: HIGH + overdue + blocker + recent");
        }

        @Test
        @DisplayName("DONE status dominates other bonuses")
        void testDoneStatusOverridesOtherBonuses() {
            LocalDateTime overdueDate = now.minusDays(1);
            Task task = new Task("Completed Blocker", "", TaskPriority.URGENT,
                    overdueDate, List.of("critical"));
            task.setStatus(TaskStatus.DONE);
            task.setUpdatedAt(now);

            int score = TaskPriorityManager.calculateTaskScore(task);

            // URGENT (40) + overdue (30) + critical (8) + recent (5) - done (50) = 33
            assertTrue(score >= 30 && score <= 36,
                    "DONE status reduces score even with other bonuses");
        }

        @Test
        @DisplayName("Multiple negative factors can result in negative score")
        void testNegativeScorePossible() {
            Task task = new Task("Old, Low, Done", "", TaskPriority.LOW, null, List.of());
            task.setStatus(TaskStatus.DONE);
            task.setUpdatedAt(now.minusDays(30));

            int score = TaskPriorityManager.calculateTaskScore(task);

            // LOW (10) - done (50) = -40
            assertEquals(-40, score,
                    "Score can be negative with low priority and done status");
        }
    }

    // ============================================================================
    // PART 7: Unit Tests for sortTasksByImportance
    // ============================================================================

    @Nested
    @DisplayName("Sort By Importance Tests")
    class SortByImportanceTests {

        @Test
        @DisplayName("Tasks are sorted in descending score order")
        void testTasksSortedByScoreDescending() {
            List<Task> tasks = new ArrayList<>();
            tasks.add(createTask("Low Priority", TaskPriority.LOW));      // Score: 10
            tasks.add(createTask("High Priority", TaskPriority.HIGH));    // Score: 30
            tasks.add(createTask("Medium Priority", TaskPriority.MEDIUM)); // Score: 20

            List<Task> sorted = TaskPriorityManager.sortTasksByImportance(tasks);

            assertEquals("High Priority", sorted.get(0).getTitle());
            assertEquals("Medium Priority", sorted.get(1).getTitle());
            assertEquals("Low Priority", sorted.get(2).getTitle());
        }

        @Test
        @DisplayName("Empty task list returns empty")
        void testSortEmptyList() {
            List<Task> empty = new ArrayList<>();

            List<Task> sorted = TaskPriorityManager.sortTasksByImportance(empty);

            assertTrue(sorted.isEmpty());
        }

        @Test
        @DisplayName("Single task returns list with single task")
        void testSortSingleTask() {
            List<Task> single = new ArrayList<>();
            single.add(createTask("Only Task", TaskPriority.MEDIUM));

            List<Task> sorted = TaskPriorityManager.sortTasksByImportance(single);

            assertEquals(1, sorted.size());
            assertEquals("Only Task", sorted.get(0).getTitle());
        }

        @Test
        @DisplayName("Tasks with equal scores maintain relative order (stable sort)")
        void testStableSortWithEqualScores() {
            List<Task> tasks = new ArrayList<>();
            Task task1 = createTask("Task A", TaskPriority.MEDIUM);
            Task task2 = createTask("Task B", TaskPriority.MEDIUM);
            Task task3 = createTask("Task C", TaskPriority.MEDIUM);
            tasks.add(task1);
            tasks.add(task2);
            tasks.add(task3);

            List<Task> sorted = TaskPriorityManager.sortTasksByImportance(tasks);

            // All have same score, should maintain insertion order
            assertEquals(3, sorted.size());
            assertEquals("Task A", sorted.get(0).getTitle());
            assertEquals("Task B", sorted.get(1).getTitle());
            assertEquals("Task C", sorted.get(2).getTitle());
        }
    }

    // ============================================================================
    // PART 8: Unit Tests for getTopPriorityTasks
    // ============================================================================

    @Nested
    @DisplayName("Get Top Priority Tasks Tests")
    class GetTopPriorityTasksTests {

        @Test
        @DisplayName("Returns exactly N top tasks")
        void testReturnTopNTasks() {
            List<Task> tasks = new ArrayList<>();
            tasks.add(createTask("Low", TaskPriority.LOW));
            tasks.add(createTask("High 1", TaskPriority.HIGH));
            tasks.add(createTask("Medium", TaskPriority.MEDIUM));
            tasks.add(createTask("High 2", TaskPriority.HIGH));
            tasks.add(createTask("Urgent", TaskPriority.URGENT));

            List<Task> top2 = TaskPriorityManager.getTopPriorityTasks(tasks, 2);

            assertEquals(2, top2.size());
            assertEquals("Urgent", top2.get(0).getTitle());
            assertEquals("High 1", top2.get(1).getTitle());
        }

        @Test
        @DisplayName("Limit exceeds list size returns all tasks")
        void testLimitExceedsListSize() {
            List<Task> tasks = new ArrayList<>();
            tasks.add(createTask("Task 1", TaskPriority.MEDIUM));
            tasks.add(createTask("Task 2", TaskPriority.MEDIUM));

            List<Task> top = TaskPriorityManager.getTopPriorityTasks(tasks, 100);

            assertEquals(2, top.size());
        }

        @Test
        @DisplayName("Limit of zero returns empty list")
        void testLimitZero() {
            List<Task> tasks = new ArrayList<>();
            tasks.add(createTask("Task 1", TaskPriority.MEDIUM));
            tasks.add(createTask("Task 2", TaskPriority.MEDIUM));

            List<Task> top = TaskPriorityManager.getTopPriorityTasks(tasks, 0);

            assertTrue(top.isEmpty());
        }

        @Test
        @DisplayName("Empty task list returns empty")
        void testEmptyListReturnsEmpty() {
            List<Task> empty = new ArrayList<>();

            List<Task> top = TaskPriorityManager.getTopPriorityTasks(empty, 5);

            assertTrue(top.isEmpty());
        }
    }

    // ============================================================================
    // PART 9: Integration Tests
    // ============================================================================

    @Nested
    @DisplayName("Integration Tests")
    class IntegrationTests {

        @Test
        @DisplayName("Full workflow: calculate, sort, get top tasks")
        void testFullWorkflow() {
            List<Task> tasks = createRealisticTaskSet();

            // Execute full workflow
            List<Task> sorted = TaskPriorityManager.sortTasksByImportance(tasks);
            List<Task> top3 = TaskPriorityManager.getTopPriorityTasks(tasks, 3);

            // Verify results
            assertEquals(tasks.size(), sorted.size(), "Sorted list should have all tasks");
            assertEquals(3, top3.size(), "Should return exactly 3 top tasks");

            // Verify top tasks are actually highest scoring
            for (int i = 0; i < top3.size() - 1; i++) {
                int score1 = TaskPriorityManager.calculateTaskScore(top3.get(i));
                int score2 = TaskPriorityManager.calculateTaskScore(top3.get(i + 1));
                assertTrue(score1 >= score2, "Top tasks should be in descending order");
            }
        }

        @Test
        @DisplayName("Real-world scenario: daily standup prioritization")
        void testDailyStandupPrioritization() {
            List<Task> allTasks = new ArrayList<>();

            // Add realistic tasks
            Task blocker = new Task("API timeout in production", "Critical", TaskPriority.URGENT,
                    now.minusDays(2), List.of("blocker", "critical"));
            blocker.setStatus(TaskStatus.TODO);
            blocker.setUpdatedAt(now.minusHours(1));
            allTasks.add(blocker);

            Task clientIssue = new Task("Client login issue", "Auth", TaskPriority.HIGH,
                    now.minusDays(1), List.of());
            clientIssue.setStatus(TaskStatus.TODO);
            clientIssue.setUpdatedAt(now);
            allTasks.add(clientIssue);

            Task feature = new Task("Dark mode feature", "UI", TaskPriority.MEDIUM,
                    now.plusDays(7), List.of("feature"));
            feature.setStatus(TaskStatus.IN_PROGRESS);
            feature.setUpdatedAt(now.minusDays(2));
            allTasks.add(feature);

            Task techDebt = new Task("Refactor payment service", "Backend", TaskPriority.LOW,
                    now.plusDays(30), List.of());
            techDebt.setStatus(TaskStatus.TODO);
            techDebt.setUpdatedAt(now.minusDays(10));
            allTasks.add(techDebt);

            // Get top 3 for standup
            List<Task> standupItems = TaskPriorityManager.getTopPriorityTasks(allTasks, 3);

            assertEquals(3, standupItems.size());
            assertTrue(standupItems.get(0).getTitle().contains("API timeout"),
                    "Critical production bug should be top priority");
            assertFalse(standupItems.stream().anyMatch(t -> t.getTitle().contains("Refactor")),
                    "Low priority tech debt should not be in top 3");
        }
    }

    // ============================================================================
    // Helper Methods
    // ============================================================================

    private Task createTask(String title, TaskPriority priority) {
        Task task = new Task(title, "", priority, null, List.of());
        task.setStatus(TaskStatus.TODO);
        task.setUpdatedAt(now.minusDays(10));
        return task;
    }

    private List<Task> createRealisticTaskSet() {
        List<Task> tasks = new ArrayList<>();

        Task urgent = new Task("Critical Bug", "", TaskPriority.URGENT,
                now.minusDays(1), List.of("blocker"));
        urgent.setStatus(TaskStatus.TODO);
        urgent.setUpdatedAt(now);
        tasks.add(urgent);

        Task high = new Task("Feature Due Soon", "", TaskPriority.HIGH,
                now.plusDays(1), List.of());
        high.setStatus(TaskStatus.IN_PROGRESS);
        high.setUpdatedAt(now.minusDays(2));
        tasks.add(high);

        Task medium = new Task("Regular Task", "", TaskPriority.MEDIUM,
                null, List.of());
        medium.setStatus(TaskStatus.TODO);
        medium.setUpdatedAt(now.minusDays(5));
        tasks.add(medium);

        Task low = new Task("Nice to Have", "", TaskPriority.LOW,
                now.plusDays(30), List.of());
        low.setStatus(TaskStatus.TODO);
        low.setUpdatedAt(now.minusDays(20));
        tasks.add(low);

        Task done = new Task("Completed Work", "", TaskPriority.MEDIUM,
                null, List.of());
        done.setStatus(TaskStatus.DONE);
        done.setUpdatedAt(now.minusDays(15));
        tasks.add(done);

        return tasks;
    }
}

