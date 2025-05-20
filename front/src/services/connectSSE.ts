import { Event, EventSourcePolyfill } from 'event-source-polyfill';
import { getCookie } from '@/actions/auth';
import { FetchClient } from '@/api/common/fetchClient';
import { SSEEventData } from '@/types/sse';

interface ConnectSSEProps {
  url: string;
  onConnect?: (data: SSEEventData) => void;
  onStart?: (data: SSEEventData) => void;
  onProgress?: (data: SSEEventData) => void;
  onResult?: (data: SSEEventData) => void;
  onComplete?: (data: SSEEventData) => void;
  onError?: (error: SSEEventData | Event) => void;
  token?: string;
}

class SSEManager {
  private static instance: SSEManager;
  private eventSource: EventSourcePolyfill | null = null;
  private url: string | null = null;
  private visibilityHandler: (() => void) | null = null;

  private constructor() {}

  public static getInstance(): SSEManager {
    if (!SSEManager.instance) {
      SSEManager.instance = new SSEManager();
    }
    return SSEManager.instance;
  }

  private setupVisibilityHandler(): void {
    if (typeof document !== 'undefined') {
      this.visibilityHandler = () => {
        if (document.visibilityState === 'visible') {
          if (!this.isConnected() && this.url) {
            this.connect({
              url: this.url,
              onConnect: this.currentCallbacks?.onConnect,
              onStart: this.currentCallbacks?.onStart,
              onProgress: this.currentCallbacks?.onProgress,
              onResult: this.currentCallbacks?.onResult,
              onComplete: this.currentCallbacks?.onComplete,
              onError: this.currentCallbacks?.onError,
            });
          }
        }
      };
      document.addEventListener('visibilitychange', this.visibilityHandler);
    }
  }

  private currentCallbacks: ConnectSSEProps | null = null;

  private async refreshToken(): Promise<string> {
    const fetchClient = new FetchClient(process.env.NEXT_PUBLIC_BASE_URL);
    await fetchClient.post('/api/auth/refresh', {
      body: { retryCount: 0 },
      withAuth: true,
    });
    const newToken = await getCookie('accessToken');
    return newToken;
  }

  public async connect({
    url,
    onConnect,
    onStart,
    onProgress,
    onResult,
    onComplete,
    onError,
    token,
  }: ConnectSSEProps): Promise<EventSourcePolyfill> {
    // 콜백 저장
    this.currentCallbacks = {
      url,
      onConnect,
      onStart,
      onProgress,
      onResult,
      onComplete,
      onError,
    };

    // 이미 연결된 SSE가 있고 같은 URL이면 기존 연결 반환
    if (this.eventSource && this.url === url) {
      return this.eventSource;
    }

    // 이미 연결된 SSE가 있지만 다른 URL이면 기존 연결 종료
    if (this.eventSource) {
      this.disconnect();
    }

    // Visibility API 핸들러 설정 (탭 전환 시 재연결)
    this.setupVisibilityHandler();

    const accessToken = token || (await getCookie('accessToken'));

    this.eventSource = new EventSourcePolyfill(url, {
      headers: {
        Accept: 'text/event-stream',
        Authorization: `Bearer ${accessToken}`,
      },
      withCredentials: true,
    });
    this.url = url;

    // EventSource 자체의 연결 오류 처리
    this.eventSource.onerror = async (error) => {
      console.log('EventSource 연결 오류:', error);
      // 401 에러 발생 시 토큰 갱신 후 재연결
      const newToken = await this.refreshToken();

      console.log('SSE 토큰 갱신');
      if (this.url) {
        this.connect({
          url: this.url,
          onConnect: this.currentCallbacks?.onConnect,
          onStart: this.currentCallbacks?.onStart,
          onProgress: this.currentCallbacks?.onProgress,
          onResult: this.currentCallbacks?.onResult,
          onComplete: this.currentCallbacks?.onComplete,
          onError: this.currentCallbacks?.onError,
          token: newToken,
        });
      }
    };

    // 하트비트 이벤트 처리
    this.eventSource.addEventListener('heartbeat', () => {});

    // 연결 성공
    this.eventSource.addEventListener('connect', (event: MessageEvent) => {
      try {
        const data = JSON.parse(event.data) as SSEEventData;
        onConnect?.(data);
      } catch (error) {
        throw error;
      }
    });

    // 시작
    this.eventSource.addEventListener('start', (event: MessageEvent) => {
      try {
        const data = JSON.parse(event.data) as SSEEventData;
        onStart?.(data);
      } catch (error) {
        throw error;
      }
    });

    // 진행 상황
    this.eventSource.addEventListener('progress', (event: MessageEvent) => {
      try {
        const data = JSON.parse(event.data) as SSEEventData;
        onProgress?.(data);
      } catch (error) {
        throw error;
      }
    });

    // 결과
    this.eventSource.addEventListener('result', (event: MessageEvent) => {
      try {
        const data = JSON.parse(event.data) as SSEEventData;
        onResult?.(data);
      } catch (error) {
        throw error;
      }
    });

    // 완료
    this.eventSource.addEventListener('complete', (event: MessageEvent) => {
      try {
        const data = JSON.parse(event.data) as SSEEventData;
        onComplete?.(data);
      } catch (error) {
        throw error;
      }
    });

    // 오류 발생
    this.eventSource.addEventListener('error', (event: MessageEvent) => {
      try {
        const data = JSON.parse(event.data) as SSEEventData;
        onError?.(data);
      } catch (error) {
        throw error;
      }
    });

    return this.eventSource;
  }

  public disconnect(): void {
    if (this.eventSource) {
      this.eventSource.close();
      this.eventSource = null;
      this.url = null;
      this.currentCallbacks = null;
    }

    // Visibility API 핸들러 제거
    if (this.visibilityHandler && typeof document !== 'undefined') {
      document.removeEventListener('visibilitychange', this.visibilityHandler);
      this.visibilityHandler = null;
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
