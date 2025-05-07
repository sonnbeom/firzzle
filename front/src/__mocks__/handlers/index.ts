import { recommendHandlers } from './recommendHandlers';
import { summaryHandlers } from './summaryHandlers';

export const handlers = [
  ...summaryHandlers,
  ...recommendHandlers,
];
