/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.shadialian.linguistics.main;

/**
 *
 * @author shadi
 */
import ch.qos.logback.classic.Level;
import ch.qos.logback.core.FileAppender;
import com.shadialian.linguistics.crawler.NewsCrawler;
import com.shadialian.linguistics.storage.Data;
import com.uwyn.jhighlight.tools.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtConfig;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;

public class Main {

    private static final Logger logger = LoggerFactory.getLogger(Main.class);    
    public static final Data data = Data.getData();
    public static void main(String[] args) throws Exception {
        //ch.qos.logback.classic.Logger rootLogger = (ch.qos.logback.classic.Logger)LoggerFactory.getLogger(ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME);
        //rootLogger.setLevel(Level.toLevel("info"));

        /*
         if (args.length != 2) {
         logger.info("Needed parameters: ");
         logger.info("\t rootFolder (it will contain intermediate crawl data)");
         logger.info("\t numberOfCralwers (number of concurrent threads)");
         return;
         }
         */
        /*
         * crawlStorageFolder is a folder where intermediate crawl data is
         * stored.
         */
        //String crawlStorageFolder = args[0];
        String crawlStorageFolder = "local-data";

        /*
         * numberOfCrawlers shows the number of concurrent threads that should
         * be initiated for crawling.
         */
        //int numberOfCrawlers = Integer.parseInt(args[1]);
        int numberOfCrawlers = 8;

        CrawlConfig config = new CrawlConfig();

        config.setCrawlStorageFolder(crawlStorageFolder);

        /*
         * Be polite: Make sure that we don't send more than 1 request per
         * second (1000 milliseconds between requests).
         */
        config.setPolitenessDelay(1000);

        /*
         * You can set the maximum crawl depth here. The default value is -1 for
         * unlimited depth
         */
        //config.setMaxDepthOfCrawling(2);

        /*
         * You can set the maximum number of pages to crawl. The default value
         * is -1 for unlimited number of pages
         */
        //config.setMaxPagesToFetch(1000);
        /**
         * Do you want crawler4j to crawl also binary data ? example: the
         * contents of pdf, or the metadata of images etc
         */
        config.setIncludeBinaryContentInCrawling(false);

        /*
         * Do you need to set a proxy? If so, you can use:
         * config.setProxyHost("proxyserver.example.com");
         * config.setProxyPort(8080);
         *
         * If your proxy also needs authentication:
         * config.setProxyUsername(username); config.getProxyPassword(password);
         */
        //config.setProxyHost("10.1.1.2");
        //config.setProxyPort(8080);

        /*
         * This config parameter can be used to set your crawl to be resumable
         * (meaning that you can resume the crawl from a previously
         * interrupted/crashed crawl). Note: if you enable resuming feature and
         * want to start a fresh crawl, you need to delete the contents of
         * rootFolder manually.
         */
        config.setResumableCrawling(false);

        /*
         * Instantiate the controller for this crawl.
         */
        PageFetcher pageFetcher = new PageFetcher(config);
        RobotstxtConfig robotstxtConfig = new RobotstxtConfig();
        RobotstxtServer robotstxtServer = new RobotstxtServer(robotstxtConfig, pageFetcher);
        CrawlController controller = new CrawlController(config, pageFetcher, robotstxtServer);
        int max = addSeenUrls( controller );
        data.setCounter(max++);
        /*
         * For each crawl, you need to add some seed urls. These are the first
         * URLs that are fetched and then the crawler starts following links
         * which are found in these pages
         */
        controller.addSeed("http://aljazeera.net/news/arabic/");
        controller.addSeed("http://www.bbc.co.uk/arabic");
        controller.addSeed("http://www.alarabiya.net/latest-news.html");
        controller.addSeed("http://www.bbc.co.uk/arabic/topics/video");
        controller.addSeed("http://aljazeera.net/news/international/");
        controller.addSeed("http://www.bbc.co.uk/arabic/middleeast");
        controller.addSeed("http://www.alarabiya.net/");
        controller.addSeed("http://www.bbc.co.uk/arabic/worldnews");
        controller.addSeed("http://aljazeera.net/news/reportsandinterviews/");
        controller.addSeed("http://www.bbc.co.uk/arabic/business");
        controller.addSeed("http://www.alarabiya.net/aswaq.html");
        controller.addSeed("http://www.bbc.co.uk/arabic/artandculture");
        controller.addSeed("http://aljazeera.net/news/humanrights/");
        controller.addSeed("http://www.bbc.co.uk/arabic/scienceandtech");
        controller.addSeed("http://www.alarabiya.net/north-africa.html");
        controller.addSeed("http://www.bbc.co.uk/arabic/sports");
        controller.addSeed("http://aljazeera.net/news/presstour/");
        controller.addSeed("http://www.bbc.co.uk/arabic/topics/magazine");
        controller.addSeed("http://www.alarabiya.net/sport.html");
        controller.addSeed("http://www.bbc.co.uk/arabic/interactivity");
        controller.addSeed("http://aljazeera.net/news/scienceandtechnology/");
        controller.addSeed("http://www.bbc.co.uk/arabic/inthepress");
        controller.addSeed("http://www.alarabiya.net/medicine-and-health.html");
        controller.addSeed("http://www.bbc.co.uk/arabic/topics/trending");
        controller.addSeed("http://aljazeera.net/news/cultureandart/");
        controller.addSeed("http://www.bbc.co.uk/arabic/indepth");
        controller.addSeed("http://www.alarabiya.net/culture-and-art.html");
        controller.addSeed("http://aljazeera.net/news/miscellaneous/");
        controller.addSeed("http://www.alarabiya.net/technology.html");
        controller.addSeed("http://aljazeera.net/news/sport/");
        controller.addSeed("http://www.alarabiya.net/last-page.html");
        controller.addSeed("http://aljazeera.net/news/ebusiness/");
        controller.addSeed("http://www.alarabiya.net/views.html");
        controller.addSeed("http://aljazeera.net/news/presstour/");
        /*
         * Start the crawl. This is a blocking operation, meaning that your code
         * will reach the line after this only when crawling is finished.
         */
        controller.start(NewsCrawler.class, numberOfCrawlers);
    }

    private static int addSeenUrls( CrawlController controller ){
        Scanner urlScan;
        int id = 1 ;
        try {
            urlScan = new Scanner(new File("logs/urls.log")).useDelimiter(";|\\n");
            
            while( urlScan.hasNext() ){      
                urlScan.nextInt();
                String url = urlScan.next();
                controller.addSeenUrl(url,id++);
            }
        } catch (FileNotFoundException ex) {
            logger.warn("Could not find urls.log in logs");
        }
        return id;    
    }
    
}
