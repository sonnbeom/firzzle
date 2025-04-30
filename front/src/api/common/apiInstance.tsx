import { FetchClient } from './fetchClient';

const baseUrl = process.env.API_BASE_URL;

export const api = new FetchClient(baseUrl);
