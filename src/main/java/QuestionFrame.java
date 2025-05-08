import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class QuestionFrame extends JFrame {
    private JTextField questionField;
    private JTextField[] answerFields;
    private JCheckBox[] correctCheckboxes;
    private JButton saveButton;

    public QuestionFrame(){
        setupUI();
    }

    private void setupUI() {
        setTitle("Add Question:");
        setSize(600,400);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel mainPanel=new JPanel();
        mainPanel.setLayout(new GridLayout(7,2,10,10));

        mainPanel.add(new JLabel("Question:"));
        questionField=new JTextField();
        mainPanel.add(questionField);

        answerFields=new JTextField[4];
        correctCheckboxes=new JCheckBox[4];

        for(int i=0;i<4;i++)
        {
            answerFields[i]=new JTextField();
            correctCheckboxes[i]=new JCheckBox("Correct:");

            mainPanel.add(answerFields[i]);
            mainPanel.add(correctCheckboxes[i]);
        }

        saveButton=new JButton("Save Question");
        mainPanel.add(saveButton);

        add(mainPanel);

        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveQustion();
            }
        });
    }

    private void saveQustion() {
        String questionText=questionField.getText();

        if(questionText.isEmpty()){
            JOptionPane.showMessageDialog(this,"Please enter a question!");
            return;
        }
        try{
            Connection conn=Main.getDatabase().getConnection();

            PreparedStatement questionStmt=conn.prepareStatement(
                    "INSERT INTO questions (text) VALUES (?)",new String[]{"id"});
            questionStmt.setString(1,questionText);
            questionStmt.executeUpdate();

            ResultSet generatedKeys=questionStmt.getGeneratedKeys();
            int questionId=-1;
            if(generatedKeys.next()){
                questionId=generatedKeys.getInt(1);
            }
            generatedKeys.close();
            questionStmt.close();

            for(int i=0;i<4;i++){
                String answerText=answerFields[i].getText();
                boolean isCorrect=correctCheckboxes[i].isSelected();

                if(!answerText.isEmpty()){
                    PreparedStatement answerStmt=conn.prepareStatement(
                            "INSERT INTO answers(question_id,text,is_correct) VALUES (?,?,?)");
                    answerStmt.setInt(1,questionId);
                    answerStmt.setString(2,answerText);
                    answerStmt.setBoolean(3,isCorrect);

                    answerStmt.executeUpdate();
                    answerStmt.close();
                }
            }

            JOptionPane.showMessageDialog(this,"Question saved successfully!");
            questionField.setText("");
            for(int i=0;i<4;i++) {
                answerFields[i].setText("");
                correctCheckboxes[i].setSelected(false);
            }
        }catch (SQLException ex){
                JOptionPane.showMessageDialog(this,"Error saving question "+ex.getMessage());

        }
    }

}
