package com.github.NervousOrange;

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
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class Main {
    DatabaseOperation databaseOperation;

    public static void main(String[] args) {
        Main main = new Main();
        try {
            main.run();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void run() throws IOException {
        String databaseURL = "jdbc:h2:file:C:/Users/zch69/recipes/Multi-Threaded-Crawler/CrawlerDatabase";
        databaseOperation = new DatabaseOperation(databaseURL);
        databaseOperation.connectDatabase();
        ArrayList<String> linkPool;
        initialDatabaseFirstTime();
        while (!(linkPool = databaseOperation.loadLinkFromDatabase("select LINK from LINKS_TO_BE_PROCESSED")).isEmpty()) {  // 循环，每次都重新从数据库加载
            String link = linkPool.remove(linkPool.size() - 1);
            databaseOperation.updateLinkInDatabase("DELETE FROM LINKS_TO_BE_PROCESSED WHERE LINK = ?", link);
            if (isInterestLink(link) && isLinkNotBeProcessedYet(link)) {
                databaseOperation.updateLinkInDatabase("insert into LINKS_ALREADY_PROCESSED (link) values(?)", link);
                try (CloseableHttpResponse response1 = getHttpResponse(link)) {
                    HttpEntity entity1 = printStatusLineAndGetEntity(response1);
                    Document doc = parseEntity(entity1);
                    for (String newsLink : getLinkOnThePage(doc)) {
                        databaseOperation.updateLinkInDatabase("INSERT INTO LINKS_TO_BE_PROCESSED (link) values (?)", newsLink);
                    }
                    EntityUtils.consume(entity1);
                }
            }
        }
    }

    public void initialDatabaseFirstTime() {
        ArrayList<String> linkPool = databaseOperation.loadLinkFromDatabase("select LINK from LINKS_TO_BE_PROCESSED");
        if (linkPool.isEmpty() && isLinkNotBeProcessedYet("http://sina.cn")) {
            try {
                databaseOperation.initializeDatabase();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public ArrayList<String> getLinkOnThePage(Document doc) {
        ArrayList<String> linkFromPage = new ArrayList<>();
        ArrayList<Element> aTags = doc.select("a");
        for (Element aTag : aTags) {
            String tempLink = aTag.attr("href");
            if (isANewsLink(tempLink)) {
                System.out.println("A News Link: " + tempLink);
                getNewsTitleAndContent(tempLink);
            } else if (isLinkUseful(tempLink)) {
                linkFromPage.add(tempLink);
            }
        }
        return linkFromPage;
    }

    public void getNewsTitleAndContent(String newsLink) {
        try (CloseableHttpResponse response1 = getHttpResponse(newsLink)) {
            HttpEntity entity1 = printStatusLineAndGetEntity(response1);
            Document doc = parseEntity(entity1);
            if (newsLink.contains("games.sina.cn")) {
                specialHandleWithGamesLink(newsLink);
            } else {
                String title = doc.select("title").text();
                Elements paragraphs = doc.select("p");
                String content = paragraphs.stream().map(Element::text).collect(Collectors.joining("\n"));
                databaseOperation.writeNewsPagesIntoDatabase("INSERT INTO NEWS (TITLE, CONTENT, URL) VALUES (?, ?, ?)", title, content, newsLink);
                System.out.println(title);
            }
            EntityUtils.consume(entity1);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void specialHandleWithGamesLink(String gamesLink) {
        Document gamesDoc = null;
        try {
            gamesDoc = Jsoup.parse(new URL(gamesLink).openStream(), "UTF-8", gamesLink);
        } catch (IOException e) {
            e.printStackTrace();
        }
        assert gamesDoc != null;
        String title = gamesDoc.select("title").text();
        Elements paragraphs = gamesDoc.select("p");
        String content = paragraphs.stream().map(Element::text).collect(Collectors.joining("\n"));
        databaseOperation.writeNewsPagesIntoDatabase("INSERT INTO NEWS (TITLE, CONTENT, URL) VALUES (?, ?, ?)", title, content, gamesLink);
        System.out.println(title);
    }

    // print the link before visit it
    private static CloseableHttpResponse getHttpResponse(String link) throws IOException {
        CloseableHttpClient httpclient = HttpClients.createDefault();
        System.out.println("Visiting Link: " + link);
        HttpGet httpGet = new HttpGet(link);
        httpGet.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/79.0.3945.117 Safari/537.36");
        return httpclient.execute(httpGet);
    }

    private static boolean isInterestLink(String link) {
        return link.contains("sina.cn") && (!link.contains("reload")) && (!link.contains("tousu"));
    }

    private static boolean isANewsLink(String tempLink) {
        return (tempLink.contains("detail") || tempLink.contains("article")) && !tempLink.contains("video.sina.cn");
    }

    private static boolean isLinkUseful(String tempLink) {
        return !tempLink.contains("javascript") && !tempLink.contains("void") && !(tempLink.length() < 2)
                && !tempLink.contains("php?") && !tempLink.contains("live") && !tempLink.contains("passport.sina.cn");
    }

    public boolean isLinkNotBeProcessedYet(String link) {
        return !databaseOperation.isLinkInDatabase("SELECT LINK FROM LINKS_ALREADY_PROCESSED WHERE LINK = ?", link);
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


