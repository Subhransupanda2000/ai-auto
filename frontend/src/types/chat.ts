export type Channel = 'WEB' | 'WHATSAPP' | 'SMS' | 'EMAIL';

export interface ChatRequest {
  sessionId: string;
  message: string;
  channel?: Channel;
  phoneNumber?: string;
}

export interface ChatResponse {
  sessionId: string;
  reply: string;
}

export type ChatMessageRole = 'user' | 'assistant';

export interface ChatMessage {
  id: string;
  role: ChatMessageRole;
  content: string;
  timestamp: string;
  pending?: boolean;
  error?: boolean;
}

export interface ChatConversation {
  id: string;
  sessionId: string;
  title: string;
  createdAt: string;
  updatedAt: string;
  messages: ChatMessage[];
}
