package com.github.NervousOrange;

import java.io.IOException;

public class Main {

    public static void main(String[] args) {
        Crawler crawler = new Crawler();
        try {
            crawler.run();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
