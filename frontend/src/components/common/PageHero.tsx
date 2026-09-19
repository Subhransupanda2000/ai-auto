import type { ReactNode } from 'react';
import { Avatar, Box, Card, CardContent, Stack, Typography } from '@mui/material';

interface PageHeroProps {
  title: string;
  subtitle?: string;
  icon?: ReactNode;
  gradient?: string;
  actions?: ReactNode;
}

const defaultGradient = 'linear-gradient(135deg, #3B6FE0 0%, #2A50A8 55%, #0FB5A7 130%)';

export function PageHero({ title, subtitle, icon, gradient = defaultGradient, actions }: PageHeroProps) {
  return (
    <Card
      elevation={0}
      sx={{
        mb: 2.5,
        position: 'relative',
        overflow: 'hidden',
        background: gradient,
        color: '#FFFFFF',
      }}
    >
      <Box
        sx={{
          position: 'absolute',
          inset: 0,
          pointerEvents: 'none',
          background:
            'radial-gradient(circle at 90% 10%, rgba(255,255,255,0.16) 0%, transparent 45%), radial-gradient(circle at 10% 110%, rgba(255,255,255,0.12) 0%, transparent 45%)',
        }}
      />
      <CardContent sx={{ position: 'relative', p: { xs: 3, md: 4 } }}>
        <Stack
          direction={{ xs: 'column', sm: 'row' }}
          justifyContent="space-between"
          alignItems={{ xs: 'flex-start', sm: 'center' }}
          spacing={2.5}
        >
          <Stack direction="row" spacing={2} alignItems="center">
            {icon && (
              <Avatar
                variant="rounded"
                sx={{
                  width: 52,
                  height: 52,
                  borderRadius: 3,
                  bgcolor: 'rgba(255,255,255,0.16)',
                  color: '#FFFFFF',
                }}
              >
                {icon}
              </Avatar>
            )}
            <Box>
              <Typography variant="h5" sx={{ fontWeight: 700 }}>
                {title}
              </Typography>
              {subtitle && (
                <Typography variant="body2" sx={{ mt: 0.5, opacity: 0.9, maxWidth: 480 }}>
                  {subtitle}
                </Typography>
              )}
            </Box>
          </Stack>
          {actions && <Box>{actions}</Box>}
        </Stack>
      </CardContent>
    </Card>
  );
}
