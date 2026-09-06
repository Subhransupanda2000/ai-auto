import { useMemo, useState } from 'react';
import { useSnackbar } from 'notistack';
import {
  Box,
  Button,
  Card,
  CardContent,
  Chip,
  IconButton,
  InputAdornment,
  Stack,
  TextField,
  Typography,
} from '@mui/material';
import Grid from '@mui/material/Grid2';
import AddRoundedIcon from '@mui/icons-material/AddRounded';
import SearchRoundedIcon from '@mui/icons-material/SearchRounded';
import EditRoundedIcon from '@mui/icons-material/EditRounded';
import DeleteRoundedIcon from '@mui/icons-material/DeleteRounded';
import { PageHeader } from '../../components/common/PageHeader';
import { EmptyState } from '../../components/common/EmptyState';
import { LoadingState } from '../../components/common/LoadingState';
import { ConfirmDialog } from '../../components/common/ConfirmDialog';
import { ArticleFormDialog } from './ArticleFormDialog';
import {
  useCreateArticle,
  useDeleteArticle,
  useKnowledgeBase,
  useUpdateArticle,
} from '../../hooks/useKnowledgeBase';
import type { FaqArticle, FaqArticleRequest, FaqCategory } from '../../types/knowledge-base';

const categoryLabels: Record<FaqCategory, string> = {
  CLINIC_INFO: 'Clinic Information',
  INSURANCE: 'Insurance',
  SERVICES: 'Services',
  BILLING: 'Billing',
  GENERAL: 'General',
};

export function KnowledgeBasePage() {
  const { enqueueSnackbar } = useSnackbar();
  const { data: articles = [], isLoading } = useKnowledgeBase();
  const createArticle = useCreateArticle();
  const updateArticle = useUpdateArticle();
  const deleteArticle = useDeleteArticle();

  const [search, setSearch] = useState('');
  const [categoryFilter, setCategoryFilter] = useState<FaqCategory | 'ALL'>('ALL');
  const [formOpen, setFormOpen] = useState(false);
  const [editingArticle, setEditingArticle] = useState<FaqArticle | null>(null);
  const [deleteTarget, setDeleteTarget] = useState<FaqArticle | null>(null);

  const filtered = useMemo(() => {
    const q = search.trim().toLowerCase();
    return articles.filter((a) => {
      const matchesCategory = categoryFilter === 'ALL' || a.category === categoryFilter;
      const matchesSearch =
        !q ||
        a.question.toLowerCase().includes(q) ||
        a.answer.toLowerCase().includes(q) ||
        a.tags.some((t) => t.toLowerCase().includes(q));
      return matchesCategory && matchesSearch;
    });
  }, [articles, search, categoryFilter]);

  const handleCreate = (values: FaqArticleRequest) => {
    createArticle.mutate(values, {
      onSuccess: () => {
        enqueueSnackbar('Article created', { variant: 'success' });
        setFormOpen(false);
      },
    });
  };

  const handleUpdate = (values: FaqArticleRequest) => {
    if (!editingArticle) return;
    updateArticle.mutate(
      { id: editingArticle.id, payload: values },
      {
        onSuccess: () => {
          enqueueSnackbar('Article updated', { variant: 'success' });
          setFormOpen(false);
          setEditingArticle(null);
        },
      },
    );
  };

  const handleDelete = () => {
    if (!deleteTarget) return;
    deleteArticle.mutate(deleteTarget.id, {
      onSuccess: () => {
        enqueueSnackbar('Article deleted', { variant: 'success' });
        setDeleteTarget(null);
      },
    });
  };

  return (
    <Box>
      <PageHeader
        title="Knowledge Base"
        subtitle="Manage FAQs the AI receptionist uses to answer patient questions."
        actions={
          <Button
            variant="contained"
            startIcon={<AddRoundedIcon />}
            onClick={() => {
              setEditingArticle(null);
              setFormOpen(true);
            }}
          >
            Upload FAQ
          </Button>
        }
      />

      <Stack direction={{ xs: 'column', sm: 'row' }} spacing={1.5} sx={{ mb: 2.5 }}>
        <TextField
          placeholder="Search articles..."
          size="small"
          value={search}
          onChange={(e) => setSearch(e.target.value)}
          sx={{ maxWidth: 420 }}
          fullWidth
          slotProps={{
            input: {
              startAdornment: (
                <InputAdornment position="start">
                  <SearchRoundedIcon fontSize="small" color="action" />
                </InputAdornment>
              ),
            },
          }}
        />
        <Stack direction="row" spacing={1} sx={{ flexWrap: 'wrap', gap: 1 }}>
          <Chip
            label="All"
            color={categoryFilter === 'ALL' ? 'primary' : 'default'}
            variant={categoryFilter === 'ALL' ? 'filled' : 'outlined'}
            onClick={() => setCategoryFilter('ALL')}
          />
          {(Object.keys(categoryLabels) as FaqCategory[]).map((cat) => (
            <Chip
              key={cat}
              label={categoryLabels[cat]}
              color={categoryFilter === cat ? 'primary' : 'default'}
              variant={categoryFilter === cat ? 'filled' : 'outlined'}
              onClick={() => setCategoryFilter(cat)}
            />
          ))}
        </Stack>
      </Stack>

      {isLoading ? (
        <LoadingState label="Loading knowledge base..." />
      ) : filtered.length === 0 ? (
        <EmptyState title="No articles found" description="Try a different search or add a new article." />
      ) : (
        <Grid container spacing={2.5}>
          {filtered.map((article) => (
            <Grid key={article.id} size={{ xs: 12, md: 6 }}>
              <Card sx={{ height: '100%' }}>
                <CardContent>
                  <Stack direction="row" justifyContent="space-between" alignItems="flex-start">
                    <Chip size="small" label={categoryLabels[article.category]} color="primary" variant="outlined" />
                    <Stack direction="row">
                      <IconButton
                        size="small"
                        onClick={() => {
                          setEditingArticle(article);
                          setFormOpen(true);
                        }}
                      >
                        <EditRoundedIcon fontSize="small" />
                      </IconButton>
                      <IconButton size="small" onClick={() => setDeleteTarget(article)}>
                        <DeleteRoundedIcon fontSize="small" />
                      </IconButton>
                    </Stack>
                  </Stack>
                  <Typography variant="subtitle1" sx={{ mt: 1.5 }}>
                    {article.question}
                  </Typography>
                  <Typography variant="body2" color="text.secondary" sx={{ mt: 1 }}>
                    {article.answer}
                  </Typography>
                  {article.tags.length > 0 && (
                    <Stack direction="row" spacing={0.75} sx={{ mt: 1.5, flexWrap: 'wrap', gap: 0.75 }}>
                      {article.tags.map((tag) => (
                        <Chip key={tag} label={tag} size="small" variant="outlined" />
                      ))}
                    </Stack>
                  )}
                </CardContent>
              </Card>
            </Grid>
          ))}
        </Grid>
      )}

      <ArticleFormDialog
        open={formOpen}
        article={editingArticle}
        loading={createArticle.isPending || updateArticle.isPending}
        onClose={() => {
          setFormOpen(false);
          setEditingArticle(null);
        }}
        onSubmit={editingArticle ? handleUpdate : handleCreate}
      />

      <ConfirmDialog
        open={Boolean(deleteTarget)}
        title="Delete article"
        message={`Are you sure you want to delete "${deleteTarget?.question}"?`}
        confirmLabel="Delete"
        destructive
        loading={deleteArticle.isPending}
        onConfirm={handleDelete}
        onClose={() => setDeleteTarget(null)}
      />
    </Box>
  );
}
