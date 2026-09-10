package com.healthcareai.ai;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.healthcareai.entity.FaqCategory;
import com.healthcareai.entity.FaqDocument;
import com.healthcareai.exception.ResourceNotFoundException;
import com.healthcareai.integration.GeminiClient;
import com.healthcareai.repository.FaqDocumentRepository;
import com.healthcareai.tenant.TenantContext;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class KnowledgeBaseServiceImpl implements KnowledgeBaseService {

    private final FaqDocumentRepository faqDocumentRepository;
    private final GeminiClient geminiClient;

    @Override
    @Transactional
    public FaqDocument upsertDocument(UUID id, FaqCategory category, String title, String content, String source) {
        UUID tenantId = TenantContext.getCurrentTenantId();
        FaqDocument document = (id != null)
                ? faqDocumentRepository.findByIdAndTenantId(id, tenantId).orElseThrow(() -> ResourceNotFoundException.of("FaqDocument", id))
                : FaqDocument.builder().tenantId(tenantId).build();

        document.setCategory(category);
        document.setTitle(title);
        document.setContent(content);
        document.setSource(source);
        document.setActive(true);
        document.setEmbedding(geminiClient.createEmbedding(title + "\n" + content));

        FaqDocument saved = faqDocumentRepository.save(document);
        log.info("Upserted knowledge base document {} ({})", saved.getId(), saved.getTitle());
        return saved;
    }

    @Override
    @Transactional
    public void deactivateDocument(UUID id) {
        FaqDocument document = faqDocumentRepository.findByIdAndTenantId(id, TenantContext.getCurrentTenantId())
                .orElseThrow(() -> ResourceNotFoundException.of("FaqDocument", id));
        document.setActive(false);
        faqDocumentRepository.save(document);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FaqDocument> retrieveRelevant(String query, int maxResults) {
        float[] queryEmbedding = geminiClient.createEmbedding(query);
        return faqDocumentRepository.findMostSimilar(TenantContext.getCurrentTenantId(), queryEmbedding, PageRequest.of(0, maxResults));
    }

    @Override
    @Transactional(readOnly = true)
    public List<FaqDocument> retrieveRelevant(String query, FaqCategory category, int maxResults) {
        float[] queryEmbedding = geminiClient.createEmbedding(query);
        return faqDocumentRepository.findMostSimilarByCategory(
                TenantContext.getCurrentTenantId(), queryEmbedding, category, PageRequest.of(0, maxResults));
    }
}
