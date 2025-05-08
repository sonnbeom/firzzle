import {
  ApiResponseError,
  ApiResponseWithData,
  ApiResponseWithoutData,
} from '@/types/common';
import { getCookie, removeCookie } from '@/utils/auth';
import { refresh } from '../auth';

type Params<T = unknown> = {
  [K in keyof T]?: string | number | boolean | null | undefined;
};

type FetchOptions<TBody = unknown, TParams = unknown> = Omit<
  RequestInit,
  'headers' | 'body'
> & {
  headers?: Record<string, string>; // 요청 헤더
  body?: TBody; // 요청 본문
  withAuth?: boolean; // 인증 여부
  contentType?: string; // 요청 컨텐츠 타입
  params?: Params<TParams>; // URL 쿼리 파라미터
  retryCount?: number; // 재시도 횟수
};

export class FetchClient {
  private baseUrl: string;
  private readonly MAX_RETRIES = 2;

  constructor(baseUrl: string) {
    this.baseUrl = baseUrl;
  }

  // 응답 데이터가 있는 요청 메서드(오버로드)
  private async request<TResponse, TBody = unknown>(
    url: string,
    options: FetchOptions<TBody>,
  ): Promise<ApiResponseWithData<TResponse>>;

  // 응답 데이터가 없는 요청 메서드(오버로드)
  private async request<TBody = unknown>(
    url: string,
    options: FetchOptions<TBody>,
  ): Promise<ApiResponseWithData<undefined | null>>;

  // 요청 메서드
  private async request<TResponse = undefined, TBody = unknown>(
    url: string,
    options: FetchOptions<TBody>,
  ): Promise<ApiResponseWithData<TResponse> | null> {
    const {
      withAuth = true,
      contentType = 'application/json',
      headers,
      body,
      params,
      retryCount = 0,
      ...restOptions
    } = options;

    const accessToken = withAuth ? getCookie('accessToken') : '';

    // 헤더 객체 생성
    const allHeaders = new Headers(
      Object.assign(
        {
          'Content-Type': contentType,
        },
        withAuth ? { Authorization: `Bearer ${accessToken}` } : {},
        headers,
      ),
    );

    let apiUrl = `${this.baseUrl}${url}`;

    if (params && Object.keys(params).length > 0) {
      const queryParams = Object.entries(params)
        .filter(([_, value]) => value != null)
        .map(([key, value]) => `${key}=${value}`)
        .join('&');

      if (queryParams) {
        apiUrl += `?${queryParams}`;
      }
    }

    try {
      // fetch 요청 응답
      const response = await fetch(apiUrl, {
        ...restOptions,
        headers: allHeaders,
        body: body ? JSON.stringify(body) : undefined,
      });

      // accessToken이 만료된 경우 토큰 갱신
      if (response.status === 401) {
        if (retryCount >= this.MAX_RETRIES) {
          // 토큰 갱신 실패 시 로그아웃 처리
          removeCookie('accessToken');
          window.location.href = '/';
          throw new Error('토큰 갱신을 실패했습니다.');
        }

        await refresh();
        return this.request(url, {
          ...options,
          retryCount: retryCount + 1,
        });
      }

      // 응답 데이터 파싱
      const responseData = await response.json();

      // 응답 데이터가 에러일 경우 예외처리
      if (!response.ok) {
        throw {
          status: 'FAIL',
          cause: responseData.cause,
          message: responseData.message,
          prevUrl: responseData.prevUrl,
          redirectUrl: responseData.redirectUrl,
          data: null,
        } as ApiResponseError;
      }

      // 204 No Content 응답 처리
      if (response.status === 204) {
        return {
          status: 'OK',
          cause: '',
          message: '',
          prevUrl: '',
          redirectUrl: '',
          data: null,
        } as ApiResponseWithoutData;
      }

      const dataResponse = responseData as ApiResponseWithData<TResponse>;

      // 응답 데이터의 status가 FAIL인 경우 예외처리
      if (dataResponse.status === 'FAIL') {
        throw new Error(dataResponse.message);
      }

      return dataResponse;
    } catch (error) {
      // 재시도 횟수가 최대 재시도 횟수보다 작은 경우에만 재시도
      if (retryCount < this.MAX_RETRIES) {
        await new Promise((resolve) =>
          setTimeout(resolve, 1000 * (retryCount + 1)),
        );

        // 재시도 시도
        return this.request(url, {
          ...options,
          retryCount: retryCount + 1,
        });
      }

      // 최대 재시도 횟수를 초과한 경우 에러 처리
      throw error;
    }
  }

  // GET 요청
  public get<TResponse>(url: string, options?: FetchOptions) {
    return this.request<TResponse>(url, { method: 'GET', ...options });
  }

  // POST 요청
  public post<TResponse, TBody = unknown>(
    url: string,
    options?: FetchOptions<TBody>,
  ) {
    return this.request<TResponse>(url, { method: 'POST', ...options });
  }

  // PATCH 요청
  public patch<TResponse, TBody>(url: string, options?: FetchOptions<TBody>) {
    return this.request<TResponse>(url, { method: 'PATCH', ...options });
  }

  // DELETE 요청
  public delete<TResponse>(url: string, options?: FetchOptions) {
    return this.request<TResponse>(url, { method: 'DELETE', ...options });
  }
}
