package com.bruce.bruceaiagent.tools;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.io.IOException;

/**
 * Web scraping tool for extracting content from web pages. 网页抓取工具，用于从网页中提取内容
 **/
public class WebScrapingTool {

    @Tool(description = "Scrape the content of a web page")     // 用英文描述效果会更好
    public String scrapeWebPage(@ToolParam(description = "URL of the web page to scrape") String url) {
        try {
            Document doc = Jsoup.connect(url).get();
            return doc.html();
        } catch (IOException e) {
            return "Error scraping web page: " + e.getMessage();
        }
    }
}
