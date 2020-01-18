package com.github.NervousOrange;

public interface CrawlerDAO {
    String loadLinkFromDatabase();

    void insertLinkInLinkToBeProcessed(String link);

    void insertLinkInLinkAlreadyProcessed(String link);

    void deleteLinkInDatabase(String link);

    void writeNewsPagesIntoDatabase(String title, String content, String URL);

    boolean isLinkInDatabase(String link);

}
