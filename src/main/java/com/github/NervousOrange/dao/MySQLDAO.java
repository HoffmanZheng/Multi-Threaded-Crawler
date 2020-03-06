package com.github.NervousOrange.dao;

import com.github.NervousOrange.entity.News;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

public class MySQLDAO implements CrawlerDAO {
    private SqlSessionFactory sqlSessionFactory;

    public MySQLDAO() {
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
    }


    @Override
    public synchronized String loadLinkFromDatabaseAndDelete() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            String linkLoadFromDatabase = session.selectOne("com.github.NervousOrange.CrawlerMapper.loadLinkFromDatabase");
            deleteLinkInDatabase(linkLoadFromDatabase);
            return linkLoadFromDatabase;
        }
    }

    @Override
    public void insertLinkInLinkToBeProcessed(String link) {
        insertLinkIntoDatabase(link, "LINKS_TO_BE_PROCESSED");
    }

    @Override
    public void insertLinkInLinkAlreadyProcessed(String link) {
        insertLinkIntoDatabase(link, "LINKS_ALREADY_PROCESSED");
    }

    private void insertLinkIntoDatabase(String link, String tableName) {
        try (SqlSession session = sqlSessionFactory.openSession(true)) {
            HashMap param = new HashMap();
            param.put("tableName", tableName);
            param.put("link", link);
            session.selectOne("com.github.NervousOrange.CrawlerMapper.insertLinkIntoDatabase", param);
        }
    }


    @Override
    public void deleteLinkInDatabase(String link) {
        try (SqlSession session = sqlSessionFactory.openSession(true)) {
            session.selectOne("com.github.NervousOrange.CrawlerMapper.deleteLINKS_TO_BE_PROCESSED", link);
        }
    }


    @Override
    public void writeNewsPagesIntoDatabase(String title, String content, String URL) {
        try (SqlSession session = sqlSessionFactory.openSession(true)) {
            session.selectOne("com.github.NervousOrange.CrawlerMapper.insertNews", new News(title, content, URL));
        }
    }

    @Override
    public boolean isLinkInDatabase(String link) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            int count = session.selectOne("com.github.NervousOrange.CrawlerMapper.isLinkInDatabase", link);
            return count != 0;
        }
    }
}
