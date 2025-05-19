import { Event, EventSourcePolyfill } from 'event-source-polyfill';
import { getCookie } from '@/actions/auth';
import { SSEEventData } from '@/types/sse';

interface ConnectSSEProps {
  url: string;
  onConnect?: (data: SSEEventData) => void;
  onStart?: (data: SSEEventData) => void;
  onProgress?: (data: SSEEventData) => void;
  onResult?: (data: SSEEventData) => void;
  onComplete?: (data: SSEEventData) => void;
  onError?: (error: SSEEventData | Event) => void;
}

class SSEManager {
  private static instance: SSEManager;
  private eventSource: EventSourcePolyfill | null = null;
  private url: string | null = null;
  private reconnectAttempts: number = 0;
  private maxReconnectAttempts: number = 2;

  private constructor() {}

  public static getInstance(): SSEManager {
    if (!SSEManager.instance) {
      SSEManager.instance = new SSEManager();
    }
    return SSEManager.instance;
  }

  public async connect({
    url,
    onConnect,
    onStart,
    onProgress,
    onResult,
    onComplete,
    onError,
  }: ConnectSSEProps): Promise<EventSourcePolyfill> {
    // 재연결 시도 횟수 초기화
    this.reconnectAttempts = 0;

    // 이미 연결된 SSE가 있고 같은 URL이면 기존 연결 반환
    if (this.eventSource && this.url === url) {
      return this.eventSource;
    }

    // 이미 연결된 SSE가 있지만 다른 URL이면 기존 연결 종료
    if (this.eventSource) {
      this.disconnect();
    }

    const accessToken = await getCookie('accessToken');

    this.eventSource = new EventSourcePolyfill(url, {
      headers: {
        Accept: 'text/event-stream',
        Authorization: `Bearer ${accessToken}`,
      },
      heartbeatTimeout: 45000, // 45초 타임아웃 설정
    });
    this.url = url;

    // 하트비트 이벤트 처리
    this.eventSource.addEventListener('heartbeat', () => {
      // 하트비트 수신 시 아무 작업도 하지 않음 (연결 유지 목적)
      console.debug('Heartbeat received');
    });

    // 연결 성공
    this.eventSource.addEventListener('connect', (event: MessageEvent) => {
      try {
        const data = JSON.parse(event.data) as SSEEventData;
        onConnect?.(data);
      } catch (error) {
        console.error('Connect event parsing error:', error);
      }
    });

    // 시작
    this.eventSource.addEventListener('start', (event: MessageEvent) => {
      try {
        const data = JSON.parse(event.data) as SSEEventData;
        onStart?.(data);
      } catch (error) {
        console.error('Start event parsing error:', error);
      }
    });

    // 진행 상황
    this.eventSource.addEventListener('progress', (event: MessageEvent) => {
      try {
        const data = JSON.parse(event.data) as SSEEventData;
        onProgress?.(data);
      } catch (error) {
        console.error('Progress event parsing error:', error);
      }
    });

    // 결과
    this.eventSource.addEventListener('result', (event: MessageEvent) => {
      try {
        const data = JSON.parse(event.data) as SSEEventData;
        onResult?.(data);
      } catch (error) {
        console.error('Result event parsing error:', error);
      }
    });

    // 완료
    this.eventSource.addEventListener('complete', (event: MessageEvent) => {
      try {
        const data = JSON.parse(event.data) as SSEEventData;
        onComplete?.(data);
        this.disconnect();
      } catch (error) {
        console.error('Complete event parsing error:', error);
      }
    });

    // 오류 발생
    this.eventSource.addEventListener('error', (event: MessageEvent) => {
      try {
        // 데이터가 있는 경우에만 JSON 파싱 시도
        if (event.data) {
          const data = JSON.parse(event.data) as SSEEventData;
          onError?.(data);
        } else {
          // 데이터가 없는 경우 기본 에러 객체 생성
          onError?.({
            message: '연결이 끊어졌습니다. 다시 연결을 시도합니다.',
            timestamp: new Date().toISOString(),
          });
        }
      } catch (error) {
        console.error('Error event parsing error:', error);
        // JSON 파싱 에러 시 기본 에러 객체 생성
        onError?.({
          message: '연결 중 오류가 발생했습니다.',
          timestamp: new Date().toISOString(),
        });
      }

      // 연결 종료
      this.disconnect();

      // 재연결 시도 횟수가 최대 시도 횟수를 초과하지 않은 경우에만 재연결
      if (this.reconnectAttempts < this.maxReconnectAttempts) {
        this.reconnectAttempts++;
        // 1초 후 재연결 시도
        setTimeout(() => {
          this.connect({
            url,
            onConnect,
            onStart,
            onProgress,
            onResult,
            onComplete,
            onError,
          });
        }, 1000);
      } else {
        // 최대 재연결 시도 횟수를 초과한 경우
        onError?.({
          message:
            '연결 시도 횟수를 초과했습니다. 페이지를 새로고침하여 다시 시도해주세요.',
          timestamp: new Date().toISOString(),
        });
      }
    });

    return this.eventSource;
  }

  public disconnect(): void {
    if (this.eventSource) {
      this.eventSource.close();
      this.eventSource = null;
      this.url = null;
      this.reconnectAttempts = 0; // 연결이 끊어질 때 재연결 시도 횟수 초기화
    }
  }

  public isConnected(): boolean {
    return this.eventSource !== null;
  }

  public getCurrentUrl(): string | null {
    return this.url;
  }
}

export const sseManager = SSEManager.getInstance();
