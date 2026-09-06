import { Avatar, Box, CircularProgress, Paper, Stack, Typography, useTheme } from '@mui/material';
import SmartToyRoundedIcon from '@mui/icons-material/SmartToyRounded';
import ErrorOutlineRoundedIcon from '@mui/icons-material/ErrorOutlineRounded';
import ReactMarkdown from 'react-markdown';
import remarkGfm from 'remark-gfm';
import { Prism as SyntaxHighlighter } from 'react-syntax-highlighter';
import { oneDark, oneLight } from 'react-syntax-highlighter/dist/esm/styles/prism';
import { format } from 'date-fns';
import type { ChatMessage } from '../../types/chat';
import { useThemeStore } from '../../store/themeStore';

interface MessageBubbleProps {
  message: ChatMessage;
  userInitials: string;
}

export function MessageBubble({ message, userInitials }: MessageBubbleProps) {
  const theme = useTheme();
  const mode = useThemeStore((s) => s.mode);
  const isUser = message.role === 'user';

  return (
    <Stack direction={isUser ? 'row-reverse' : 'row'} spacing={1.5} alignItems="flex-start" sx={{ mb: 2.5 }}>
      <Avatar
        sx={{
          width: 32,
          height: 32,
          bgcolor: isUser ? 'secondary.main' : 'primary.main',
          fontSize: 13,
        }}
      >
        {isUser ? userInitials : <SmartToyRoundedIcon fontSize="small" />}
      </Avatar>

      <Box sx={{ maxWidth: '75%', minWidth: 0 }}>
        <Paper
          elevation={0}
          sx={{
            px: 2,
            py: 1.25,
            borderRadius: 3,
            ...(isUser
              ? { bgcolor: 'primary.main', color: 'primary.contrastText', borderTopRightRadius: 4 }
              : {
                  bgcolor: message.error ? 'error.main' : 'background.paper',
                  color: message.error ? 'error.contrastText' : 'text.primary',
                  border: message.error ? 'none' : '1px solid',
                  borderColor: 'divider',
                  borderTopLeftRadius: 4,
                }),
          }}
        >
          {message.pending ? (
            <Stack direction="row" spacing={1} alignItems="center" sx={{ py: 0.5 }}>
              <CircularProgress size={14} thickness={5} color="inherit" />
              <Typography variant="body2" color="text.secondary">
                Typing...
              </Typography>
            </Stack>
          ) : message.error ? (
            <Stack direction="row" spacing={1} alignItems="center">
              <ErrorOutlineRoundedIcon fontSize="small" />
              <Typography variant="body2">{message.content}</Typography>
            </Stack>
          ) : isUser ? (
            <Typography variant="body2" sx={{ whiteSpace: 'pre-wrap' }}>
              {message.content}
            </Typography>
          ) : (
            <Box
              sx={{
                fontSize: 14,
                '& p': { m: 0, mb: 1, '&:last-child': { mb: 0 } },
                '& ul, & ol': { mt: 0, mb: 1, pl: 2.5 },
                '& a': { color: 'primary.main' },
              }}
            >
              <ReactMarkdown
                remarkPlugins={[remarkGfm]}
                components={{
                  code(props) {
                    const { children, className, ...rest } = props;
                    const match = /language-(\w+)/.exec(className ?? '');
                    return match ? (
                      <SyntaxHighlighter
                        style={mode === 'dark' ? oneDark : oneLight}
                        language={match[1]}
                        PreTag="div"
                        customStyle={{ borderRadius: 8, fontSize: 12.5, margin: '8px 0' }}
                      >
                        {String(children).replace(/\n$/, '')}
                      </SyntaxHighlighter>
                    ) : (
                      <code
                        {...rest}
                        className={className}
                        style={{
                          background: theme.palette.action.hover,
                          padding: '2px 5px',
                          borderRadius: 4,
                          fontSize: 12.5,
                        }}
                      >
                        {children}
                      </code>
                    );
                  },
                }}
              >
                {message.content}
              </ReactMarkdown>
            </Box>
          )}
        </Paper>
        <Typography
          variant="caption"
          color="text.secondary"
          sx={{ display: 'block', mt: 0.5, textAlign: isUser ? 'right' : 'left' }}
        >
          {format(new Date(message.timestamp), 'h:mm a')}
        </Typography>
      </Box>
    </Stack>
  );
}
