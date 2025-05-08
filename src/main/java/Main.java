
import javax.swing.*;

public class Main {
    private static Database database;
    private static int loggedInUserId;

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        database = new Database();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (database != null) {
                database.close();
            }
            System.out.println("Application shutting down, database connection closed.");
        }));

        SwingUtilities.invokeLater(() -> {
            LoginFrame loginFrame = new LoginFrame();
            loginFrame.setVisible(true);
        });
    }

    public static Database getDatabase() {
        return database;
    }

    public static void setLoggedInUserId(int id) {
        loggedInUserId = id;
    }

    public static int getLoggedInUserId() {
        return loggedInUserId;
    }
}
