# Grade-Tracker
This Java/IntelliJ-runnable application allows you to use a virtual grade book to calculate students' averages in their classes, complete with edit/add/delete functionality for both individual students and grades they possess.

## Course Topic Map

This table lists the core Java/Software Engineering topics demonstrated in the project and maps them to the specific class or method where they are implemented.

| Course Topic | Location (Class/Method) | Description of Implementation |
| :--- | :--- | :--- |
| **Interfaces** | `Gradable.java` | Defines the contract (`calculateAverage()`, `getLetterScore()`) that any graded entity, specifically the `Student` class, must implement. |
| **Inheritance** | `GradeTrackerApp.java` | Extends the abstract class `javafx.application.Application` and overrides its lifecycle methods (`init()`, `start()`, and `stop()`). |
| **Multithreading / Concurrency** | `AutosaveTask.java` (`run()`) | Implements the `Runnable` interface and runs on a daemon `Thread` (`autosaveThread`) to perform non-blocking, periodic data saving. |
| **Collections / Data Structures** | `Student.java` (The `scores` field) | Uses `java.util.List<Integer>` (`scores`) backed by `ArrayList` to dynamically store all assessment scores for a student. |
| **File I/O / Persistence** | `FileRosterStorage.java` | Handles reading and writing student data to the persistent `roster.csv` file. |
| **Exception Handling (I/O)** | `FileRosterStorage.java` (`loadRoster()`) | Uses a `try-catch` block to handle checked exceptions like `IOException` during file operations and `NumberFormatException` during score parsing. |
| **Lambda Expressions** | `GradeTrackerApp.java` (Toolbar methods) | Uses lambda expressions (e.g., `_ -> handleAddStudent()`) for concise and functional event handling on JavaFX buttons. |
| **Stream API** | `FileRosterStorage.java` (`loadRoster()`) | Uses `Arrays.stream()` to process the scores from the loaded CSV line, including `map()` and `collect()`. |
| **Encapsulation / Immutability** | `RosterService.java` (`getRoster()`) | Returns the roster using `Collections.unmodifiableList(roster)` to prevent unauthorized external modification of the core data list. |
| **Encapsulation / Accessors** | `Student.java` (private fields) | Protects internal state (like `name` and `scores`) by making them `private` and providing controlled access via methods like `getName()` and `setScores()`. |