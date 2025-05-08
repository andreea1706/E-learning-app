import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class RegisterFrame extends JFrame {
    private JTextField firstNameField;
    private JTextField lastNameField;
    private JTextField emailField;
    private JPasswordField passwordField;
    private JPasswordField confirmPasswordField;
    private JComboBox<String> roleComboBox;

    private JButton registerButton;
    private JButton loginButton;

    public RegisterFrame() {
        setupUI();
    }

    private void setupUI() {
        setTitle("Register");
        setSize(500, 450);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(null);

        JLabel titleLabel = new JLabel("Register new account", JLabel.CENTER);
        titleLabel.setBounds(100, 20, 300, 30);
        Font titleFont = titleLabel.getFont();
        Font largerFont = titleFont.deriveFont(24f);
        titleLabel.setFont(largerFont);

        JLabel firstNameLabel = new JLabel("First name:");
        firstNameLabel.setBounds(50, 80, 100, 25);

        JLabel lastNameLabel = new JLabel("Last name:");
        lastNameLabel.setBounds(50, 120, 100, 25);

        JLabel emailLabel = new JLabel("Email:");
        emailLabel.setBounds(50, 160, 100, 25);

        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setBounds(50, 200, 120, 25);

        JLabel confirmPasswordLabel = new JLabel("Confirm password:");
        confirmPasswordLabel.setBounds(50, 240, 120, 25);

        JLabel roleLabel = new JLabel("Role:");
        roleLabel.setBounds(50, 280, 100, 25);

        firstNameField = new JTextField();
        firstNameField.setBounds(180, 80, 250, 25);

        lastNameField = new JTextField();
        lastNameField.setBounds(180, 120, 250, 25);

        emailField = new JTextField();
        emailField.setBounds(180, 160, 250, 25);

        passwordField = new JPasswordField();
        passwordField.setBounds(180, 200, 250, 25);

        confirmPasswordField = new JPasswordField();
        confirmPasswordField.setBounds(180, 240, 250, 25);

        roleComboBox = new JComboBox<>(new String[]{"Student", "Professor"});
        roleComboBox.setBounds(180, 280, 250, 25);

        registerButton = new JButton("Register");
        registerButton.setBounds(140, 340, 100, 30);

        loginButton = new JButton("Login");
        loginButton.setBounds(260, 340, 100, 30);

        mainPanel.add(titleLabel);
        mainPanel.add(firstNameLabel);
        mainPanel.add(lastNameLabel);
        mainPanel.add(emailLabel);
        mainPanel.add(passwordLabel);
        mainPanel.add(confirmPasswordLabel);
        mainPanel.add(roleLabel);

        mainPanel.add(firstNameField);
        mainPanel.add(lastNameField);
        mainPanel.add(emailField);
        mainPanel.add(passwordField);
        mainPanel.add(confirmPasswordField);
        mainPanel.add(roleComboBox);

        mainPanel.add(registerButton);
        mainPanel.add(loginButton);

        add(mainPanel);

        // buton register
        registerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                registerUser();
            }
        });

        // buton login
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                LoginFrame loginFrame = new LoginFrame();
                loginFrame.setVisible(true);
                dispose();
            }
        });
    }

    private void registerUser() {
        try {
            Connection conn = Main.getDatabase().getConnection();

            String firstName = firstNameField.getText().trim();
            String lastName = lastNameField.getText().trim();
            String email = emailField.getText().trim();
            String password = new String(passwordField.getPassword()).trim();
            String confirmPassword = new String(confirmPasswordField.getPassword()).trim();
            String role = (String) roleComboBox.getSelectedItem();

            if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please fill in all fields!");
                return;
            }

            if (!password.equals(confirmPassword)) {
                JOptionPane.showMessageDialog(this, "Passwords do not match!");
                return;
            }

            PreparedStatement userStmt = conn.prepareStatement(
                    "INSERT INTO users (first_name, last_name, email, password) VALUES (?, ?, ?, ?)",
                    new String[]{"id"}
            );
            userStmt.setString(1, firstName);
            userStmt.setString(2, lastName);
            userStmt.setString(3, email);
            userStmt.setString(4, password);

            userStmt.executeUpdate();

            ResultSet generatedKeys = userStmt.getGeneratedKeys();
            if (generatedKeys.next()) {
                int userId = generatedKeys.getInt(1);

                if ("Professor".equals(role)) {
                    PreparedStatement profStmt = conn.prepareStatement(
                            "INSERT INTO professors (user_id) VALUES (?)"
                    );
                    profStmt.setInt(1, userId);
                    profStmt.executeUpdate();
                    profStmt.close();
                } else {
                    PreparedStatement studentStmt = conn.prepareStatement(
                            "INSERT INTO students (user_id) VALUES (?)"
                    );
                    studentStmt.setInt(1, userId);
                    studentStmt.executeUpdate();
                    studentStmt.close();
                }

                JOptionPane.showMessageDialog(this, "Registration successful!");
                dispose();
                LoginFrame loginFrame = new LoginFrame();
                loginFrame.setVisible(true);
            }

            generatedKeys.close();
            userStmt.close();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Registration error: " + ex.getMessage());
        }
    }
}
