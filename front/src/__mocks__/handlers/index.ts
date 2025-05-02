import { quizHandlers } from './quizHandlers';
import { summaryHandlers } from './summaryHandlers';
import { recommendHandlers } from './recommendHandlers';

export const handlers = [...summaryHandlers, ...quizHandlers, ... recommendHandlers];
