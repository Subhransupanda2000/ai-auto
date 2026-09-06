/**
 * The backend persists `Conversation`/message history server-side for the AI
 * receptionist, but exposes no endpoint to list/retrieve past sessions.
 * This service keeps a local mirror of conversations (per browser) so the
 * Chat page can show a conversation history sidebar; the actual AI replies
 * always come from the real POST /api/chat endpoint.
 */
import { generateId } from '../utils/id';
import type { ChatConversation, ChatMessage } from '../types/chat';

const storageKey = 'hc_chat_conversations';

function readAll(): ChatConversation[] {
  const raw = localStorage.getItem(storageKey);
  if (!raw) return [];
  try {
    return JSON.parse(raw) as ChatConversation[];
  } catch {
    return [];
  }
}

function writeAll(conversations: ChatConversation[]): void {
  localStorage.setItem(storageKey, JSON.stringify(conversations));
}

export const chatHistoryService = {
  list(): ChatConversation[] {
    return readAll().sort((a, b) => b.updatedAt.localeCompare(a.updatedAt));
  },

  get(id: string): ChatConversation | undefined {
    return readAll().find((c) => c.id === id);
  },

  create(): ChatConversation {
    const now = new Date().toISOString();
    const conversation: ChatConversation = {
      id: generateId(),
      sessionId: generateId(),
      title: 'New conversation',
      createdAt: now,
      updatedAt: now,
      messages: [],
    };
    writeAll([conversation, ...readAll()]);
    return conversation;
  },

  appendMessage(conversationId: string, message: ChatMessage): void {
    const conversations = readAll();
    const updated = conversations.map((c) => {
      if (c.id !== conversationId) return c;
      const messages = [...c.messages, message];
      const title =
        c.title === 'New conversation' && message.role === 'user'
          ? message.content.slice(0, 48)
          : c.title;
      return { ...c, messages, title, updatedAt: new Date().toISOString() };
    });
    writeAll(updated);
  },

  updateMessage(conversationId: string, messageId: string, patch: Partial<ChatMessage>): void {
    const conversations = readAll();
    const updated = conversations.map((c) => {
      if (c.id !== conversationId) return c;
      return {
        ...c,
        messages: c.messages.map((m) => (m.id === messageId ? { ...m, ...patch } : m)),
        updatedAt: new Date().toISOString(),
      };
    });
    writeAll(updated);
  },

  remove(id: string): void {
    writeAll(readAll().filter((c) => c.id !== id));
  },
};
