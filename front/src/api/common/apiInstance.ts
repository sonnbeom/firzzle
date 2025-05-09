import { FetchClient } from './fetchClient';

const baseUrl =
  process.env.NODE_ENV === 'production'
    ? process.env.NEXT_PUBLIC_API_BASE_URL
    : process.env.NEXT_PUBLIC_API_BASE_URL_DEV;

const isServer = () => {
  return typeof window === 'undefined';
};

// 서버 사이드 API 인스턴스
export const externalApi = new FetchClient(baseUrl, isServer());
