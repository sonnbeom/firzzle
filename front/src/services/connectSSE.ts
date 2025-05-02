import { Event, EventSourcePolyfill } from 'event-source-polyfill';

interface ConnectSSEProps {
  url: string;
  onMessage: (event: MessageEvent) => void;
  onConnect?: () => void;
  onError?: (error: Event) => void;
}

export const connectSSE = async ({
  url,
  onMessage,
  onConnect,
  onError,
}: ConnectSSEProps) => {
  const accessToken = ''; // 추후 auth api 연결

  // SSE 연결 설정
  const eventSource = new EventSourcePolyfill(url, {
    headers: { Authorization: `Bearer ${accessToken}` },
  });

  // 연결 성공
  eventSource.addEventListener('connect', (event: MessageEvent) => {
    if (event.data === 'connected') {
      onConnect?.(); // 연결 성공 콜백 호출
    }
  });

  // 메시지 수신
  eventSource.addEventListener('message', (event: MessageEvent) => {
    onMessage(event);
  });

  // 오류 발생
  eventSource.onerror = (error) => {
    console.error('SSE Error:', error);
    onError?.(error);
    eventSource.close();
  };

  return eventSource;
};
