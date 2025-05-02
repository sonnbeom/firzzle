import { quizHandlers } from './quizHandlers';
import { recommendHandlers } from './recommendHandlers';
import { summaryHandlers } from './summaryHandlers';

export const handlers = [
  ...summaryHandlers,
  ...quizHandlers,
  ...recommendHandlers,
];
