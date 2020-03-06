# Multi-Threaded-Crawler
[![](https://img.shields.io/badge/Circle-CI-brightgreen)](https://circleci.com/gh/NervousOrange/Multi-Threaded-Crawler) [![](https://img.shields.io/badge/微信-联系作者-brightgreen)](https://s2.ax1x.com/2020/03/06/3bV0L8.jpg)

### 多线程网络爬虫与Elasticsearch新闻搜索引擎

使用 Java 编写爬虫，实现对新浪新闻站的 HTTP 请求、模拟登录、Cookie 保存、HTML 解析的功能。筛选链接循环 爬取新闻站内内容，连接 MySQL 数据库实现断点续传功能，使用 Elasticsearch 分析数据，并完成一个简单的搜索引 擎。 

* 使用 Git 进行版本控制，小步提交 PR 至 Github 主分支，用 Maven 进行依赖包的管理，CircleCI 进行自动化测 试，在生命周期绑定 Checkstyle、SpotBugs 插件保证代码质量。 
* 使用 Flyway 自动迁移工具完成数据库初始化建表及添加原始数据工作，用 MyBatis 实现数据与 Java 对象的关系映射，对 MySQL 数据库进行索引优化，使百万级新闻内容的查找效率提升近 2 倍。
*  采用多线程完成爬虫任务，提高爬取效率约 3 倍，使用 Elasticsearch 搜索引擎进行新闻内容的全文检索，实现了 百万级文本内容的快速搜索功能。

## How to build

clone 项目至本地目录：

```shell
git clone https://github.com/NervousOrange/Multi-Threaded-Crawler.git
```

从 Docker 启动 MySQL 数据库：

- [Docker 下载地址](https://www.docker.com/)
- 如果需要持久化数据需要配置 -v 磁盘文件映射

```shell
docker run --name crawler -p 3306:3306 -e MYSQL_ROOT_PASSWORD=123456 -d mysql:8.0
```

使用 IDEA 打开项目，刷新 Maven，再使用开源数据库迁移工具 Flyway 完成自动建表工作：

```shell
mvn flyway:migrate
```

项目测试：

```shell
mvn verify
```

运行项目：

- Run Main 类，就开始爬取数据啦！

### 效果图：

![爬虫效果图](https://s2.ax1x.com/2020/03/06/3LEpHU.png)

### 爬虫逻辑图：

![](https://s2.ax1x.com/2020/03/06/3bZrX6.png)

