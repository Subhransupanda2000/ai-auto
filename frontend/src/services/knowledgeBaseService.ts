/**
 * The backend does not yet expose a Knowledge Base / FAQ API (the
 * `FaqDocument`/`FaqCategory` JPA entities exist but have no controller).
 * This service persists articles to localStorage so the Knowledge Base page
 * is fully usable today; swap the internals for calls to a future
 * `api/knowledgeBaseApi.ts` once that endpoint ships.
 */
import { generateId } from '../utils/id';
import type { FaqArticle, FaqArticleRequest } from '../types/knowledge-base';

const storageKey = 'hc_knowledge_base';

const seedArticles: FaqArticle[] = [
  {
    id: generateId(),
    category: 'CLINIC_INFO',
    question: 'What are your clinic hours?',
    answer: 'We are open Monday through Friday, 9:00 AM to 5:00 PM, and closed on weekends and public holidays.',
    tags: ['hours', 'schedule'],
    createdAt: new Date().toISOString(),
    updatedAt: new Date().toISOString(),
  },
  {
    id: generateId(),
    category: 'INSURANCE',
    question: 'Which insurance providers do you accept?',
    answer: 'We accept most major insurance providers including Aetna, Cigna, UnitedHealthcare, and Blue Cross Blue Shield. Please contact us to confirm your specific plan.',
    tags: ['insurance', 'billing'],
    createdAt: new Date().toISOString(),
    updatedAt: new Date().toISOString(),
  },
  {
    id: generateId(),
    category: 'SERVICES',
    question: 'Do you offer telehealth appointments?',
    answer: 'Yes, we offer telehealth consultations for a subset of appointment types. Ask our AI receptionist or front desk staff to book a virtual visit.',
    tags: ['telehealth', 'appointments'],
    createdAt: new Date().toISOString(),
    updatedAt: new Date().toISOString(),
  },
];

function readAll(): FaqArticle[] {
  const raw = localStorage.getItem(storageKey);
  if (!raw) {
    localStorage.setItem(storageKey, JSON.stringify(seedArticles));
    return seedArticles;
  }
  try {
    return JSON.parse(raw) as FaqArticle[];
  } catch {
    return [];
  }
}

function writeAll(articles: FaqArticle[]): void {
  localStorage.setItem(storageKey, JSON.stringify(articles));
}

export const knowledgeBaseService = {
  list(): FaqArticle[] {
    return readAll().sort((a, b) => b.updatedAt.localeCompare(a.updatedAt));
  },

  search(query: string): FaqArticle[] {
    const q = query.trim().toLowerCase();
    if (!q) return knowledgeBaseService.list();
    return knowledgeBaseService.list().filter(
      (a) =>
        a.question.toLowerCase().includes(q) ||
        a.answer.toLowerCase().includes(q) ||
        a.tags.some((t) => t.toLowerCase().includes(q)),
    );
  },

  create(payload: FaqArticleRequest): FaqArticle {
    const now = new Date().toISOString();
    const article: FaqArticle = {
      id: generateId(),
      category: payload.category,
      question: payload.question,
      answer: payload.answer,
      tags: payload.tags ?? [],
      createdAt: now,
      updatedAt: now,
    };
    writeAll([article, ...readAll()]);
    return article;
  },

  update(id: string, payload: Partial<FaqArticleRequest>): void {
    writeAll(
      readAll().map((a) => (a.id === id ? { ...a, ...payload, updatedAt: new Date().toISOString() } : a)),
    );
  },

  remove(id: string): void {
    writeAll(readAll().filter((a) => a.id !== id));
  },
};
