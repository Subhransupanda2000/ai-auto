import { useCallback, useEffect, useState } from 'react';
import { chatApi } from '../api/chatApi';
import { chatHistoryService } from '../services/chatHistoryService';
import { generateId } from '../utils/id';
import type { ChatConversation, ChatMessage } from '../types/chat';

export function useChat() {
  const [conversations, setConversations] = useState<ChatConversation[]>([]);
  const [activeId, setActiveId] = useState<string | null>(null);
  const [isSending, setIsSending] = useState(false);

  const refresh = useCallback(() => {
    const list = chatHistoryService.list();
    setConversations(list);
    return list;
  }, []);

  useEffect(() => {
    const list = refresh();
    if (list.length > 0) {
      setActiveId(list[0].id);
    } else {
      const created = chatHistoryService.create();
      setActiveId(created.id);
      refresh();
    }
  }, [refresh]);

  const active = conversations.find((c) => c.id === activeId) ?? null;

  const newConversation = useCallback(() => {
    const created = chatHistoryService.create();
    refresh();
    setActiveId(created.id);
  }, [refresh]);

  const selectConversation = useCallback((id: string) => {
    setActiveId(id);
  }, []);

  const deleteConversation = useCallback(
    (id: string) => {
      chatHistoryService.remove(id);
      const list = refresh();
      if (activeId === id) {
        setActiveId(list[0]?.id ?? null);
      }
    },
    [activeId, refresh],
  );

  const sendMessage = useCallback(
    async (content: string) => {
      if (!active || !content.trim()) return;

      const userMessage: ChatMessage = {
        id: generateId(),
        role: 'user',
        content,
        timestamp: new Date().toISOString(),
      };
      chatHistoryService.appendMessage(active.id, userMessage);

      const pendingId = generateId();
      const pendingMessage: ChatMessage = {
        id: pendingId,
        role: 'assistant',
        content: '',
        timestamp: new Date().toISOString(),
        pending: true,
      };
      chatHistoryService.appendMessage(active.id, pendingMessage);
      refresh();
      setIsSending(true);

      try {
        const response = await chatApi.send({
          sessionId: active.sessionId,
          message: content,
          channel: 'WEB',
        });
        chatHistoryService.updateMessage(active.id, pendingId, {
          content: response.reply,
          pending: false,
        });
      } catch {
        chatHistoryService.updateMessage(active.id, pendingId, {
          content: 'Sorry, the AI receptionist is currently unavailable. Please try again shortly.',
          pending: false,
          error: true,
        });
      } finally {
        setIsSending(false);
        refresh();
      }
    },
    [active, refresh],
  );

  return {
    conversations,
    active,
    isSending,
    newConversation,
    selectConversation,
    deleteConversation,
    sendMessage,
  };
}
