import { FetchClient } from './fetchClient';

const baseUrl =
  process.env.NODE_ENV === 'production'
    ? process.env.NEXT_PUBLIC_API_BASE_URL
    : process.env.NEXT_PUBLIC_API_BASE_URL_DEV;

export const api = new FetchClient(baseUrl);
