// GradeTrackerApp.java
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Arrays; // Required for table column setup

public class GradeTrackerApp extends Application {

    private RosterService rosterService;
    private TableView<Student> rosterTable;

    // Fields for managing the background autosave process.
    private AutosaveTask autosaveTask;
    private Thread autosaveThread;

    @Override
    public void init() throws Exception {
        super.init();
        this.rosterService = new RosterService();

        // Initialize and start the background thread for autosaving data.
        this.autosaveTask = new AutosaveTask(rosterService);
        this.autosaveThread = new Thread(autosaveTask);
        this.autosaveThread.setDaemon(true);
        this.autosaveThread.start();
    }

    @Override
    public void start(Stage primaryStage) {
        // Define scene constants locally.
        final int SCENE_WIDTH = 1000;
        final int SCENE_HEIGHT = 700;

        BorderPane root = new BorderPane();

        root.setTop(createToolbar());
        this.rosterTable = createRosterTable();
        root.setCenter(rosterTable);
        root.setBottom(new Label("Status: Application ready. Autosave active every 60s."));

        Scene scene = new Scene(root, SCENE_WIDTH, SCENE_HEIGHT);
        primaryStage.setTitle("Student Grade Tracker");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        if (autosaveThread != null) {
            autosaveTask.stop();
            // Wait for the thread to finish saving
            autosaveThread.join(5000);
            rosterService.saveData();
        }
        System.out.println("LOG: Application closed and final data saved.");
    }

    private ToolBar createToolbar() {
        Button addStudentButton = new Button("Add Student");
        Button editStudentButton = new Button("Edit Student");
        Button deleteStudentButton = new Button("Delete Student");
        Button enterScoresButton = new Button("Enter Scores");
        Button editScoresButton = new Button("Edit Scores");
        Button saveButton = new Button("Save Now");

        addStudentButton.setOnAction(_ -> handleAddStudent());
        editStudentButton.setOnAction(_ -> handleEditStudent());
        deleteStudentButton.setOnAction(_ -> handleDeleteStudent());
        enterScoresButton.setOnAction(_ -> handleAddScore());
        editScoresButton.setOnAction(_ -> handleEditScores());

        saveButton.setOnAction(_ -> {
            rosterService.saveData();
            new Alert(Alert.AlertType.INFORMATION, "Data manually saved!").show();
        });

        return new ToolBar(addStudentButton, editStudentButton, deleteStudentButton, new Separator(),
                enterScoresButton, editScoresButton, new Separator(), saveButton);
    }

    private void handleDeleteStudent() {
        Student selectedStudent = rosterTable.getSelectionModel().getSelectedItem();

        if (selectedStudent == null) {
            new Alert(Alert.AlertType.WARNING, "Please select a student to delete.").showAndWait();
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Deletion");
        alert.setHeaderText("Are you sure you want to delete " + selectedStudent.getName() + "?");
        alert.setContentText("This action cannot be undone.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (rosterService.removeStudent(selectedStudent)) {
                rosterTable.getItems().setAll(rosterService.getRoster());
                rosterService.saveData();
                System.out.println("LOG: Deleted student: " + selectedStudent.getName());
            } else {
                new Alert(Alert.AlertType.ERROR, "Failed to delete student.").show();
            }
        }
    }

    private void handleEditStudent() {
        Student selectedStudent = rosterTable.getSelectionModel().getSelectedItem();
        if (selectedStudent == null) {
            new Alert(Alert.AlertType.WARNING, "Please select a student to edit.").showAndWait();
            return;
        }

        TextInputDialog dialog = new TextInputDialog(selectedStudent.getName());
        dialog.setTitle("Edit Student Name");
        dialog.setHeaderText("Editing student: " + selectedStudent.getName());
        dialog.setContentText("New Name:");

        dialog.showAndWait().ifPresent(name -> {
            String newName = name.trim();
            if (!newName.isEmpty() && !newName.equalsIgnoreCase(selectedStudent.getName())) {

                List<Integer> scores = selectedStudent.getScores();
                rosterService.removeStudent(selectedStudent);

                if (rosterService.addStudent(newName)) {

                    Student newStudent = rosterService.findStudent(newName);

                    if (newStudent != null) {
                        newStudent.setScores(scores);
                    }
                    rosterTable.getItems().setAll(rosterService.getRoster());
                    rosterService.saveData();
                    System.out.println("LOG: Renamed student to: " + newName);
                } else {
                    new Alert(Alert.AlertType.ERROR, "Cannot rename. Name may already exist or roster is full.").show();
                    // Restore original student if rename failed
                    if (rosterService.addStudent(selectedStudent.getName())) {
                        Student originalStudent = rosterService.findStudent(selectedStudent.getName());
                        if (originalStudent != null) {
                            originalStudent.setScores(scores);
                        }
                    }
                    rosterTable.getItems().setAll(rosterService.getRoster());
                }
            }
        });
    }

    private void handleAddScore() {
        Student selectedStudent = rosterTable.getSelectionModel().getSelectedItem();

        if (selectedStudent == null) {
            new Alert(Alert.AlertType.WARNING, "Please select a student from the table first.").showAndWait();
            return;
        }

        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Add Score for " + selectedStudent.getName());
        dialog.setHeaderText("Current Scores: " + selectedStudent.getScoresString());
        dialog.setContentText("Enter New Score (0-100):");

        dialog.showAndWait().ifPresent(scoreText -> {
            try {
                int score = Integer.parseInt(scoreText.trim());

                if (score >= 0 && score <= 100) {
                    selectedStudent.addScore(score);
                    rosterTable.refresh();
                    rosterService.saveData();
                    System.out.println("LOG: Added score " + score + " to " + selectedStudent.getName());
                } else {
                    new Alert(Alert.AlertType.ERROR, "Score must be between 0 and 100.").showAndWait();
                }
            } catch (NumberFormatException ex) {
                new Alert(Alert.AlertType.ERROR, "Invalid input. Please enter a whole number.").showAndWait();
            }
        });
    }

    private void handleEditScores() {
        Student selectedStudent = rosterTable.getSelectionModel().getSelectedItem();

        if (selectedStudent == null) {
            new Alert(Alert.AlertType.WARNING, "Please select a student to edit scores for.").showAndWait();
            return;
        }

        List<Integer> currentScores = selectedStudent.getScores();
        if (currentScores.isEmpty()) {
            new Alert(Alert.AlertType.INFORMATION, selectedStudent.getName() + " has no scores to edit. Use 'Enter Scores' to add one.").showAndWait();
            return;
        }

        String scoreListHeader = "Enter scores separated by commas (e.g., 90, 85, 77). Delete a score by removing it from the list.\n\n" +
                "Current Scores: " + selectedStudent.getScoresString();

        TextInputDialog dialog = new TextInputDialog(selectedStudent.getScoresString().replace(" ", ""));
        dialog.setTitle("Edit Scores for " + selectedStudent.getName());
        dialog.setHeaderText(scoreListHeader);
        dialog.setContentText("All Scores:");

        dialog.showAndWait().ifPresent(newScoresText -> {
            try {
                List<Integer> updatedScores = parseScoresFromText(newScoresText);

                rosterService.updateStudentScores(selectedStudent, updatedScores);
                rosterTable.refresh();
                rosterService.saveData();
                System.out.println("LOG: Scores updated for " + selectedStudent.getName());
            } catch (NumberFormatException e) {
                new Alert(Alert.AlertType.ERROR, "Invalid score format. Please ensure all entries are numbers.").showAndWait();
            } catch (IllegalArgumentException e) {
                new Alert(Alert.AlertType.ERROR, e.getMessage()).showAndWait();
            }
        });
    }

    // Ensures that only IllegalArgumentException is declared, addressing the "more general exception" warning.
    private List<Integer> parseScoresFromText(String newScoresText) throws IllegalArgumentException {
        List<Integer> updatedScores = new ArrayList<>();
        if (!newScoresText.trim().isEmpty()) {
            String[] scoresArray = newScoresText.split(",");
            for (String scoreStr : scoresArray) {
                int score = Integer.parseInt(scoreStr.trim());
                if (score < 0 || score > 100) {
                    throw new IllegalArgumentException("Score out of range (0-100): " + score);
                }
                updatedScores.add(score);
            }
        }
        return updatedScores;
    }

    private void handleAddStudent() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Add New Student");
        dialog.setHeaderText("Enter the full name of the new student.");
        dialog.setContentText("Name:");

        dialog.showAndWait().ifPresent(name -> {
            String newName = name.trim();
            if (!newName.isEmpty()) {
                boolean success = rosterService.addStudent(newName);

                if (success) {
                    rosterTable.getItems().setAll(rosterService.getRoster());
                    rosterService.saveData();
                    System.out.println("LOG: Added student: " + name);
                } else {
                    new Alert(Alert.AlertType.ERROR, "Could not add student. Name may exist or roster is full.").show();
                }
            }
        });
    }

    private TableView<Student> createRosterTable() {
        TableView<Student> table = new TableView<>();
        ObservableList<Student> data = FXCollections.observableArrayList(rosterService.getRoster());
        table.setItems(data);

        TableColumn<Student, String> nameCol = new TableColumn<>("Student Name");
        nameCol.setMinWidth(150);
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn<Student, Double> avgCol = new TableColumn<>("Average");
        avgCol.setMinWidth(100);
        avgCol.setCellValueFactory(cellData -> {
            double avg = cellData.getValue().calculateAverage();
            return new javafx.beans.property.SimpleDoubleProperty(avg).asObject();
        });

        TableColumn<Student, String> letterCol = new TableColumn<>("Grade");
        letterCol.setMinWidth(80);
        letterCol.setCellValueFactory(cellData -> {
            String letter = cellData.getValue().getLetterScore();
            return new javafx.beans.property.SimpleStringProperty(letter);
        });

        TableColumn<Student, String> scoresCol = new TableColumn<>("Scores");
        scoresCol.setMinWidth(300);
        scoresCol.setCellValueFactory(new PropertyValueFactory<>("scoresString"));

        // Uses Arrays.asList() to pass columns as a Collection, avoiding varargs generics warning.
        table.getColumns().addAll(Arrays.asList(nameCol, avgCol, letterCol, scoresCol));

        return table;
    }

    // Standard main method, using String[] args to prevent varargs warnings.
    static void main(String[] args) {
        launch(args);
    }
}