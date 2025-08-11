package com.runxing.aiagent.tools;


import org.springframework.ai.tool.ToolCallback;

import org.springframework.ai.tool.ToolCallbacks;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

;

@Configuration
public class ToolRegistration {


    @Bean
    public ToolCallback[] allTools() {
        TerminateTool terminateTool = new TerminateTool();
        return ToolCallbacks.from(terminateTool);
//        // ① MCP 远端工具
//        FunctionCallback[] remote = toolCallbackProvider.getToolCallbacks();
//
//        // ② 本地 TerminateTool → MethodToolCallback
//        FunctionCallback[] local = MethodToolCallbackProvider.builder()
//                .toolObjects(terminateTool)
//                .build()
//                .getToolCallbacks();              // 返回值就是 FunctionCallback[]
//
//        // ③ 合并两组回调
//        return Stream.concat(
//                Arrays.stream(remote),
//                Arrays.stream(local)
//        ).toArray(FunctionCallback[]::new);

    }

}
