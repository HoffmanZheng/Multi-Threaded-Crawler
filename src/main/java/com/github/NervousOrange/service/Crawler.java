package com.github.NervousOrange.service;

import com.github.NervousOrange.dao.CrawlerDAO;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;

public class Crawler implements Runnable {
    CrawlerDAO databaseAccess;
    CountDownLatch latch;

    public Crawler(CrawlerDAO databaseAccess, CountDownLatch latch) {
        this.databaseAccess = databaseAccess;
        this.latch = latch;
    }

    public Crawler(CrawlerDAO databaseAccess) {
        this.databaseAccess = databaseAccess;
    }

    @Override
    public void run() {
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        try {
            String linkLoadFromDatabase;            // 循环，每次都重新从数据库加载
            while ((linkLoadFromDatabase = databaseAccess.loadLinkFromDatabaseAndDelete()) != null) {
                if (isLinkNotBeProcessedYet(linkLoadFromDatabase)) {
                    if (isANewsLink(linkLoadFromDatabase)) {
                        getNewsTitleAndContent(linkLoadFromDatabase);
                    }
                    if (isInterestLink(linkLoadFromDatabase)) {
                        dealWithTheInterestLink(linkLoadFromDatabase);
                    }
                    databaseAccess.insertLinkInLinkAlreadyProcessed(linkLoadFromDatabase);
                }
            }
        } finally {
            latch.countDown();
        }
    }


    private void dealWithTheInterestLink(String link) {
        try (CloseableHttpResponse response1 = getHttpResponse(link)) {
            HttpEntity entity1 = printStatusLineAndGetEntity(response1);
            Document doc = parseEntity(entity1);
            for (String newsLink : getLinkOnThePage(doc)) {
                databaseAccess.insertLinkInLinkToBeProcessed(newsLink);
            }
            EntityUtils.consume(entity1);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public ArrayList<String> getLinkOnThePage(Document doc) {
        ArrayList<String> linkFromPage = new ArrayList<>();
        ArrayList<Element> aTags = doc.select("a");
        for (Element aTag : aTags) {
            String tempLink = aTag.attr("href");
            if (isLinkUseful(tempLink)) {
                linkFromPage.add(tempLink);
            }
        }
        return linkFromPage;
    }

    public void getNewsTitleAndContent(String link) {
        System.out.println("A News Link: " + link);
        Document newsDoc;
        try (CloseableHttpResponse response1 = getHttpResponse(link)) {
            HttpEntity entity1 = printStatusLineAndGetEntity(response1);
            newsDoc = Jsoup.parse(entity1.getContent(), "UTF-8", link);
            assert newsDoc != null;
            String title = newsDoc.select("title").text();
            Elements paragraphs = newsDoc.select("p");
            String content = paragraphs.stream().map(Element::text).collect(Collectors.joining("\n"));
            databaseAccess.writeNewsPagesIntoDatabase(title, content, link);
            System.out.println(title);
            EntityUtils.consume(entity1);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // print the link before visit it
    private static CloseableHttpResponse getHttpResponse(String link) throws IOException {
        CloseableHttpClient httpclient = HttpClients.createDefault();
        System.out.println("Visiting Link: " + link);
        if (link.startsWith("//")) {
            link = "http:" + link;
        }

        HttpGet httpGet = new HttpGet(link);
        httpGet.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/79.0.3945.117 Safari/537.36");
        return httpclient.execute(httpGet);
    }

    private static boolean isInterestLink(String link) {
        return link.contains("sina.cn") && (!link.contains("reload")) && (!link.contains("tousu"))
                && !link.contains("video.sina.cn") && !link.contains("book.sina.cn") && !link.contains("k.sina.cn")
                && !link.contains("my.sina.cn") && !link.contains("auto.sina.cn") && !link.contains("so.sina.cn")
                && !link.contains("\"/'\"/");
    }

    private static boolean isANewsLink(String tempLink) {
        return (tempLink.contains("album") || tempLink.contains("detail") || tempLink.contains("article"));
    }

    private static boolean isLinkUseful(String tempLink) {
        return !tempLink.contains("javascript") && !tempLink.contains("void") && !(tempLink.length() < 2)
                && !tempLink.contains("php?") && !tempLink.contains("live") && !tempLink.contains("passport.sina.cn")
                && !tempLink.contains("\"/'\"/") && tempLink.length() < 3000;
    }

    public boolean isLinkNotBeProcessedYet(String link) {
        return !databaseAccess.isLinkInDatabase(link);
    }

    private static HttpEntity printStatusLineAndGetEntity(CloseableHttpResponse response1) {
        System.out.println(response1.getStatusLine());
        return response1.getEntity();
    }

    private static Document parseEntity(HttpEntity entity1) throws IOException {
        String html = EntityUtils.toString(entity1);
        return Jsoup.parse(html);
    }
}
