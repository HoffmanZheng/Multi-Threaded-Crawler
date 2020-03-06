package com.github.NervousOrange;

import com.github.NervousOrange.dao.CrawlerDAO;
import com.github.NervousOrange.dao.MySQLDAO;
import com.github.NervousOrange.service.Crawler;

import java.util.concurrent.CountDownLatch;


public class Main {

    public static void main(String[] args) {
        runCrawler();
    }

    public static void runCrawler() {
        final CountDownLatch latch = new CountDownLatch(4);
        CrawlerDAO databaseAccess = new MySQLDAO();
        Crawler crawler = new Crawler(databaseAccess, latch);

        while (true) {
            for (int i = 0; i < 4; i++) {
                new Thread(crawler).start();
            }
            try {
                latch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
