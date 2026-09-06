-- Required for gen_random_uuid()
CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- Required for storing/searching embedding vectors (RAG knowledge base)
CREATE EXTENSION IF NOT EXISTS vector;
