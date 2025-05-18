import BasicToaster from '@/components/common/BasicToaster';

export const getPlayer = async (url: string) => {
  // 서버 사이드에서는 절대 경로를, 클라이언트 사이드에서는 상대 경로를 사용
  const baseUrl =
    typeof window === 'undefined' ? process.env.NEXT_PUBLIC_BASE_URL : '';

  const response = await fetch(
    `${baseUrl}/api/player?url=${encodeURIComponent(url)}`,
    {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json',
      },
    },
  );

  const data = await response.json();

  if (response.status === 200) {
    return data.data;
  } else {
    BasicToaster.error(data.message, {
      id: 'fetch youtube',
      duration: 2000,
    });
  }
};
