import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { queryKeys } from '../api/queryKeys';
import { knowledgeBaseService } from '../services/knowledgeBaseService';
import type { FaqArticleRequest } from '../types/knowledge-base';

export function useKnowledgeBase() {
  return useQuery({
    queryKey: queryKeys.knowledgeBase,
    queryFn: async () => knowledgeBaseService.list(),
  });
}

export function useCreateArticle() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: async (payload: FaqArticleRequest) => knowledgeBaseService.create(payload),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: queryKeys.knowledgeBase }),
  });
}

export function useUpdateArticle() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: async ({ id, payload }: { id: string; payload: Partial<FaqArticleRequest> }) =>
      knowledgeBaseService.update(id, payload),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: queryKeys.knowledgeBase }),
  });
}

export function useDeleteArticle() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: async (id: string) => knowledgeBaseService.remove(id),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: queryKeys.knowledgeBase }),
  });
}
