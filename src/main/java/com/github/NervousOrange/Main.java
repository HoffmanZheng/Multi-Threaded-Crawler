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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.stream.Collectors;

public class Main {
    static HashSet<String> newsLinkPool = new HashSet<>();
    static HashSet<String> processedLink;
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

        while (true) {  // 循环，每次都重新从数据库加载
            ArrayList<String> linkPool = databaseOperation.loadLinkFromDatabase("select LINK from LINKS_TO_BE_PROCESSED");
            processedLink = new HashSet<>(databaseOperation.loadLinkFromDatabase("select LINK from LINKS_ALREADY_PROCESSED"));
            while (!linkPool.isEmpty()) {
                String link = linkPool.remove(linkPool.size() - 1);
                databaseOperation.updateDatabase("DELETE FROM LINKS_TO_BE_PROCESSED WHERE LINK = ?", link);
                if (isInterestLink(link) && !isLinkAlreadyProcessed(link)) {
                    databaseOperation.updateDatabase("insert into LINKS_ALREADY_PROCESSED (link) values(?)", link);
                    try (CloseableHttpResponse response1 = getHttpResponse(link)) {
                        HttpEntity entity1 = printStatusLineAndGetEntity(response1);
                        Document doc = parseEntity(entity1);
                        linkPool.addAll(getLinkOnThePage(doc));
                        EntityUtils.consume(entity1);
                    }
                }
            }
        }
    }

    public static ArrayList<String> getLinkOnThePage(Document doc) {
        ArrayList<String> linkFromPage = new ArrayList<>();
        ArrayList<Element> aTags = doc.select("a");
        for (Element aTag : aTags) {
            String tempLink = aTag.attr("href");
            if (isANewsLink(tempLink)) {
                System.out.println("A News Link: " + tempLink);
                getNewsTitleAndContent(tempLink);
                newsLinkPool.add(tempLink);
            } else if (excludeUselessLink(tempLink)) {
                linkFromPage.add(tempLink);
            }
        }
        return linkFromPage;
    }

    public static void getNewsTitleAndContent(String newsLink) {
        try (CloseableHttpResponse response1 = getHttpResponse(newsLink)) {
            HttpEntity entity1 = printStatusLineAndGetEntity(response1);
            Document doc = parseEntity(entity1);
            if (newsLink.contains("games.sina.cn")) {
                specialHandleWithGamesLink(newsLink);
            } else {
                String title = doc.select("title").text();
                Elements paragraphs = doc.select("p");
                String content = paragraphs.stream().map(Element::text).collect(Collectors.joining("\n"));
                System.out.println(title);
            }
            EntityUtils.consume(entity1);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean isLinkAlreadyProcessed(String link) {
        return databaseOperation.isLinkInDatabase("SELECT LINK FROM LINKS_ALREADY_PROCESSED WHERE LINK = ?", link);
    }

    public static void specialHandleWithGamesLink(String gamesLink) {
        Document gamesDoc = null;
        try {
            gamesDoc = Jsoup.parse(new URL(gamesLink).openStream(), "UTF-8", gamesLink);
        } catch (IOException e) {
            e.printStackTrace();
        }
        String title = gamesDoc.select("title").text();
        Elements paragraphs = gamesDoc.select("p");
        String content = paragraphs.stream().map(Element::text).collect(Collectors.joining("\n"));
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
        return link.contains("sina.cn") && (!link.contains("reload")) && (!processedLink.contains(link)) && (!link.contains("tousu"));
    }

    private static boolean isANewsLink(String tempLink) {
        return (tempLink.contains("detail") || tempLink.contains("article")) && !tempLink.contains("video.sina.cn");
    }

    // 不是新闻内容页，加入链接池，排除异常链接：包含 passport、php?、mail、live、排除已经处理过的链接
    private static boolean excludeUselessLink(String tempLink) {
        return !tempLink.contains("javascript") && !tempLink.contains("void") && !(tempLink.length() < 2)
                && !tempLink.contains("php?") && !tempLink.contains("live") && !tempLink.contains("passport.sina.cn");
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


