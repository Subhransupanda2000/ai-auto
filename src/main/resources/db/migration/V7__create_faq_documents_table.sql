-- Knowledge base for Retrieval Augmented Generation (RAG).
-- Embedding dimension (1536) matches OpenAI's text-embedding-3-small model
-- (see app.openai.embedding-dimensions). If you change the embedding model
-- to one with a different dimensionality, add a new migration to alter this
-- column accordingly.
CREATE TABLE faq_documents (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    category      VARCHAR(32)  NOT NULL
                     CHECK (category IN ('FAQ', 'DOCTOR_PROFILE', 'CLINIC_TIMINGS', 'INSURANCE', 'MEDICAL_SERVICE')),
    title         VARCHAR(255) NOT NULL,
    content       TEXT         NOT NULL,
    embedding     VECTOR(1536),
    source        VARCHAR(255),
    active        BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at    TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE INDEX idx_faq_documents_category ON faq_documents (category);
CREATE INDEX idx_faq_documents_active ON faq_documents (active);

-- Approximate nearest neighbour index for cosine similarity search.
CREATE INDEX idx_faq_documents_embedding_hnsw
    ON faq_documents USING hnsw (embedding vector_cosine_ops);
