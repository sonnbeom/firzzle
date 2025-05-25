import { FetchClient } from './fetchClient';

const baseUrl = process.env.NEXT_PUBLIC_API_BASE_URL;

// API 인스턴스
export const api = new FetchClient(baseUrl);
