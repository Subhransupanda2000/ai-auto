import { apiClient } from './client';
import type { ChatRequest, ChatResponse } from '../types/chat';

export const chatApi = {
  send: (payload: ChatRequest) =>
    apiClient.post<ChatResponse>('/chat', payload).then((res) => res.data),
};
