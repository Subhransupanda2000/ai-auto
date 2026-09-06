import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { queryKeys } from '../api/queryKeys';
import { settingsService } from '../services/settingsService';
import type { AppSettings } from '../types/settings';

export function useSettings() {
  return useQuery({
    queryKey: queryKeys.settings,
    queryFn: async () => settingsService.get(),
  });
}

export function useSaveSettings() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: async (settings: AppSettings) => {
      settingsService.save(settings);
      return settings;
    },
    onSuccess: () => queryClient.invalidateQueries({ queryKey: queryKeys.settings }),
  });
}
