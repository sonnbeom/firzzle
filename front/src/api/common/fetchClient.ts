import { ApiResponseError, ApiResponseWithData } from '@/types/common';

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
};

export class FetchClient {
  private baseUrl: string;

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
      ...restOptions
    } = options;

    const session = { accessToken: '' }; // 추후 auth api 연결

    // 헤더 객체 생성
    const allHeaders = new Headers(
      Object.assign(
        {
          'Content-Type': contentType,
        },
        withAuth ? { Authorization: `Bearer ${session?.accessToken}` } : {},
        headers,
      ),
    );

    let apiUrl = `${this.baseUrl}${url}`;

    if (params && Object.keys(params).length > 0) {
      const queryString = new URLSearchParams(
        Object.entries(params)
          .filter(([_, value]) => value != null)
          .map(([key, value]) => [key, String(value)]),
      ).toString();
      if (queryString) {
        apiUrl += `?${queryString}`;
      }
    }

    // fetch 요청 응답
    const response = await fetch(apiUrl, {
      ...restOptions,
      headers: allHeaders,
      body: body ? JSON.stringify(body) : undefined,
    });

    // 응답 데이터 파싱
    const responseData = await response.json();

    // 응답 데이터가 에러일 경우 예외처리
    if (!response.ok) {
      throw responseData as ApiResponseError;
    }

    return responseData as ApiResponseWithData<TResponse>;
  }

  // GET 요청
  public get<TResponse>(url: string, options?: FetchOptions) {
    return this.request<TResponse>(url, { method: 'GET', ...options });
  }

  // POST 요청
  public post<TResponse, TBody>(url: string, options?: FetchOptions<TBody>) {
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
