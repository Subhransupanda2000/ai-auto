package com.healthcareai.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.healthcareai.entity.FaqCategory;
import com.healthcareai.entity.FaqDocument;

/**
 * Repository for the RAG knowledge base. Similarity search relies on
 * Hibernate's {@code cosine_distance()} HQL function (from the
 * {@code hibernate-vector} module), which is translated to pgvector's
 * {@code <=>} operator.
 */
public interface FaqDocumentRepository extends JpaRepository<FaqDocument, UUID> {

    List<FaqDocument> findByCategoryAndActiveTrue(FaqCategory category);

    /**
     * Returns the {@code pageable}-limited most similar active documents to
     * the given query embedding, ordered from most to least similar.
     */
    @Query("""
            select f from FaqDocument f
            where f.active = true and f.embedding is not null
            order by cosine_distance(f.embedding, :queryEmbedding) asc
            """)
    List<FaqDocument> findMostSimilar(@Param("queryEmbedding") float[] queryEmbedding, Pageable pageable);

    @Query("""
            select f from FaqDocument f
            where f.active = true and f.embedding is not null and f.category = :category
            order by cosine_distance(f.embedding, :queryEmbedding) asc
            """)
    List<FaqDocument> findMostSimilarByCategory(@Param("queryEmbedding") float[] queryEmbedding,
                                                 @Param("category") FaqCategory category,
                                                 Pageable pageable);
}
