// Student.java
import java.util.ArrayList;
import java.util.List;

public class Student implements Gradable {

    private final String name;
    private final List<Integer> scores = new ArrayList<>();

    public Student(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    // Adds a score, ensuring it is within the valid 0-100 range.
    public void addScore(int s) {
        if (s >= 0 && s <= 100) {
            scores.add(s);
        }
    }

    // Sets the student's scores by clearing the current list and adding new valid scores.
    public void setScores(List<Integer> newScores) {
        scores.clear();
        for (int score : newScores) {
            if (score >= 0 && score <= 100) {
                scores.add(score);
            }
        }
    }

    // Returns a defensive copy of the scores list.
    public List<Integer> getScores() {
        return new ArrayList<>(scores);
    }

    @Override
    public double calculateAverage() {
        if (scores.isEmpty()) return 0.0;
        int sum = 0;
        for (int s : scores) {
            sum += s;
        }
        return sum / (double) scores.size();
    }

    @Override
    public String getLetterScore() {
        double avg = calculateAverage();
        if (avg >= 90) {
            return "A";
        } else if (avg >= 80) {
            return "B";
        } else if (avg >= 70) {
            return "C";
        } else if (avg >= 60) {
            return "D";
        } else {
            return "F";
        }
    }

    // Returns the list of scores formatted as a comma-separated string.
    public String getScoresString() {
        return scores.isEmpty() ? "N/A" : scores.toString().replace("[", "").replace("]", "");
    }
}