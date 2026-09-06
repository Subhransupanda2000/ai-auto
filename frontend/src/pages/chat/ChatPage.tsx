import { useEffect, useRef, useState, type KeyboardEvent } from 'react';
import {
  Avatar,
  Box,
  Card,
  Divider,
  IconButton,
  Paper,
  Stack,
  TextField,
  Typography,
} from '@mui/material';
import SendRoundedIcon from '@mui/icons-material/SendRounded';
import SmartToyRoundedIcon from '@mui/icons-material/SmartToyRounded';
import { PageHeader } from '../../components/common/PageHeader';
import { EmptyState } from '../../components/common/EmptyState';
import { MessageBubble } from './MessageBubble';
import { ChatSidebar } from './ChatSidebar';
import { useChat } from '../../hooks/useChat';
import { useAuthStore } from '../../store/authStore';

export function ChatPage() {
  const { conversations, active, isSending, newConversation, selectConversation, deleteConversation, sendMessage } =
    useChat();
  const user = useAuthStore((s) => s.user);
  const [input, setInput] = useState('');
  const scrollRef = useRef<HTMLDivElement | null>(null);

  useEffect(() => {
    scrollRef.current?.scrollTo({ top: scrollRef.current.scrollHeight, behavior: 'smooth' });
  }, [active?.messages.length]);

  const userInitials = (user?.email ?? '?').slice(0, 2).toUpperCase();

  const handleSend = () => {
    if (!input.trim() || isSending) return;
    const content = input;
    setInput('');
    sendMessage(content);
  };

  const handleKeyDown = (e: KeyboardEvent<HTMLDivElement>) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault();
      handleSend();
    }
  };

  return (
    <Box sx={{ display: 'flex', flexDirection: 'column', height: 'calc(100vh - 128px)' }}>
      <PageHeader title="AI Chat" subtitle="Chat with the AI receptionist just like a patient would." />

      <Card sx={{ flexGrow: 1, display: 'flex', overflow: 'hidden', minHeight: 0 }}>
        <Box sx={{ width: 300, display: { xs: 'none', md: 'block' }, borderRight: '1px solid', borderColor: 'divider' }}>
          <ChatSidebar
            conversations={conversations}
            activeId={active?.id}
            onSelect={selectConversation}
            onNew={newConversation}
            onDelete={deleteConversation}
          />
        </Box>

        <Box sx={{ flexGrow: 1, display: 'flex', flexDirection: 'column', minWidth: 0 }}>
          <Stack direction="row" spacing={1.5} alignItems="center" sx={{ px: 2.5, py: 1.5, borderBottom: '1px solid', borderColor: 'divider' }}>
            <Avatar sx={{ bgcolor: 'primary.main', width: 32, height: 32 }}>
              <SmartToyRoundedIcon fontSize="small" />
            </Avatar>
            <Box>
              <Typography variant="subtitle2">AI Receptionist</Typography>
              <Typography variant="caption" color="text.secondary">
                Connected to /api/chat
              </Typography>
            </Box>
          </Stack>

          <Box ref={scrollRef} sx={{ flexGrow: 1, overflowY: 'auto', px: 2.5, py: 2 }}>
            {!active || active.messages.length === 0 ? (
              <EmptyState
                title="Start a conversation"
                description="Ask about appointments, insurance, clinic hours, or anything else a patient might ask the front desk."
              />
            ) : (
              active.messages.map((message) => (
                <MessageBubble key={message.id} message={message} userInitials={userInitials} />
              ))
            )}
          </Box>

          <Divider />
          <Box sx={{ p: 2 }}>
            <Paper
              variant="outlined"
              sx={{ display: 'flex', alignItems: 'flex-end', gap: 1, p: 1, borderRadius: 3 }}
            >
              <TextField
                placeholder="Type a message..."
                variant="standard"
                fullWidth
                multiline
                maxRows={5}
                value={input}
                onChange={(e) => setInput(e.target.value)}
                onKeyDown={handleKeyDown}
                slotProps={{ input: { disableUnderline: true } }}
                sx={{ px: 1 }}
              />
              <IconButton color="primary" onClick={handleSend} disabled={!input.trim() || isSending}>
                <SendRoundedIcon />
              </IconButton>
            </Paper>
            <Typography variant="caption" color="text.secondary" sx={{ display: 'block', mt: 0.75, ml: 1 }}>
              Press Enter to send, Shift+Enter for a new line.
            </Typography>
          </Box>
        </Box>
      </Card>
    </Box>
  );
}
