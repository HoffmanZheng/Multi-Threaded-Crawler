package com.github.NervousOrange;

import java.util.ArrayList;

public interface DAO {
    ArrayList<String> loadLinkFromDatabase(String sqlStatement);

    // insert or delete a link
    void updateLinkInDatabase(String sqlStatement, String link);

    void writeNewsPagesIntoDatabase(String sqlStatement, String title, String content, String URL);

    boolean isLinkInDatabase(String sqlStatement, String link);
}
