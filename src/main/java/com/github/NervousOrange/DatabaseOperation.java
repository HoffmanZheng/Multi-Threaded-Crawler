package com.github.NervousOrange;

import java.sql.*;
import java.util.ArrayList;

public class DatabaseOperation {
    Connection connection;
    String databaseURL;

    public DatabaseOperation(String databaseURL) {
        this.databaseURL = databaseURL;
    }

    public ArrayList<String> loadLinkFromDatabase(String sqlStatement) {
        ArrayList<String> linksFromDatabase = new ArrayList<>();
        try (PreparedStatement preparedStatement = connection.prepareStatement(sqlStatement)) {
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                linksFromDatabase.add(resultSet.getString(1));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return linksFromDatabase;
    }

    public String updateDatabase(String sqlStatement, String link) {
        try (PreparedStatement preparedStatement = connection.prepareStatement(sqlStatement)) {
            preparedStatement.setString(1, link);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean isLinkInDatabase(String sqlStatement, String link) {
        try (PreparedStatement preparedStatement = connection.prepareStatement(sqlStatement)) {
            preparedStatement.setString(1, link);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void connectDatabase() {
        try {
            connection = DriverManager.getConnection(this.databaseURL, "root", "root");
            PreparedStatement preparedStatement = connection.prepareStatement("insert into links_to_be_processed (link) values (?)");
            preparedStatement.setString(1, "http://sina.cn");
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
