export interface ApiResponse {
  status: string;
  message: string;
}

// 에러 응답
export type ApiResponseError = ApiResponse & {
  message: string;
};

// 데이터 응답
export type ApiResponseWithData<T> = ApiResponse & {
  data: T;
};
