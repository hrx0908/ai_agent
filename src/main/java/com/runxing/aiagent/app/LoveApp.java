package com.runxing.aiagent.app;

import com.runxing.aiagent.advisor.MyLoggerAdvisor;
import com.runxing.aiagent.rag.LoveAppRagCustomAdvisorFactory;
import com.runxing.aiagent.rag.QueryRewriter;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Component;

import java.util.List;

import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY;
import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_RETRIEVE_SIZE_KEY;

@Slf4j
@Component
public class LoveApp {
    private final ChatClient chatClient;
    private static final String SYSTEM_PROMPT = "扮演深耕恋爱心理领域的专家。开场向用户表明身份，告知用户可倾诉恋爱难题。" +
            "围绕单身、恋爱、已婚三种状态提问：单身状态询问社交圈拓展及追求心仪对象的困扰；" +
            "恋爱状态询问沟通、习惯差异引发的矛盾；已婚状态询问家庭责任与亲属关系处理的问题。" +
            "引导用户详述事情经过、对方反应及自身想法，以便给出专属解决方案。";

    record LoveReport(String title, List<String> suggestions) {
    }

    public LoveApp(ChatModel dashscopeChatModel) {
        ChatMemory chatMemory = new InMemoryChatMemory();
        chatClient = ChatClient.builder(dashscopeChatModel)
                .defaultSystem(SYSTEM_PROMPT)
                .defaultAdvisors(
                        new MessageChatMemoryAdvisor(chatMemory),
                        // 自定义日志 Advisor，可按需开启
                        new MyLoggerAdvisor()
                )
                .build();
    }

    public String doChat(String message, String chatId) {
        String content = chatClient.prompt()
                .user(message)
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                .call()
                .content();
        log.info("content: {}", content);
        return content;
    }
    public LoveReport doChatWithReport(String message, String chatId) {
        LoveReport loveReport = chatClient.prompt()
                .system(SYSTEM_PROMPT + "每次对话后都要生成恋爱结果，标题为{用户名}的恋爱报告，内容为建议列表")
                .user(message)
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                .call()
                .entity(LoveReport.class);
        log.info("loveReport:{}",loveReport);
        return loveReport;
    }
    @Resource(name = "pgVectorVectorStore")
    private VectorStore pgVectorVectorStore;
    @Resource(name = "LoveAppVectorStore")
    private VectorStore loveAppVectorStore;
    @Resource
    private Advisor loveAppRagCloudAdvisor;
    @Resource
    private QueryRewriter queryRewriter;
    public String doChatWithRAG(String message, String chatId) {
        // 查询重写
        String rewrittenMessage = queryRewriter.doQueryRewrite(message);
        String content = chatClient.prompt()
                //重写用户输入
                .user(rewrittenMessage)
//                .user(message)
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                //开启日志，在上面默认的地方已经开启了，这里不用再开启自定义日志，否则控制台会打印两份日志
//                .advisors(new MyLoggerAdvisor())
                //开启RAG功能，本地知识库
                .advisors(new QuestionAnswerAdvisor(loveAppVectorStore))
               // 开启RAG功能，云知识库
//                .advisors(loveAppRagCloudAdvisor)
                // 开启RAG功能，本地知识库（基于pgVector）
//                .advisors(new QuestionAnswerAdvisor(pgVectorVectorStore))
                // 应用自定义的RAG检索增强服务（文档向量检索查询器+空上下文增强）
//                .advisors(LoveAppRagCustomAdvisorFactory.createLoveAppRagCustomAdvisor(loveAppVectorStore,"已婚"))
                .call()
                .content();
        log.info("content: {}", content);
        return content;
    }
}
