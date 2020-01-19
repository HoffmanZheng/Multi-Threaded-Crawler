package com.github.NervousOrange;
import java.util.concurrent.CountDownLatch;

public class Main {

    public static void main(String[] args) {
        runCrawler();
    }

    public static void runCrawler() {
        final CountDownLatch latch = new CountDownLatch(4);
        CrawlerDAO databaseAccess = new MyBatisCrawlerDAO();
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
