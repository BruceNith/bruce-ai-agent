package com.bruce.bruceaiagent.demo.invoke;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 用于获取 DashScope API Key 的配置组件
 */
@Component
@ConfigurationProperties(prefix = "ai.dashscope")
public class TestApiKey {

    private String apiKey;
    public static String API_KEY;

    @PostConstruct
    public void init() {
        API_KEY = this.apiKey;
    }
    public String getApiKey() {
        return apiKey;
    }

//    public void setApiKey(String apiKey) {
//        this.apiKey = apiKey;
//    }
}
