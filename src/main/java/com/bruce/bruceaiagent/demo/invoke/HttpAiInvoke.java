package com.bruce.bruceaiagent.demo.invoke;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.Header;
import cn.hutool.json.JSONObject;

/**
 * 阿里云灵积 AI SDK
 * 接入方式2： 使用 HTTP 请求调用 AI
 */
public class HttpAiInvoke {
    public static void main(String[] args) {
        // 替换为你的实际 API Key
//        String apiKey = System.getenv("DASHSCOPE_API_KEY");
        String apiKey = TestApiKey.API_KEY;

        // 构建请求体 JSON
        JSONObject body = new JSONObject();
        body.set("model", "qwen-plus");

        // 构建 messages 数组
        JSONObject messages = new JSONObject();
        messages.set("messages", new Object[]{
            new JSONObject().set("role", "system").set("content", "You are a helpful assistant."),
            new JSONObject().set("role", "user").set("content", "你是谁？")
        });
        body.set("input", messages);

        // 设置参数
        body.set("parameters", new JSONObject().set("result_format", "message"));

        // 发送 POST 请求
        HttpResponse response = HttpRequest.post("https://dashscope.aliyuncs.com/api/v1/services/aigc/text-generation/generation")
                .header(Header.AUTHORIZATION, "Bearer " + apiKey)
                .header(Header.CONTENT_TYPE, "application/json")
                .body(body.toString())  // 将 JSON 对象转为字符串
                .execute();

        // 打印响应
        System.out.println("Status Code: " + response.getStatus());
        System.out.println("Response Body: " + response.body());
    }
}