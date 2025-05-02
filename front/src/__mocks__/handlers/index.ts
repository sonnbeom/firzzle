import { quizHandlers } from './quizHandlers';
import { summaryHandlers } from './summaryHandlers';

export const handlers = [...summaryHandlers, ...quizHandlers];
