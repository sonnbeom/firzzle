import { ApiResponseWithData, ApiResponseWithoutData } from '@/types/common';

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
      contentType = 'application/json',
      headers,
      body,
      params,
      retryCount = 0,
      ...restOptions
    } = options;

    // 헤더 객체 생성
    const allHeaders = new Headers(
      Object.assign(
        {
          'Content-Type': contentType,
        },
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
        credentials: 'include',
      });

      // 401 에러 처리
      if (response.status === 401) {
        throw new Error(response.statusText);
      }

      if (!response.ok) {
        throw new Error('API 호출을 실패했습니다.');
      }

      // 204 No Content 응답 처리
      if (response.status === 204) {
        return {
          status: 'OK',
          message: '',
          cause: '',
          prevUrl: '',
          redirectUrl: '',
          data: null,
        } as ApiResponseWithoutData;
      }

      // 응답 데이터 파싱
      const responseData = await response.json();

      const dataResponse = responseData as ApiResponseWithData<TResponse>;

      // 응답 데이터의 status가 FAIL인 경우 예외처리
      if (dataResponse.status === 'FAIL') {
        throw new Error(dataResponse.message);
      }

      return dataResponse;
    } catch (error) {
      // 재시도
      if (retryCount < this.MAX_RETRIES) {
        await new Promise((resolve) =>
          setTimeout(resolve, 1000 * (retryCount + 1)),
        );

        if (error.message === 'Unauthorized') {
          // 토큰 갱신 API
          console.log('토큰 갱신 시도');
          const response = await fetch('/api/auth/refresh', {
            method: 'POST',
            credentials: 'include',
          });

          console.log('토큰 갱신 응답: ', response);

          const data = await response.json();

          console.log('토큰 갱신 응답 데이터: ', data);

          if (data.message === '토큰 갱신 성공') {
            return this.request(url, {
              ...options,
              retryCount: retryCount + 1,
            });
          } else {
            throw new Error(data.message);
          }
        }

        return this.request(url, {
          ...options,
          retryCount: retryCount + 1,
        });
      }
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
