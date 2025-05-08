import java.io.InputStream;
import java.sql.*;
import java.util.Properties;

public class Database {
    private static final String CONFIG_FILE = "config.properties";
    private Connection connection;

    public Database() {
        try {
            Properties props = new Properties();
            InputStream input = getClass().getClassLoader().getResourceAsStream(CONFIG_FILE);
            if (input == null) {
                throw new RuntimeException("Config file not found: " + CONFIG_FILE);
            }
            props.load(input);

            String dbUrl = props.getProperty("db.url");
            String user = props.getProperty("db.user");
            String password = props.getProperty("db.password");

            connection = DriverManager.getConnection(dbUrl, user, password);
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    private void initDatabase() {
        try {
            DatabaseMetaData meta = connection.getMetaData();

            ResultSet tables = meta.getTables(null, connection.getMetaData().getUserName().toUpperCase(), "USERS", null);
            if (!tables.next()) {
                Statement statement = connection.createStatement();
                statement.execute(
                        "CREATE TABLE users (" +
                                "id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY," +
                                "first_name VARCHAR2(60) NOT NULL," +
                                "last_name VARCHAR2(30) NOT NULL," +
                                "email VARCHAR2(100) UNIQUE NOT NULL," +
                                "password VARCHAR2(100) NOT NULL" +
                                ")"
                );
                statement.close();
            }
            tables.close();

            tables = meta.getTables(null, connection.getMetaData().getUserName().toUpperCase(), "PROFESSORS", null);
            if (!tables.next()) {
                Statement statement = connection.createStatement();
                statement.execute(
                        "CREATE TABLE professors (" +
                                "user_id NUMBER PRIMARY KEY," +
                                "CONSTRAINT fk_professor_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE" +
                            ")"
                );
                statement.close();
            }
            tables.close();

            tables = meta.getTables(null, connection.getMetaData().getUserName().toUpperCase(), "STUDENTS", null);
            if (!tables.next()) {
                Statement statement = connection.createStatement();
                statement.execute(
                        "CREATE TABLE students (" +
                                "user_id NUMBER PRIMARY KEY," +
                                "CONSTRAINT fk_student_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE" +
                            ")"
                );
                statement.close();
            }
            tables.close();

            tables=meta.getTables(null,connection.getMetaData().getUserName().toUpperCase(),"QUESTIONS",null);
            if(!tables.next()){
                Statement statement1=connection.createStatement();
                statement1.execute(
                        "CREATE TABLE questions("+
                        "id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,"+
                                " text VARCHAR2(500) NOT NULL"+
                                ")"
                );
                statement1.close();
            }
            tables.close();

            tables=meta.getTables(null,connection.getMetaData().getUserName().toUpperCase(),"ANSWERS",null);
            if(!tables.next()){
                Statement statement1=connection.createStatement();
                statement1.execute(
                        "CREATE TABLE answers ("+
                                "id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,"+
                                "question_id NUMBER,"+
                                "text VARCHAR2(500) NOT NULL,"+
                                "is_correct NUMBER(1) CHECK(is_correct IN(0,1)),"+
                                "FOREIGN KEY(question_id) REFERENCES questions(id) ON DELETE CASCADE" + ")"
                );
                statement1.close();
            }
            tables.close();

            tables = meta.getTables(null, connection.getMetaData().getUserName().toUpperCase(), "RESULTS", null);
            if (!tables.next()) {
                Statement statement = connection.createStatement();
                statement.execute(
                        "CREATE TABLE results (" +
                                "id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY, " +
                                "user_id NUMBER NOT NULL, " +
                                "score NUMBER NOT NULL, " +
                                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                                "CONSTRAINT fk_result_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE" +
                                ")"
                );
                statement.close();
            }
            tables.close();




            Statement statement = connection.createStatement();
            ResultSet professorCount = statement.executeQuery("SELECT COUNT(*) FROM professors");
            professorCount.next();

            int count = professorCount.getInt(1);

            professorCount.close();

            if (count == 0) {
                PreparedStatement userStmt = connection.prepareStatement(
                        "INSERT INTO users (first_name, last_name, email, password) VALUES (?, ?, ?, ?)",
                        new String[]{"id"}
                );

                userStmt.setString(1, "Ocneanu");
                userStmt.setString(2, "George");
                userStmt.setString(3, "professor@gmail.com");
                userStmt.setString(4, "123123Aa");

                userStmt.executeUpdate();

                ResultSet generatedKeys = userStmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    int userId = generatedKeys.getInt(1);

                    PreparedStatement profStmt = connection.prepareStatement(
                            "INSERT INTO professors (user_id) VALUES (?)"
                    );

                    profStmt.setInt(1, userId);
                    profStmt.executeUpdate();
                    profStmt.close();

                    System.out.println("Default professor created with ID: " + userId);
                }
                generatedKeys.close();
                userStmt.close();

            }
        } catch (SQLException e) {
            System.out.println("Database initialization error: " + e.getMessage());
        }
    }

    public void close() {
        if (connection != null) {
            try {
                connection.close();
                System.out.println("Database connection closed successfully.");
            } catch (SQLException e) {
                System.out.println("Error closing database connection: " + e.getMessage());
            }
        }
    }

    public Connection getConnection() {
        return connection;
    }
}