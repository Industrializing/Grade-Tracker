// AutosaveTask.java
public class AutosaveTask implements Runnable {

    private final RosterService rosterService;
    // Flag to signal the thread to stop processing
    private volatile boolean isStopped = false;
    private static final int AUTOSAVE_INTERVAL_MS = 60000;

    public AutosaveTask(RosterService rosterService) {
        this.rosterService = rosterService;
    }

    // Suppresses the IDE warning regarding the use of Thread.sleep() inside a loop.
    @SuppressWarnings("BusyWait")
    @Override
    public void run() {
        // The loop continues until the stop flag is set.
        while (!isStopped) {
            try {
                // Pause the thread for the defined interval
                Thread.sleep(AUTOSAVE_INTERVAL_MS);

                // Save data if the stop flag was not set during the sleep period
                if (!isStopped) {
                    rosterService.saveData();
                    System.out.println("LOG: Autosave complete.");
                }

            } catch (InterruptedException e) {
                // If interrupted, gracefully set the stop flag and exit the loop.
                isStopped = true;
                System.out.println("LOG: Autosave thread interrupted and stopping.");
                break; // Exit the while loop immediately upon interruption
            }
        }
        System.out.println("LOG: Autosave thread gracefully shut down.");
    }

    // Signals the thread to stop running.
    public void stop() {
        isStopped = true;
        // The thread running this task must be interrupted externally (in GradeTrackerApp.stop()).
    }
}