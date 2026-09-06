package com.healthcareai.ai;

import java.util.List;
import java.util.UUID;

import com.healthcareai.entity.FaqCategory;
import com.healthcareai.entity.FaqDocument;

/**
 * Retrieval Augmented Generation (RAG) over the clinic knowledge base
 * (FAQs, doctor profiles, clinic timings, insurance information, and
 * medical services), backed by pgvector similarity search.
 */
public interface KnowledgeBaseService {

    /**
     * Adds or updates a knowledge base document and (re)computes its
     * embedding via the configured embedding model.
     */
    FaqDocument upsertDocument(UUID id, FaqCategory category, String title, String content, String source);

    void deactivateDocument(UUID id);

    /**
     * Returns the top {@code maxResults} most relevant active documents for
     * the given natural-language query, across all categories.
     */
    List<FaqDocument> retrieveRelevant(String query, int maxResults);

    /**
     * Returns the top {@code maxResults} most relevant active documents for
     * the given query, restricted to a single category.
     */
    List<FaqDocument> retrieveRelevant(String query, FaqCategory category, int maxResults);
}
