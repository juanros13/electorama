import { TonalPalette } from '@/app/core/theming/palette';

export type Scheme = 'light' | 'dark' | 'system';
export type Colors = {
  primary: string;
  secondary: string;
  error: string;
};
export type ThemeConfig = Colors & Record<'scheme', Scheme>;

export type Theme = {
  primary: TonalPalette;
  secondary: TonalPalette;
  error: TonalPalette;
};
