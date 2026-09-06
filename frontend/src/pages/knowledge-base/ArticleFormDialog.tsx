import { useEffect } from 'react';
import { zodResolver } from '@hookform/resolvers/zod';
import { Controller, useForm } from 'react-hook-form';
import { z } from 'zod';
import { Button, Dialog, DialogActions, DialogContent, DialogTitle, MenuItem, TextField } from '@mui/material';
import Grid from '@mui/material/Grid2';
import type { FaqArticle, FaqArticleRequest, FaqCategory } from '../../types/knowledge-base';

const categories: { value: FaqCategory; label: string }[] = [
  { value: 'CLINIC_INFO', label: 'Clinic Information' },
  { value: 'INSURANCE', label: 'Insurance' },
  { value: 'SERVICES', label: 'Services' },
  { value: 'BILLING', label: 'Billing' },
  { value: 'GENERAL', label: 'General' },
];

const articleSchema = z.object({
  category: z.enum(['CLINIC_INFO', 'INSURANCE', 'SERVICES', 'BILLING', 'GENERAL']),
  question: z.string().min(1, 'Question is required'),
  answer: z.string().min(1, 'Answer is required'),
  tags: z.string().optional(),
});

type ArticleFormValues = z.infer<typeof articleSchema>;

interface ArticleFormDialogProps {
  open: boolean;
  article?: FaqArticle | null;
  loading?: boolean;
  onClose: () => void;
  onSubmit: (values: FaqArticleRequest) => void;
}

export function ArticleFormDialog({ open, article, loading, onClose, onSubmit }: ArticleFormDialogProps) {
  const {
    control,
    handleSubmit,
    reset,
    formState: { errors },
  } = useForm<ArticleFormValues>({
    resolver: zodResolver(articleSchema),
    defaultValues: { category: 'GENERAL', question: '', answer: '', tags: '' },
  });

  useEffect(() => {
    if (open) {
      reset({
        category: article?.category ?? 'GENERAL',
        question: article?.question ?? '',
        answer: article?.answer ?? '',
        tags: article?.tags.join(', ') ?? '',
      });
    }
  }, [open, article, reset]);

  const submit = handleSubmit((values) => {
    onSubmit({
      category: values.category,
      question: values.question,
      answer: values.answer,
      tags: values.tags
        ? values.tags.split(',').map((t) => t.trim()).filter(Boolean)
        : [],
    });
  });

  return (
    <Dialog open={open} onClose={onClose} maxWidth="sm" fullWidth>
      <DialogTitle>{article ? 'Edit Article' : 'New Knowledge Base Article'}</DialogTitle>
      <DialogContent>
        <Grid container spacing={2} sx={{ mt: 0.5 }}>
          <Grid size={12}>
            <Controller
              name="category"
              control={control}
              render={({ field }) => (
                <TextField {...field} select label="Category" fullWidth>
                  {categories.map((c) => (
                    <MenuItem key={c.value} value={c.value}>
                      {c.label}
                    </MenuItem>
                  ))}
                </TextField>
              )}
            />
          </Grid>
          <Grid size={12}>
            <Controller
              name="question"
              control={control}
              render={({ field }) => (
                <TextField {...field} label="Question" fullWidth error={Boolean(errors.question)} helperText={errors.question?.message} />
              )}
            />
          </Grid>
          <Grid size={12}>
            <Controller
              name="answer"
              control={control}
              render={({ field }) => (
                <TextField
                  {...field}
                  label="Answer"
                  fullWidth
                  multiline
                  minRows={4}
                  error={Boolean(errors.answer)}
                  helperText={errors.answer?.message}
                />
              )}
            />
          </Grid>
          <Grid size={12}>
            <Controller
              name="tags"
              control={control}
              render={({ field }) => <TextField {...field} label="Tags (comma separated)" fullWidth />}
            />
          </Grid>
        </Grid>
      </DialogContent>
      <DialogActions sx={{ px: 3, pb: 2 }}>
        <Button onClick={onClose} color="inherit">
          Cancel
        </Button>
        <Button onClick={submit} variant="contained" loading={loading}>
          {article ? 'Save changes' : 'Create article'}
        </Button>
      </DialogActions>
    </Dialog>
  );
}
