// FileRosterStorage.java
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class FileRosterStorage {

    private static final String FILE_NAME = "roster.csv";
    // Regex to split CSV entries, allowing for zero or more spaces after the comma.
    private static final String CSV_DELIMITER_REGEX = ",\\s*";

    public List<Student> loadRoster() {
        List<Student> roster = new ArrayList<>();
        File file = new File(FILE_NAME);

        if (file.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.trim().isEmpty()) continue;

                    // Split the line into a maximum of 3 parts (Name, Avg/Grade, Scores)
                    String[] parts = line.split(CSV_DELIMITER_REGEX, 3);
                    if (parts.length < 1) continue;

                    String name = parts[0].trim();
                    Student student = new Student(name);

                    if (parts.length >= 2) {
                        // The last part contains the score data.
                        String scoresText = parts[parts.length - 1].trim();

                        if (!scoresText.isEmpty() && !scoresText.equalsIgnoreCase("N/A")) {
                            List<Integer> scores = Arrays.stream(scoresText.split(CSV_DELIMITER_REGEX))
                                    .map(String::trim)
                                    .filter(s -> !s.isEmpty())
                                    .map(Integer::parseInt)
                                    .collect(Collectors.toList());
                            student.setScores(scores);
                        }
                    }
                    roster.add(student);
                }
            } catch (IOException | NumberFormatException e) {
                System.err.println("Error loading roster data: " + e.getMessage());
                // Fallback to empty list upon file error
                return new ArrayList<>();
            }
        }
        return roster;
    }

    public void saveRoster(List<Student> roster) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_NAME))) {
            for (Student student : roster) {
                // Format: Name, Score1, Score2, ...
                String line = student.getName() + ", " + student.getScoresString().replace("N/A", "");
                writer.write(line);
                writer.newLine();
            }
            System.out.println("LOG: Data saved to " + FILE_NAME);
        } catch (IOException e) {
            System.err.println("Error saving roster data: " + e.getMessage());
        }
    }
}