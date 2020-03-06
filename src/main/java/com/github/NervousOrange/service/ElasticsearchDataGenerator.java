package com.github.NervousOrange.service;

import com.github.NervousOrange.entity.News;
import org.apache.http.HttpHost;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ElasticsearchDataGenerator {
    private SqlSessionFactory sqlSessionFactory;
    private List<News> newsCollection;

    public static void main(String[] args) {
        ElasticsearchDataGenerator elasticsearchDataGenerator = new ElasticsearchDataGenerator();
        elasticsearchDataGenerator.run();
    }

    public void run() {
        String resource = "db/MyBatis/config.xml";
        InputStream inputStream;
        {
            try {
                inputStream = Resources.getResourceAsStream(resource);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
        newsCollection = loadNewsFromMySQL();
        for (int i = 0; i < 4; i++) {
            new Thread(this::writeDataIntoElasticsearch).start();
        }
    }

    public List<News> loadNewsFromMySQL() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            return session.selectList("com.github.NervousOrange.MockDataMapper.loadNewsFromDatabase");
        }
    }

    public void writeDataIntoElasticsearch() {
        try (RestHighLevelClient client = new RestHighLevelClient(RestClient.builder(
                new HttpHost("localhost", 9200, "http")))) {
            BulkRequest bulkRequest = new BulkRequest();
            for (int i = 0; i < 3; i++) {
                for (News news : newsCollection) {
                    IndexRequest request = new IndexRequest("news");
                    Map<String, Object> data = new HashMap<>();
                    data.put("title", news.getTitle());
                    if (news.getContent().length() > 20) {
                        news.setContent(news.getContent().substring(0, 20));
                    }
                    data.put("content", news.getContent());
                    data.put("url", news.getURL());
                    data.put("createdAt", news.getCreatedAt());
                    data.put("modifiedAt", news.getModifiedAt());
                    request.source(data, XContentType.JSON);
                    bulkRequest.add(request);
                }
                BulkResponse bulkResponse = client.bulk(bulkRequest, RequestOptions.DEFAULT);
                System.out.println("Current Thread: " + Thread.currentThread().getName() + " finish " + i + bulkResponse.status().getStatus());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
