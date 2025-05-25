// 로그 응답 타입
export interface LogResponse {
  data: string;
}

// 컨텐츠 전환 로그 요청 타입
export type TransitionLogRequest = {
  fromContent: string;
  toContent: string;
};
