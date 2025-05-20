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
          console.log('[SSE] Tab became visible, checking connection...');
          if (!this.isConnected() && this.url) {
            console.log('[SSE] Reconnecting after tab visibility change...');
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

  public async connect({
    url,
    onConnect,
    onStart,
    onProgress,
    onResult,
    onComplete,
    onError,
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

    // Visibility API 핸들러 설정
    this.setupVisibilityHandler();

    const accessToken = await getCookie('accessToken');

    console.log('[SSE] Attempting to connect:', {
      url,
      hasAccessToken: !!accessToken,
      timestamp: new Date().toISOString(),
    });

    this.eventSource = new EventSourcePolyfill(url, {
      headers: {
        Accept: 'text/event-stream',
        Authorization: `Bearer ${accessToken}`,
      },
      withCredentials: true,
    });
    this.url = url;

    // 연결 시도 시점 로깅
    this.eventSource.onopen = () => {
      console.log('[SSE] Connection opened successfully:', {
        url,
        timestamp: new Date().toISOString(),
        readyState: this.eventSource?.readyState,
      });
    };

    // 연결 실패 시점 로깅
    this.eventSource.onerror = (error) => {
      console.error('[SSE] Connection error:', {
        error,
        readyState: this.eventSource?.readyState,
        timestamp: new Date().toISOString(),
        url: this.url,
      });

      // readyState 값에 따른 상태 로깅
      const state = this.eventSource?.readyState;
      switch (state) {
        case 0:
          console.error('[SSE] Connection is being established');
          break;
        case 1:
          console.error('[SSE] Connection is open and working');
          break;
        case 2:
          console.error('[SSE] Connection is closed');
          break;
        default:
          console.error('[SSE] Unknown connection state');
      }
    };

    // 하트비트 이벤트 처리
    this.eventSource.addEventListener('heartbeat', () => {
      console.debug('[SSE] Heartbeat received at:', new Date().toISOString());
    });

    // 연결 성공
    this.eventSource.addEventListener('connect', (event: MessageEvent) => {
      console.log('[SSE] Connect event received:', {
        event,
        data: event.data ? JSON.parse(event.data) : null,
        timestamp: new Date().toISOString(),
      });
      try {
        if (!event.data) {
          console.warn('[SSE] Connect event received with no data');
          return;
        }
        const data = JSON.parse(event.data) as SSEEventData;
        onConnect?.(data);
      } catch (error) {
        console.error('[SSE] Connect event parsing error:', error);
      }
    });

    // 시작
    this.eventSource.addEventListener('start', (event: MessageEvent) => {
      console.log('[SSE] Start event received:', {
        event,
        data: event.data ? JSON.parse(event.data) : null,
        timestamp: new Date().toISOString(),
      });
      try {
        if (!event.data) {
          console.warn('[SSE] Start event received with no data');
          return;
        }
        const data = JSON.parse(event.data) as SSEEventData;
        onStart?.(data);
      } catch (error) {
        console.error('[SSE] Start event parsing error:', error);
      }
    });

    // 진행 상황
    this.eventSource.addEventListener('progress', (event: MessageEvent) => {
      console.log('[SSE] Progress event received:', {
        event,
        data: event.data ? JSON.parse(event.data) : null,
        timestamp: new Date().toISOString(),
      });
      try {
        if (!event.data) {
          console.warn('[SSE] Progress event received with no data');
          return;
        }
        const data = JSON.parse(event.data) as SSEEventData;
        onProgress?.(data);
      } catch (error) {
        console.error('[SSE] Progress event parsing error:', error);
      }
    });

    // 결과
    this.eventSource.addEventListener('result', (event: MessageEvent) => {
      console.log('[SSE] Result event received:', {
        event,
        data: event.data ? JSON.parse(event.data) : null,
        timestamp: new Date().toISOString(),
      });
      try {
        if (!event.data) {
          console.warn('[SSE] Result event received with no data');
          return;
        }
        const data = JSON.parse(event.data) as SSEEventData;
        onResult?.(data);
      } catch (error) {
        console.error('[SSE] Result event parsing error:', error);
      }
    });

    // 완료
    this.eventSource.addEventListener('complete', (event: MessageEvent) => {
      console.log('[SSE] Complete event received:', {
        event,
        data: event.data ? JSON.parse(event.data) : null,
        timestamp: new Date().toISOString(),
      });
      try {
        if (!event.data) {
          console.warn('[SSE] Complete event received with no data');
          return;
        }
        const data = JSON.parse(event.data) as SSEEventData;
        onComplete?.(data);
        this.disconnect();
      } catch (error) {
        console.error('[SSE] Complete event parsing error:', error);
      }
    });

    // 오류 발생
    this.eventSource.addEventListener('error', (event: MessageEvent) => {
      console.log('에러사항: ', event);
      console.log('[SSE] Error event received:', {
        event,
        data: event.data ? JSON.parse(event.data) : null,
        timestamp: new Date().toISOString(),
        error: event instanceof Error ? event.message : 'Unknown error',
      });

      // 연결 상태 확인
      const state = this.eventSource?.readyState;
      console.log('[SSE] Connection state at error:', state);

      try {
        if (!event.data) {
          console.warn('[SSE] Error event received with no data');

          // 연결이 끊어지거나 연결 중인 경우 재연결 시도
          if (state === 2 || state === 0) {
            console.log(
              '[SSE] Connection lost or failed to connect. Attempting to reconnect...',
            );
            this.disconnect();

            // 재연결 시도 전 약간의 지연 추가
            setTimeout(() => {
              if (this.url) {
                this.connect({
                  url: this.url,
                  onConnect,
                  onStart,
                  onProgress,
                  onResult,
                  onComplete,
                  onError,
                });
              }
            }, 1000); // 1초 지연
          }

          onError?.(event);
          return;
        }
        const data = JSON.parse(event.data) as SSEEventData;
        onError?.(data);
      } catch (error) {
        console.error('[SSE] Error event parsing error:', error);
        onError?.(event);
      }

      // 연결이 완전히 끊어진 경우에만 disconnect 호출
      if (state === 2) {
        this.disconnect();
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
