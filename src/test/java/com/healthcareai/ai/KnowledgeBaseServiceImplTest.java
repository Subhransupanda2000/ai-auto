package com.healthcareai.ai;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.healthcareai.entity.FaqCategory;
import com.healthcareai.entity.FaqDocument;
import com.healthcareai.exception.ResourceNotFoundException;
import com.healthcareai.integration.GeminiClient;
import com.healthcareai.repository.FaqDocumentRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class KnowledgeBaseServiceImplTest {

    @Mock
    private FaqDocumentRepository faqDocumentRepository;
    @Mock
    private GeminiClient geminiClient;

    private KnowledgeBaseServiceImpl knowledgeBaseService;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        knowledgeBaseService = new KnowledgeBaseServiceImpl(faqDocumentRepository, geminiClient);
    }

    @Test
    void upsertDocument_computesEmbeddingAndSaves() {
        float[] embedding = new float[]{0.1f, 0.2f, 0.3f};
        when(geminiClient.createEmbedding(anyString())).thenReturn(embedding);
        when(faqDocumentRepository.save(any(FaqDocument.class))).thenAnswer(invocation -> invocation.getArgument(0));

        FaqDocument result = knowledgeBaseService.upsertDocument(
                null, FaqCategory.FAQ, "Opening hours", "We are open 9-5 Mon-Fri.", "manual");

        assertThat(result.getEmbedding()).isEqualTo(embedding);
        assertThat(result.getCategory()).isEqualTo(FaqCategory.FAQ);
        assertThat(result.isActive()).isTrue();
        verify(faqDocumentRepository).save(any(FaqDocument.class));
    }

    @Test
    void upsertDocument_throwsNotFound_whenIdDoesNotExist() {
        UUID id = UUID.randomUUID();
        when(faqDocumentRepository.findByIdAndTenantId(eq(id), nullable(UUID.class))).thenReturn(Optional.empty());

        assertThatThrownBy(() -> knowledgeBaseService.upsertDocument(id, FaqCategory.FAQ, "t", "c", "s"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void retrieveRelevant_delegatesToRepositoryWithComputedEmbedding() {
        float[] embedding = new float[]{0.5f};
        when(geminiClient.createEmbedding("clinic hours")).thenReturn(embedding);
        FaqDocument doc = FaqDocument.builder().category(FaqCategory.CLINIC_TIMINGS).title("Hours").content("9-5").build();
        when(faqDocumentRepository.findMostSimilar(nullable(UUID.class), eq(embedding), any())).thenReturn(List.of(doc));

        List<FaqDocument> results = knowledgeBaseService.retrieveRelevant("clinic hours", 3);

        assertThat(results).containsExactly(doc);
    }
}
