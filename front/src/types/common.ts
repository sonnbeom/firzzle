// 공통 응답 타입
interface ApiResponse {
  status: 'OK' | 'FAIL';
  cause: string;
  message: string;
  prevUrl: string;
  redirectUrl: string;
}

export type ApiResponseWithData<T> = ApiResponse & {
  data: T;
};

export type ApiResponseError = ApiResponse & {
  data: null;
};
