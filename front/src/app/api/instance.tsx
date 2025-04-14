import axios, { AxiosError } from 'axios';
import { ErrorResponse } from 'types/common/ErrorResponse';

const api = axios.create({
  baseURL: process.env.NEXT_BASE_URL,
  withCredentials: true,
  headers: {
    'Content-Type': 'application/json',
  },
});

api.interceptors.response.use(
  (response) => response,
  async (error: AxiosError<ErrorResponse>) => {
    return Promise.reject(error);
  },
);
