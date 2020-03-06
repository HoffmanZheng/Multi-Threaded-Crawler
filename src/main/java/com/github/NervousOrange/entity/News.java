package com.github.NervousOrange.entity;

import java.time.Instant;

public class News {
    private int id;
    private String title;
    private String content;
    private String URL;
    private Instant createdAt;
    private Instant modifiedAt;

    public News(int id, String title, String content, String URL, Instant createdAt, Instant modifiedAt) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.URL = URL;
        this.createdAt = createdAt;
        this.modifiedAt = modifiedAt;
    }

    public News(News old) {
        this.id = old.id;
        this.title = old.title;
        this.content = old.content;
        this.URL = old.URL;
        this.createdAt = old.createdAt;
        this.modifiedAt = old.modifiedAt;
    }

    public News(String title, String content, String URL) {
        this.title = title;
        this.content = content;
        this.URL = URL;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getURL() {
        return URL;
    }

    public void setURL(String URL) {
        this.URL = URL;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getModifiedAt() {
        return modifiedAt;
    }

    public void setModifiedAt(Instant modifiedAt) {
        this.modifiedAt = modifiedAt;
    }
}
