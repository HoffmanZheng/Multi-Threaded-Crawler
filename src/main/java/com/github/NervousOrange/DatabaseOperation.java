package com.github.NervousOrange;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.sql.*;
import java.util.ArrayList;

public class DatabaseOperation {
    Connection connection;
    String databaseURL;

    public DatabaseOperation(String databaseURL) {
        this.databaseURL = databaseURL;
    }

    public ArrayList<String> loadLinkFromDatabase(String sqlStatement) {
        ResultSet resultSet = null;
        ArrayList<String> linksFromDatabase = new ArrayList<>();
        try (PreparedStatement preparedStatement = connection.prepareStatement(sqlStatement)) {
            resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                linksFromDatabase.add(resultSet.getString(1));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (resultSet != null) {
                try {
                    resultSet.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        return linksFromDatabase;
    }

    public void updateLinkInDatabase(String sqlStatement, String link) {
        try (PreparedStatement preparedStatement = connection.prepareStatement(sqlStatement)) {
            preparedStatement.setString(1, link);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateLinkInDatabase(String sqlStatement) {
        try (PreparedStatement preparedStatement = connection.prepareStatement(sqlStatement)) {
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void writeNewsPagesIntoDatabase(String sqlStatement, String title, String content, String URL) {
        try (PreparedStatement preparedStatement = connection.prepareStatement(sqlStatement)) {
            preparedStatement.setString(1, title);
            preparedStatement.setString(2, content);
            preparedStatement.setString(3, URL);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean isLinkInDatabase(String sqlStatement, String link) {
        ResultSet resultSet = null;
        try (PreparedStatement preparedStatement = connection.prepareStatement(sqlStatement)) {
            preparedStatement.setString(1, link);
            resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (resultSet != null) {
                try {
                    resultSet.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        return false;
    }

    @SuppressFBWarnings("DMI_CONSTANT_DB_PASSWORD")
    public void connectDatabase() {
        try {
            connection = DriverManager.getConnection(this.databaseURL, "root", "root");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void initializeDatabase() throws SQLException {
        try (PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO LINKS_TO_BE_PROCESSED (link) values (?)")) {
            preparedStatement.setString(1, "http://sina.cn");
            preparedStatement.executeUpdate();
            updateLinkInDatabase("DELETE FROM LINKS_ALREADY_PROCESSED");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
