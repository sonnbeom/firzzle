export const BASE_URL =
  process.env.NODE_ENV === 'production'
    ? process.env.NEXT_PUBLIC_API_BASE_URL
    : process.env.NEXT_PUBLIC_API_BASE_URL_DEV;
// 러닝챗 입력 글자수 제한
export const MAX_LEARNING_CHAT_LENGTH = 200;
// 러닝챗봇 아이디
export const LEARNING_CHATBOT_ID = '0';
// 스냅 리뷰 입력 글자수 제제한
export const MAX_SNAP_REVIEW_LENGTH = 250;
// 사진첩 페이지네이션 숫자
export const ITEMS_PER_PAGE = 2;
