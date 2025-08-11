package com.runxing.aiagent.controller;

import com.runxing.aiagent.agent.model.RainManus;
import com.runxing.aiagent.app.LoveApp;
import jakarta.annotation.Resource;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;

import java.io.IOException;

@RestController
@RequestMapping("ai")
public class AiController {
    @Resource
    private LoveApp loveApp;

    @GetMapping("/love_app/chat/sync")
    public String doChatWithAppSync(String message, String chatId) {
        return loveApp.doChat(message, chatId);
    }

    @GetMapping(value = "/love_app/chat/sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> doChatWithLoveAppSSE(String message, String chatId) {
        return loveApp.doChatByStream(message, chatId);
    }

    @GetMapping("/love_app/chat/sse/emitter")
    public SseEmitter doChatWithLoveAppSseEmitter(String message, String chatId) {
        // 创建一个超时时间较长的 SseEmitter
        SseEmitter emitter = new SseEmitter(180000L);// 3分钟超时
        // 获取 Flux 数据流并直接订阅
        loveApp.doChatByStream(message, chatId).subscribe(
                // 处理每条消息
                chunk -> {
                    try {
                        emitter.send(chunk);
                    } catch (IOException e) {
                        emitter.completeWithError(e);
                    }
                },
                // 处理错误
                emitter::completeWithError,
                // 完成处理
                emitter::complete
        );
        // 返回emitter
        return emitter;
    }
    @Resource
    private ToolCallback[] allTools;
    @Resource
    private ChatModel dashscopeChatModel;
    @Resource
    private ToolCallbackProvider toolCallbackProvider;
//    @Resource
//    private RainManus rainManus;
    /**
     * 流式调用 Manus 超级智能体
     *
     * @param message
     * @return
     */
    @GetMapping("/manus/chat")
    public SseEmitter doChatWithManus(String message) {
        RainManus rainManus = new RainManus(allTools, dashscopeChatModel,toolCallbackProvider);
        return rainManus.runStream(message);
    }
}
