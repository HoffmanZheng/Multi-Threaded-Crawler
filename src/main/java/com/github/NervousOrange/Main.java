package com.github.NervousOrange;

public class Main {

    public static void main(String[] args) {
        CrawlerDAO databaseAccess = new JDBCCrawlerDAO();

        for (int i = 0; i < 5; i++) {
            new Crawler(databaseAccess).start();
        }
    }
}
