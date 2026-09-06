import type { ReactNode } from 'react';
import { Avatar, Box, Card, CardContent, Stack, Typography } from '@mui/material';
import TrendingUpRoundedIcon from '@mui/icons-material/TrendingUpRounded';
import TrendingDownRoundedIcon from '@mui/icons-material/TrendingDownRounded';

interface StatCardProps {
  label: string;
  value: string | number;
  icon: ReactNode;
  color?: 'primary' | 'secondary' | 'success' | 'warning' | 'error' | 'info';
  trendValue?: number;
  helperText?: string;
}

export function StatCard({ label, value, icon, color = 'primary', trendValue, helperText }: StatCardProps) {
  const isPositive = (trendValue ?? 0) >= 0;

  return (
    <Card sx={{ height: '100%' }}>
      <CardContent>
        <Stack direction="row" alignItems="flex-start" justifyContent="space-between">
          <Box>
            <Typography variant="body2" color="text.secondary" fontWeight={600}>
              {label}
            </Typography>
            <Typography variant="h4" sx={{ mt: 0.5 }}>
              {value}
            </Typography>
          </Box>
          <Avatar
            variant="rounded"
            sx={{
              bgcolor: (theme) => `${theme.palette[color].main}1F`,
              color: `${color}.main`,
              width: 46,
              height: 46,
              borderRadius: 2.5,
            }}
          >
            {icon}
          </Avatar>
        </Stack>

        {(trendValue !== undefined || helperText) && (
          <Stack direction="row" spacing={0.75} alignItems="center" sx={{ mt: 1.5 }}>
            {trendValue !== undefined && (
              <>
                {isPositive ? (
                  <TrendingUpRoundedIcon fontSize="small" color="success" />
                ) : (
                  <TrendingDownRoundedIcon fontSize="small" color="error" />
                )}
                <Typography variant="caption" color={isPositive ? 'success.main' : 'error.main'} fontWeight={700}>
                  {Math.abs(trendValue)}%
                </Typography>
              </>
            )}
            {helperText && (
              <Typography variant="caption" color="text.secondary">
                {helperText}
              </Typography>
            )}
          </Stack>
        )}
      </CardContent>
    </Card>
  );
}
