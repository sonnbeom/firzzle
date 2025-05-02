import { FetchClient } from './fetchClient';

const baseUrl =
  process.env.NODE_ENV === 'production'
    ? process.env.NEXT_PUBLIC_API_BASE_URL
    : process.env.NEXT_PUBLIC_API_BASE_URL_DEV;

// 외부 API 인스턴스
export const externalApi = new FetchClient(baseUrl);

// 내부 API 인스턴스
export const internalApi = new FetchClient('/api');
