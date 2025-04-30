import { FetchClient } from './fetchClient';

const baseUrl = process.env.NEXT_PUBLIC_API_BASE_URL;

export const api = new FetchClient(baseUrl);
