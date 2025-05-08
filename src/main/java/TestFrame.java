import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class TestFrame extends JFrame {
    private JPanel questionPanel;
    private JButton submitButton;
    private Map<Integer, ButtonGroup> answerGroups = new HashMap<>();

    public TestFrame() {
        setupUI();
        loadQuestions();
    }

    private void setupUI() {
        setTitle("Take the test");
        setSize(700, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        questionPanel = new JPanel();
        questionPanel.setLayout(new BoxLayout(questionPanel, BoxLayout.Y_AXIS));
        JScrollPane scrollPane = new JScrollPane(questionPanel);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        submitButton = new JButton("Submit the test");
        submitButton.setFont(new Font("Arial", Font.BOLD, 14));
        submitButton.addActionListener(this::submitTest);

        add(scrollPane, BorderLayout.CENTER);
        add(submitButton, BorderLayout.SOUTH);
    }

    private void loadQuestions() {
        try {
            Connection conn = Main.getDatabase().getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rsQuestions = stmt.executeQuery("SELECT * FROM questions");

            while (rsQuestions.next()) {
                int questionId = rsQuestions.getInt("id");
                String questionText = rsQuestions.getString("text");

                JLabel questionLabel = new JLabel("Question: " + questionText);
                questionLabel.setFont(new Font("Arial", Font.BOLD, 14));
                questionPanel.add(questionLabel);

                ButtonGroup group = new ButtonGroup();
                answerGroups.put(questionId, group);

                PreparedStatement stmtAnswers = conn.prepareStatement(
                        "SELECT * FROM answers WHERE question_id = ?");
                stmtAnswers.setInt(1, questionId);
                ResultSet rsAnswers = stmtAnswers.executeQuery();

                while (rsAnswers.next()) {
                    int answerId = rsAnswers.getInt("id");
                    String answerText = rsAnswers.getString("text");

                    JRadioButton answerButton = new JRadioButton(answerText);
                    answerButton.setActionCommand(String.valueOf(answerId));
                    group.add(answerButton);
                    questionPanel.add(answerButton);
                }

                rsAnswers.close();
                stmtAnswers.close();
                questionPanel.add(Box.createRigidArea(new Dimension(0, 10)));
            }

            rsQuestions.close();
            stmt.close();
            questionPanel.revalidate();
            questionPanel.repaint();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading questions!");
        }
    }

    private void submitTest(ActionEvent e) {
        try {
            Connection conn = Main.getDatabase().getConnection();
            int total = answerGroups.size();
            int correct = 0;

            for (Map.Entry<Integer, ButtonGroup> entry : answerGroups.entrySet()) {
                ButtonModel selected = entry.getValue().getSelection();
                if (selected != null) {
                    int selectedAnswerId = Integer.parseInt(selected.getActionCommand());

                    PreparedStatement stmt = conn.prepareStatement(
                            "SELECT is_correct FROM answers WHERE id = ?");
                    stmt.setInt(1, selectedAnswerId);
                    ResultSet rs = stmt.executeQuery();

                    if (rs.next() && rs.getInt("is_correct") == 1) {
                        correct++;
                    }
                    rs.close();
                    stmt.close();
                }
            }

            int score = (int) (((double) correct / total) * 100);

            JOptionPane.showMessageDialog(
                    this, "Test completed!\nYou answered correctly to " + correct +
                            " out of " + total + " questions.\nScore: " + score + "%");

            int userId = Main.getLoggedInUserId();
            PreparedStatement saveScore = conn.prepareStatement(
                    "INSERT INTO results(user_id,score) VALUES (?, ?)");
            saveScore.setInt(1, userId);
            saveScore.setInt(2, score);
            saveScore.executeUpdate();
            saveScore.close();

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Test submission error!");
        }
    }
}
