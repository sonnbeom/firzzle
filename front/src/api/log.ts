import { TransitionLogRequest } from '@/types/log';

// 방문 로깅 추가
export const addVistiCount = async () => {
  const response = await fetch('/api/logging/visit', {
    method: 'POST',
  });

  const data = await response.json();

  if (response.status === 200) {
    return data;
  }

  throw new Error(data.message);
};

// 컨텐츠 전환 로깅
export const postTransitionLog = async (request: TransitionLogRequest) => {
  const response = await fetch('/api/logging/transition', {
    method: 'POST',
    body: JSON.stringify(request),
  });

  const data = await response.json();

  if (response.status === 200) {
    return data;
  }

  throw new Error(data.message);
};
