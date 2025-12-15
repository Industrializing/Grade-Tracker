// RosterService.java
import java.util.List;
import java.util.Collections;

public class RosterService {

    private final List<Student> roster;
    private final FileRosterStorage storage = new FileRosterStorage();
    private static final int MAX_STUDENTS = 50;

    public RosterService() {
        // Load the roster from storage upon initialization.
        this.roster = storage.loadRoster();

        if (roster.isEmpty()) {
            addStudent("Alice Smith");
            addStudent("Bob Johnson");
            addStudent("Charlie Brown");

            Student alice = findStudent("Alice Smith");
            if (alice != null) {
                alice.addScore(95);
                alice.addScore(88);
            }
            Student bob = findStudent("Bob Johnson");
            if (bob != null) {
                bob.addScore(72);
            }
            System.out.println("LOG: Loaded fresh demo data.");
        } else {
            System.out.println("LOG: Loaded " + roster.size() + " students from file.");
        }
    }

    public boolean addStudent(String name) {
        if (roster.size() >= MAX_STUDENTS) {
            System.err.println("Roster is full.");
            return false;
        }
        if (findStudent(name) == null && !name.trim().isEmpty()) {
            roster.add(new Student(name.trim()));
            return true;
        }
        return false;
    }

    // Method to remove a student object from the roster.
    public boolean removeStudent(Student student) {
        return roster.remove(student);
    }

    // Method to update a student's scores after editing.
    public void updateStudentScores(Student student, List<Integer> newScores) {
        student.setScores(newScores);
    }

    // Returns an unmodifiable view of the roster list.
    public List<Student> getRoster() {
        return Collections.unmodifiableList(roster);
    }

    public Student findStudent(String name) {
        for (Student s : roster) {
            if (s.getName().equalsIgnoreCase(name.trim())) {
                return s;
            }
        }
        return null;
    }

    public void saveData() {
        storage.saveRoster(this.roster);
    }
}