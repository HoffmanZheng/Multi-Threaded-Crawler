package com.github.NervousOrange.dao;

public interface CrawlerDAO {
    String loadLinkFromDatabaseAndDelete();

    void insertLinkInLinkToBeProcessed(String link);

    void insertLinkInLinkAlreadyProcessed(String link);

    void deleteLinkInDatabase(String link);

    void writeNewsPagesIntoDatabase(String title, String content, String URL);

    boolean isLinkInDatabase(String link);

}
