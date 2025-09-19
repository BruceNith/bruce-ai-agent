package com.bruce.bruceaiagent.demo.rag;

import com.bruce.bruceaiagent.demo.rag.MultiQueryExpanderDemo;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.ai.rag.Query;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
class MultiQueryExpanderDemoTest {

    @Resource
    private MultiQueryExpanderDemo multiQueryExpanderDemo;
    @Test
    void expand() {
        List<Query> queries = multiQueryExpanderDemo.expand("谁是程序员鱼皮啊？saaaaaaaaaaaaa");
        Assertions.assertNotNull( queries);
    }

}