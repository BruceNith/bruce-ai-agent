package com.bruce.bruceaiagent;

import com.bruce.bruceaiagent.rag.PgVectorVectorStoreConfig;
import org.springframework.ai.autoconfigure.vectorstore.pgvector.PgVectorStoreAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(exclude = PgVectorStoreAutoConfiguration.class)
public class BruceAiAgentApplication {

    public static void main(String[] args) {
        SpringApplication.run(BruceAiAgentApplication.class, args);
    }

}
