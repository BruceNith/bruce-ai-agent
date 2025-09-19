package com.bruce.bruceaiagent.app;

import com.bruce.bruceaiagent.advisor.MyLoggerAdvisor;
import com.bruce.bruceaiagent.advisor.ReReadingAdvisor;
import com.bruce.bruceaiagent.chatMemory.FileBasedChatMemory;
import com.bruce.bruceaiagent.rag.LoveAppRagCustomAdvisorFactory;
import com.bruce.bruceaiagent.rag.QueryRewriter;
import com.bruce.bruceaiagent.tools.ToolRegistration;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Component;

import java.util.List;

import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY;
import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_RETRIEVE_SIZE_KEY;

@Component
@Slf4j
public class LoveApp {

    private final ChatClient chatClient;

    private static final String SYSTEM_PROMPT = "扮演深耕恋爱心理领域的专家。开场向用户表明身份，告知用户可倾诉恋爱难题。" +
            "围绕单身、恋爱、已婚三种状态提问：单身状态询问社交圈拓展及追求心仪对象的困扰；" +
            "恋爱状态询问沟通、习惯差异引发的矛盾；已婚状态询问家庭责任与亲属关系处理的问题。" +
            "引导用户详述事情经过、对方反应及自身想法，以便给出专属解决方案。";

    @Resource
    private VectorStore loveAppVectorStore;

    @Resource
    private Advisor loveAppRagCloudAdvisor;

    @Resource
    private VectorStore pgVectorVectorStore;

    @Resource
    private QueryRewriter queryRewriter;

    /**
     * 创建基于Dashscope的ChatClient实例
     */
    public LoveApp(ChatModel dashscopeChatModel) {
        // 初始化基于文件的对话记忆
        String fileDir = System.getProperty("user.dir") + "/tmp/chat-memory";
        ChatMemory chatMemory = new FileBasedChatMemory(fileDir);

        // 初始化基于内存的对话记忆
//        ChatMemory chatMemory = new InMemoryChatMemory();
        chatClient = ChatClient.builder(dashscopeChatModel)
                .defaultSystem(SYSTEM_PROMPT)
                .defaultAdvisors(
                        new MessageChatMemoryAdvisor(chatMemory),
                        // 自定义日志 Advisor, 按需开启
                        new MyLoggerAdvisor()
                        // 自定义推理增加 advisor , 按需开启  一般不开启，会多次调用Token翻倍
//                        , new ReReadingAdvisor()
                )
                .build();
    }

    /**
     * 确保输入消息不超过DashScope API的最大长度限制(129024个字符)
     * @param message 原始输入消息
     * @return 截断后的消息（如果需要）
     */
    private String ensureInputLength(String message) {
        final int MAX_INPUT_LENGTH = 129024;
        if (message != null && message.length() > MAX_INPUT_LENGTH) {
            log.warn("Input message length ({}) exceeds maximum allowed length ({}). Truncating message.", 
                    message.length(), MAX_INPUT_LENGTH);
            return message.substring(0, MAX_INPUT_LENGTH);
        }
        return message;
    }

    /**
     * AI 基础对话（支持多轮对话记忆）
     *
     * @param message
     * @param chatId
     * @return
     */
    public String doChat(String message, String chatId) {
        // 限制输入消息长度，防止超出DashScope API限制(129024个字符)
        message = ensureInputLength(message);
        
        ChatResponse response = chatClient
                .prompt()
                .user(message)
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                .call()
//                .entity()
                .chatResponse();
        // 将结果展开，方便后面操作和入数据库
        String content = response.getResult().getOutput().getText();
        log.info("content: {}", content);
        return content;
    }

    record  LoveReport(String title, List<String> suggestions) {
    }

    /**
     * AI 恋爱报告功能（实战结构化输出 ）
     *
     * @param message
     * @param chatId
     * @return
     */
    public LoveReport doChatWithReport(String message, String chatId) {
        // 限制输入消息长度，防止超出DashScope API限制(129024个字符)
        message = ensureInputLength(message);
        
        LoveReport loveReport = chatClient
                .prompt()
                .system(SYSTEM_PROMPT + "每次对话后都要生成恋爱结果，标题为{用户名}的恋爱报告，内容为建议列表")
                .user(message)
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                .call()
                .entity(LoveReport.class);
        log.info("loveReport: {}", loveReport);
        return loveReport;
    }

    /**
     * 和 RAG 知识库进行对话
     *
     * @param message
     * @param chatId
     * @return
     */
    public String doChatWithRag(String message, String chatId) {
        // 限制输入消息长度，防止超出DashScope API限制(129024个字符)
        message = ensureInputLength(message);
        
        // 查询重写
        String rewritenMessage = queryRewriter.doQueryRewrite(message);

        ChatResponse chatResponse = chatClient
                .prompt()
                // 使用改写后的查询
                .user(rewritenMessage)
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                // 开启日志，便于观察效果
                .advisors(new MyLoggerAdvisor())
                // 应用 RAG 知识库问答
                .advisors(new QuestionAnswerAdvisor(loveAppVectorStore))
                // 应用 RAG 检索增强服务 （基于云知识库服务）
//                .advisors(loveAppRagCloudAdvisor)
                // 应用 RAG 检索增强服务 （基于 Pgvector 知识库服务）  使用阿里云 Pgvector 服务时要开启弹性收费模式 不然会按小时计费一直扣钱
//                .advisors( new QuestionAnswerAdvisor(pgVectorVectorStore))
                // 自定义 RAG 检索增强服务（文档查询器 + 上下文增强器）
//                .advisors(
//                        LoveAppRagCustomAdvisorFactory.createLoveAppRagCustomAdvisor(pgVectorVectorStore, "单身")
//                )
                .call()
                .chatResponse();
        String content = chatResponse.getResult().getOutput().getText();
        log.info("content: {}", content);
        return content;
    }


    // AI 库调用工具
    @Resource
    private ToolCallback[] allTools;

    /**
     * AI 恋爱报告功能（支持工具调用 ）
     *
     * @param message
     * @param chatId
     * @return
     */
    public String doChatWithTools(String message, String chatId) {
        // 限制输入消息长度，防止超出DashScope API限制(129024个字符)
        message = ensureInputLength(message);
        
        ChatResponse response = chatClient
                .prompt()
                .user(message)
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                // 开启日志，便于观察效果
                .advisors(new MyLoggerAdvisor())
                .tools(allTools)
                .call()
                .chatResponse();
        String content = response.getResult().getOutput().getText();
        log.info("content: {}", content);
        return content;
    }


}
