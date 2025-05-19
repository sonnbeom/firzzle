// 방문 로깅 추가
export const addVistiCount = async () => {
  const response = await fetch('/api/logging/visit', {
    method: 'POST',
  });

  const data = await response.json();

  if (response.status === 200) {
    return data;
  }

  console.error('방문체크 실패:', data.message);
  throw new Error(data.message);
};
