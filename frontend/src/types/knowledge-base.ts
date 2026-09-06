export type FaqCategory =
  | 'CLINIC_INFO'
  | 'INSURANCE'
  | 'SERVICES'
  | 'BILLING'
  | 'GENERAL';

export interface FaqArticle {
  id: string;
  category: FaqCategory;
  question: string;
  answer: string;
  tags: string[];
  createdAt: string;
  updatedAt: string;
}

export interface FaqArticleRequest {
  category: FaqCategory;
  question: string;
  answer: string;
  tags?: string[];
}
