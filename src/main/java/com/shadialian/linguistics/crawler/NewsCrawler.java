/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.shadialian.linguistics.crawler;

import com.googlecode.concurrenttrees.radix.ConcurrentRadixTree;
import com.googlecode.concurrenttrees.radix.RadixTree;
import com.googlecode.concurrenttrees.radix.node.concrete.SmartArrayBasedNodeFactory;
import com.googlecode.concurrenttrees.radix.node.concrete.voidvalue.VoidValue;
import com.shadialian.linguistics.storage.Data;
import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.frontier.Frontier;
import edu.uci.ics.crawler4j.parser.HtmlParseData;
import edu.uci.ics.crawler4j.url.WebURL;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import org.apache.http.Header;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 *
 * @author shadi
 */
public class NewsCrawler extends WebCrawler {
    private static final Pattern BINARY_FILES_EXTENSIONS
            = Pattern.compile(".*\\.(bmp|gif|jpe?g|png|tiff?|pdf|ico|xaml|pict|rif|pptx?|ps"
                    + "|mid|mp2|mp3|mp4|wav|wma|au|aiff|flac|ogg|3gp|aac|amr|au|vox"
                    + "|avi|mov|mpe?g|ra?m|m4v|smil|wm?v|swf|aaf|asf|flv|mkv"
                    + "|zip|rar|gz|7z|aac|ace|alz|apk|arc|arj|dmg|jar|lzip|lha)"
                    + "(\\?.*)?$"); // For url Query parts ( URL?q=... )
    private static final Pattern STARTS_WITH_JAZEERA
            = Pattern.compile("http://aljazeera\\.net/news/\\w+/\\d+/\\d+/\\d+/.*");
    private static final Pattern STARTS_WITH_ARABIYA
            = Pattern.compile("http://www\\.alarabiya\\.net/ar/[\\D\\w+/]+\\d+/\\d+/\\d+/.*");
    private static final Pattern STARTS_WITH_BBCARABIC
            = Pattern.compile("http://www\\.bbc\\.co\\.uk/arabic/\\w+/\\d+/\\d+/.*");
    //private static final RadixTree storedUrls = new ConcurrentRadixTree(new SmartArrayBasedNodeFactory());
    private static final Logger docsInfo = LoggerFactory.getLogger("com.shadialian.docsInfo");
    private static Data data = Data.getData();
    /**
     * You should implement this function to specify whether the given url
     * should be crawled or not (based on your crawling logic).
     */
    @Override
    public boolean shouldVisit(Page referringPage, WebURL url) {
        String href = url.getURL().toLowerCase();
        return !BINARY_FILES_EXTENSIONS.matcher(href).matches()
                && (href.startsWith("http://aljazeera.net") 
                    || href.startsWith("http://www.alarabiya.net/")
                    || href.startsWith("http://www.bbc.co.uk/arabic")
                   )
                //&& storedUrls.getValueForExactKey(href) == null
                ;
    }
    /**
     * This function is called when a page is fetched and ready to be processed
     * by your program.
     */
    @Override
    public void visit(Page page) {
        int docid = page.getWebURL().getDocid();
        String url = page.getWebURL().getURL().toLowerCase();
        String domain = page.getWebURL().getDomain();
        String path = page.getWebURL().getPath();
        String subDomain = page.getWebURL().getSubDomain();
        String parentUrl = page.getWebURL().getParentUrl();
        String anchor = page.getWebURL().getAnchor();

        logger.debug("Docid: {}", docid);
        logger.debug("Domain: '{}'", domain);
        logger.debug("Sub-domain: '{}'", subDomain);
        logger.debug("Path: '{}'", path);
        logger.debug("Parent page: {}", parentUrl);
        logger.debug("Anchor text: {}", anchor);

        if (page.getParseData() instanceof HtmlParseData) {
            HtmlParseData htmlParseData = (HtmlParseData) page.getParseData();
            String text = htmlParseData.getText();
            String html = htmlParseData.getHtml();
            Set<WebURL> links = htmlParseData.getOutgoingUrls();
            //myController.getFrontier().scheduleAll(new ArrayList<WebURL>(links));
            Document doc = Jsoup.parse(html);
            if (STARTS_WITH_JAZEERA.matcher(url).matches()) { // al jazeera news
                int start = url.indexOf('/', 26);
                String section = url.substring(26, start).trim().toLowerCase();
                if (!section.equals("caricature") && !section.equals("photogalary")) {
                    //logger.info("URL: {}, Docid: {}", url, docid);
                    String content = doc.select("html #Story div #main div #main-content article#main-story.page-section div.article-body div#DynamicContentContainer").text().trim();
                    if (content.length() != 0) {
                        int sectionId = data.addSectionIfNotExistAndReturnId(section);
                        int sourceId = data.addSourceIfNotExistAndReturnId("Jazeera");
                        String title = doc.select("html #Story div #main div #main-content article#main-story.page-section header#header-story hgroup h1.heading-story").text().trim();
                        long fileId = data.writeFile(content);
                        int end = url.indexOf('/', start + 1);
                        end = url.indexOf('/', end + 1);
                        end = url.indexOf('/', end + 1);
                        String date = url.substring(start + 1, end).trim();
                        Elements atags = doc.select("html #Story div #main div div#main-content article#main-story.page-section div.article-body div.article-tags a");
                        int [] tagIds = new int[atags.size()];
                        int i= 0 ;
                        for (Element e : atags) {
                             tagIds[i++] = data.addTagIfNotExistAndReturnId(e.text().trim());
                        }
                        String tags = Arrays.toString(tagIds);
                        logger.info("URL: {}, Docid: {}, Title: {}, Date: {}, Tags: {}, Content: {}", url, docid, title, date, atags.size(), content.length());
                        docsInfo.info("{};{};{};{};{};{}", fileId , title , date , sectionId, sourceId, tags );
                        //storedUrls.put(url, VoidValue.SINGLETON);
                    }
                }
            } else if (STARTS_WITH_ARABIYA.matcher(url).matches()) { // al arabiya news
                String content = doc.select("html body.article_tem div.page_wrapper div.page_content div article.multi_articles div.article-body").text().trim();
                if (content.length() != 0) {
                    long fileId = data.writeFile(content);
                    String title = doc.select("html body.article_tem div.page_wrapper div.page_content div article.multi_articles div.highline").text().trim();
                    Elements atags = doc.select("html body.article_tem div.page_wrapper div.page_content div article.multi_articles div.txt_focus div.tags a");
                    int [] tagIds = new int[atags.size()];
                        int i= 0 ;
                        for (Element e : atags) {
                             tagIds[i++] = data.addTagIfNotExistAndReturnId(e.text().trim());
                        }
                    String tags = Arrays.toString(tagIds);
                    int start = "http://www.alarabiya.net/ar/".length();
                    String section = url.substring(start, url.indexOf('/', start) );
                    int sectionId = data.addSectionIfNotExistAndReturnId(section);
                    int sourceId = data.addSourceIfNotExistAndReturnId("Arabiya");
                    do {
                        start = url.indexOf('/', start) + 1;
                    } while (!Character.isDigit(url.charAt(start)));
                    int end = url.indexOf('/', start);
                    end = url.indexOf('/', end + 1);
                    end = url.indexOf('/', end + 1);
                    String date = url.substring(start, end);
                    logger.info("URL: {}, Docid: {}, Title: {}, Date: {}, Tags: {}, Content: {}", url, docid, title, date, atags.size(), content.length());
                    docsInfo.info("{};{};{};{};{};{}", fileId , title , date , sectionId, sourceId, tags );
                    //storedUrls.put(url, VoidValue.SINGLETON);
                }
            }else if( STARTS_WITH_BBCARABIC.matcher(url).matches() ){
                int start = "http://www.bbc.co.uk/arabic/".length();
                int end = url.indexOf('/', start);
                String section = url.substring(start, end);
                if( !section.matches("multimedia|tvandradio")){
                    String content = doc.select("#responsive-news #asset-type-sty div #orb-modules #site-container #page div div div.column--primary div.story-body div.story-body__inner p").text().trim();
                    if( content.length() != 0 ){        
                        long fileId = data.writeFile(content);
                        int sectionId = data.addSectionIfNotExistAndReturnId(section);
                        int sourceId = data.addSourceIfNotExistAndReturnId("BBCArabic");
                        start = end + 1 ;
                        end = url.indexOf('/', end+1);
                        end = url.indexOf('/', end+1);
                        String date = url.substring(start, end+1) + url.substring(end+5, end+7);
                        String title = doc.select("#responsive-news #asset-type-sty div #orb-modules #site-container #page div div div.column--primary div.story-body h1.story-body__h1").text().trim();
                        String tags = "[]";
                        logger.info("URL: {}, Docid: {}, Title: {}, Date: {}, Tags: {}, Content: {}", url, docid, title, date, section , content.length());
                        docsInfo.info("{};{};{};{};{};{}", fileId , title , date , sectionId, sourceId, tags );
                    }
                }
            }
            logger.debug("Text length: {}", text.length());
            logger.debug("Html length: {}", html.length());
            logger.debug("Number of outgoing links: {}", links.size());
        }
        Header[] responseHeaders = page.getFetchResponseHeaders();
        if (responseHeaders != null) {
            logger.debug("Response headers:");
            for (Header header : responseHeaders) {
                logger.debug("\t{}: {}", header.getName(), header.getValue());
            }
        }
        logger.debug("=============");
    }
}
