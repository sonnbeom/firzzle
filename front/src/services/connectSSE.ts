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
    });
    this.url = url;

    // 하트비트 이벤트 처리
    this.eventSource.addEventListener('heartbeat', () => {
      // 하트비트 수신 시 아무 작업도 하지 않음 (연결 유지 목적)
      console.debug('Heartbeat received');
    });

    // 연결 성공
    this.eventSource.addEventListener('connect', (event: MessageEvent) => {
      console.log('connect', event);
      try {
        if (!event.data) {
          console.warn('Connect event received with no data');
          return;
        }
        const data = JSON.parse(event.data) as SSEEventData;
        onConnect?.(data);
      } catch (error) {
        console.error('Connect event parsing error:', error);
      }
    });

    // 시작
    this.eventSource.addEventListener('start', (event: MessageEvent) => {
      console.log('start', event);
      try {
        if (!event.data) {
          console.warn('Start event received with no data');
          return;
        }
        const data = JSON.parse(event.data) as SSEEventData;
        onStart?.(data);
      } catch (error) {
        console.error('Start event parsing error:', error);
      }
    });

    // 진행 상황
    this.eventSource.addEventListener('progress', (event: MessageEvent) => {
      console.log('progress', event);
      try {
        if (!event.data) {
          console.warn('Progress event received with no data');
          return;
        }
        const data = JSON.parse(event.data) as SSEEventData;
        onProgress?.(data);
      } catch (error) {
        console.error('Progress event parsing error:', error);
      }
    });

    // 결과
    this.eventSource.addEventListener('result', (event: MessageEvent) => {
      console.log('result', event);
      try {
        if (!event.data) {
          console.warn('Result event received with no data');
          return;
        }
        const data = JSON.parse(event.data) as SSEEventData;
        onResult?.(data);
      } catch (error) {
        console.error('Result event parsing error:', error);
      }
    });

    // 완료
    this.eventSource.addEventListener('complete', (event: MessageEvent) => {
      console.log('complete', event);
      try {
        if (!event.data) {
          console.warn('Complete event received with no data');
          return;
        }
        const data = JSON.parse(event.data) as SSEEventData;
        onComplete?.(data);
        this.disconnect();
      } catch (error) {
        console.error('Complete event parsing error:', error);
      }
    });

    // 오류 발생
    this.eventSource.addEventListener('error', (event: MessageEvent) => {
      console.log('error', event);
      try {
        if (!event.data) {
          console.warn('Error event received with no data');
          onError?.(event);
          return;
        }
        const data = JSON.parse(event.data) as SSEEventData;
        onError?.(data);
      } catch (error) {
        console.error('Error event parsing error:', error);
        onError?.(event);
      }
      this.disconnect();
    });

    return this.eventSource;
  }

  public disconnect(): void {
    if (this.eventSource) {
      this.eventSource.close();
      this.eventSource = null;
      this.url = null;
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
