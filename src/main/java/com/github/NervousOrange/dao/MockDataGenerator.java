package com.github.NervousOrange.dao;

import com.github.NervousOrange.entity.News;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Random;

public class MockDataGenerator {
    private SqlSessionFactory sqlSessionFactory;

    public static void main(String[] args) {
        MockDataGenerator mockDataGenerator = new MockDataGenerator();
        mockDataGenerator.run();
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

        List<News> newsCollection = loadNewsFromMySQL();
        reproduceNewsIntoMySQL(newsCollection, 20_0000);
    }

    public List<News> loadNewsFromMySQL() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            return session.selectList("com.github.NervousOrange.MockDataMapper.loadNewsFromDatabase");
        }
    }

    public void reproduceNewsIntoMySQL(List<News> newsCollection, int targetData) {
        try (SqlSession session = sqlSessionFactory.openSession(ExecutorType.BATCH)) {
            int count = targetData - newsCollection.size();
            Random random = new Random();
            try {
                while (count-- > 0) {
                    int index = random.nextInt(newsCollection.size());
                    News news = new News(newsCollection.get(index));
                    news.setCreatedAt(news.getCreatedAt().minusSeconds(new Random().nextInt(60 * 60 * 24 * 365)));
                    news.setModifiedAt(news.getModifiedAt().minusSeconds(new Random().nextInt(60 * 60 * 24 * 365)));
                    if (news.getContent().length() > 25) {
                        news.setContent(news.getContent().substring(0, 25));
                    }
                    session.selectOne("com.github.NervousOrange.MockDataMapper.insertNews", news);
                    if (count % 2000 == 0) {
                        session.flushStatements();
                        System.out.println("Left: " + count);
                    }
                }
                session.commit();
            } catch (Exception e) {
                session.rollback();
                throw new RuntimeException(e);
            }
        }
    }
}
