import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LoginFrame extends JFrame {
    private JTextField emailField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JButton registerButton;

    public LoginFrame() {
        setupUI();
    }

    private void setupUI() {
        setTitle("Login");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(null);

        JLabel titleLabel = new JLabel("Login to e-learning app", JLabel.CENTER);
        titleLabel.setBounds(70, 20, 250, 30);

        Font titleFont = titleLabel.getFont();
        Font largerFont = titleFont.deriveFont(24f);
        titleLabel.setFont(largerFont);

        JLabel emailLabel = new JLabel("Email:");
        emailLabel.setBounds(50, 90, 80, 25);

        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setBounds(50, 130, 80, 25);

        emailField = new JTextField();
        emailField.setBounds(140, 90, 200, 25);

        passwordField = new JPasswordField();
        passwordField.setBounds(140, 130, 200, 25);

        loginButton = new JButton("Login");
        loginButton.setBounds(110, 180, 80, 30);

        registerButton = new JButton("Register");
        registerButton.setBounds(210, 180, 80, 30);

        mainPanel.add(titleLabel);
        mainPanel.add(emailLabel);
        mainPanel.add(emailField);
        mainPanel.add(passwordLabel);
        mainPanel.add(passwordField);
        mainPanel.add(loginButton);
        mainPanel.add(registerButton);

        add(mainPanel);

        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    Connection conn = Main.getDatabase().getConnection();

                    String email = emailField.getText();
                    String password = new String(passwordField.getPassword());

                    PreparedStatement stmt = conn.prepareStatement(
                            "SELECT id FROM users WHERE email = ? AND password = ?"
                    );
                    stmt.setString(1, email);
                    stmt.setString(2, password);

                    ResultSet rs = stmt.executeQuery();

                    if (rs.next()) {
                        int userId = rs.getInt("id");

                        Main.setLoggedInUserId(userId);

                        if (isProfessor(userId)) {
                            JOptionPane.showMessageDialog(LoginFrame.this, "Login as professor!");
                            QuestionFrame questionFrame = new QuestionFrame();
                            questionFrame.setVisible(true);
                        } else {
                            JOptionPane.showMessageDialog(LoginFrame.this, "Login as student!");
                            TestFrame testFrame = new TestFrame();
                            testFrame.setVisible(true);
                        }

                        dispose();
                    } else {
                        JOptionPane.showMessageDialog(LoginFrame.this, "Incorrect email or password!");
                    }

                    rs.close();
                    stmt.close();
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(LoginFrame.this, "Login error: " + ex.getMessage());
                }
            }
        });

        registerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                RegisterFrame registerFrame = new RegisterFrame();
                registerFrame.setVisible(true);
                dispose();
            }
        });
    }

    private boolean isProfessor(int userId) {
        try {
            Connection conn = Main.getDatabase().getConnection();
            PreparedStatement stmt = conn.prepareStatement(
                    "SELECT * FROM professors WHERE user_id = ?"
            );
            stmt.setInt(1, userId);

            ResultSet rs = stmt.executeQuery();
            boolean isProf = rs.next();

            rs.close();
            stmt.close();

            return isProf;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
