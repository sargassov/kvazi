package server;

import java.sql.*;
import java.util.List;

public class SQLAuthService implements AuthService {

    public static Connection connection;
    public static Statement statement;
    public static ResultSet resultSet;
    private List<UserData> users;

    private class UserData {
        String login;
        String password;
        String nickname;

        public UserData(String login, String password, String nickname) {
            this.login = login;
            this.password = password;
            this.nickname = nickname;
        }
    }

    public SQLAuthService() throws SQLException, ClassNotFoundException {
        setConnection();
        createdb();
        writedb();
    }

    @Override
    public String getNicknameByLoginAndPassword(String login, String password) throws SQLException {
        resultSet = statement.executeQuery("SELECT * FROM users");
        String dbNickname;
        while(resultSet.next()){
            if (resultSet.getString("login").equals(login) &&
                    resultSet.getString("password").equals(password)) {
                return dbNickname = resultSet.getString("nickname");
            }
        }

        return null;
    }

    @Override
    public boolean registration(String login, String password, String nickname) throws SQLException {
        resultSet = statement.executeQuery("SELECT * FROM users");
        while(resultSet.next()){
            if (resultSet.getString("login").equals(login) ||
                    resultSet.getString("nickname").equals(nickname)) { return false;
            }
            statement.execute("INSERT INTO 'users' ('nickname', 'login', 'password') VALUES ('" + nickname + "', '"
                    + login + "', '" + password + "')");
        }

        return true;
    }

    public static void setConnection() throws ClassNotFoundException, SQLException {
        connection = null;
        Class.forName("org.sqlite.JDBC");
        connection = DriverManager.getConnection("jdbc:sqlite:authService:authdb");
    }

    public static void createdb() throws SQLException {
        statement = connection.createStatement();
        statement.execute("CREATE TABLE if not exists 'users'" +
                "('id' INTEGER PRIMARY KEY AUTOINCREMENT, 'nickname' text, 'login' text, 'password' text);");
    }

    public static void writedb() throws SQLException {
        statement.execute("INSERT INTO 'users' ('nickname', 'login', 'password') VALUES('qwe', 'qwe', 'qwe')");
        statement.execute("INSERT INTO 'users' ('nickname', 'login', 'password') VALUES('asd', 'asd', 'asd')");
        statement.execute("INSERT INTO 'users' ('nickname', 'login', 'password') VALUES('zxc', 'zxc', 'zxc')");
    }

    public static void closedb() throws SQLException {
        resultSet.close();
        statement.close();
        connection.close();
    }

}
