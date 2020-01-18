package com.github.NervousOrange;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.sql.*;

public class JDBCCrawlerDAO implements CrawlerDAO {
    Connection connection;
    static final String databaseURL = "jdbc:h2:file:C:/Users/zch69/recipes/Multi-Threaded-Crawler/CrawlerDatabase";

    public JDBCCrawlerDAO() {
        connectDatabase();
    }


    @SuppressFBWarnings("DMI_CONSTANT_DB_PASSWORD")
    public void connectDatabase() {
        try {
            connection = DriverManager.getConnection(databaseURL, "root", "root");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public synchronized String loadLinkFromDatabaseAndDelete() {
        ResultSet resultSet = null;
        String linkToBeProcessed = null;
        try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT LINK FROM LINKS_TO_BE_PROCESSED LIMIT 1")) {
            resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                linkToBeProcessed = (resultSet.getString(1));
                deleteLinkInDatabase(linkToBeProcessed);
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
        return linkToBeProcessed;
    }

    @Override
    public void insertLinkInLinkToBeProcessed(String link) {
        try (PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO LINKS_TO_BE_PROCESSED (link) values (?)")) {
            preparedStatement.setString(1, link);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void insertLinkInLinkAlreadyProcessed(String link) {
        try (PreparedStatement preparedStatement = connection.prepareStatement("insert into LINKS_ALREADY_PROCESSED (link) values(?)")) {
            preparedStatement.setString(1, link);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deleteLinkInDatabase(String link) {
        try (PreparedStatement preparedStatement = connection.prepareStatement("DELETE FROM LINKS_TO_BE_PROCESSED WHERE LINK = ?")) {
            preparedStatement.setString(1, link);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void writeNewsPagesIntoDatabase(String title, String content, String URL) {
        try (PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO NEWS (TITLE, CONTENT, URL, CREATED_AT, MODIFIED_AT) VALUES (?, ?, ?, now(), now())")) {
            preparedStatement.setString(1, title);
            preparedStatement.setString(2, content);
            preparedStatement.setString(3, URL);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean isLinkInDatabase(String link) {
        ResultSet resultSet = null;
        try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT LINK FROM LINKS_ALREADY_PROCESSED WHERE LINK = ?")) {
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

}
