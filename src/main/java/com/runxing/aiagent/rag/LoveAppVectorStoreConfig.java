package com.runxing.aiagent.rag;

import com.alibaba.cloud.ai.dashscope.api.DashScopeApi;

import jakarta.annotation.Resource;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.util.List;

@Configuration
public class LoveAppVectorStoreConfig {
    @Resource
    private LoveAppDocumentLoader loveAppDocumentLoader;
    @Resource
    private MyTokenTextSplitter myTokenTextSplitter;
    @Resource
    private MyKeywordEnricher myKeywordEnricher;

    @Bean
    VectorStore LoveAppVectorStore(EmbeddingModel dashscopEmbeddingModel) {
        SimpleVectorStore vectorStore = SimpleVectorStore.builder(dashscopEmbeddingModel)
                .build();
        // 加载文档
        List<Document> documents = loveAppDocumentLoader.loadMarkdownDocuments();
//        //自主切分
//        documents = myTokenTextSplitter.splitCustomized(documents);
        //自动补充关键词元信息
        List<Document> enrichedDocuments = myKeywordEnricher.enrichDocuments(documents);
        vectorStore.add(enrichedDocuments);
        return vectorStore;

    }
}
