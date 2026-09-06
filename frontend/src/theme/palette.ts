import type { PaletteOptions } from '@mui/material/styles';

export const lightPalette: PaletteOptions = {
  mode: 'light',
  primary: {
    main: '#3B6FE0',
    light: '#6C93EA',
    dark: '#2A50A8',
    contrastText: '#FFFFFF',
  },
  secondary: {
    main: '#0FB5A7',
    light: '#4CCBC0',
    dark: '#0B8A80',
    contrastText: '#FFFFFF',
  },
  success: { main: '#22A06B' },
  warning: { main: '#E6A23C' },
  error: { main: '#E4574C' },
  info: { main: '#3B9FE0' },
  background: {
    default: '#F5F7FB',
    paper: '#FFFFFF',
  },
  text: {
    primary: '#1B2333',
    secondary: '#5B647A',
  },
  divider: '#E4E8F1',
};

export const darkPalette: PaletteOptions = {
  mode: 'dark',
  primary: {
    main: '#6C93EA',
    light: '#8FB0F0',
    dark: '#3B6FE0',
    contrastText: '#0B1220',
  },
  secondary: {
    main: '#4CCBC0',
    light: '#7CDBD2',
    dark: '#0FB5A7',
    contrastText: '#0B1220',
  },
  success: { main: '#3FCB8E' },
  warning: { main: '#F0B958' },
  error: { main: '#F07268' },
  info: { main: '#6FB8EF' },
  background: {
    default: '#0F1420',
    paper: '#161C2C',
  },
  text: {
    primary: '#EAEEF7',
    secondary: '#98A2BE',
  },
  divider: '#262E42',
};
