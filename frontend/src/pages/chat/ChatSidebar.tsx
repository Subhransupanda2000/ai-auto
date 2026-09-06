import {
  Box,
  Button,
  Divider,
  IconButton,
  List,
  ListItemButton,
  ListItemText,
  Typography,
} from '@mui/material';
import AddCommentRoundedIcon from '@mui/icons-material/AddCommentRounded';
import DeleteOutlineRoundedIcon from '@mui/icons-material/DeleteOutlineRounded';
import { formatDistanceToNow } from 'date-fns';
import { EmptyState } from '../../components/common/EmptyState';
import type { ChatConversation } from '../../types/chat';

interface ChatSidebarProps {
  conversations: ChatConversation[];
  activeId?: string;
  onSelect: (id: string) => void;
  onNew: () => void;
  onDelete: (id: string) => void;
}

export function ChatSidebar({ conversations, activeId, onSelect, onNew, onDelete }: ChatSidebarProps) {
  return (
    <Box sx={{ display: 'flex', flexDirection: 'column', height: '100%' }}>
      <Box sx={{ p: 2 }}>
        <Button fullWidth variant="outlined" startIcon={<AddCommentRoundedIcon />} onClick={onNew}>
          New conversation
        </Button>
      </Box>
      <Divider />
      <Box sx={{ flexGrow: 1, overflowY: 'auto' }}>
        {conversations.length === 0 ? (
          <EmptyState title="No conversations yet" />
        ) : (
          <List disablePadding>
            {conversations.map((conversation) => (
              <ListItemButton
                key={conversation.id}
                selected={conversation.id === activeId}
                onClick={() => onSelect(conversation.id)}
                sx={{ px: 2, py: 1.25, alignItems: 'flex-start' }}
              >
                <ListItemText
                  primary={
                    <Typography variant="body2" fontWeight={600} noWrap>
                      {conversation.title}
                    </Typography>
                  }
                  secondary={
                    <Typography variant="caption" color="text.secondary">
                      {formatDistanceToNow(new Date(conversation.updatedAt), { addSuffix: true })}
                    </Typography>
                  }
                />
                <IconButton
                  size="small"
                  edge="end"
                  onClick={(e) => {
                    e.stopPropagation();
                    onDelete(conversation.id);
                  }}
                >
                  <DeleteOutlineRoundedIcon fontSize="small" />
                </IconButton>
              </ListItemButton>
            ))}
          </List>
        )}
      </Box>
    </Box>
  );
}
