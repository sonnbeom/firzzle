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
  token?: string;
}

class SSEManager {
  private static instance: SSEManager;
  private eventSource: EventSourcePolyfill | null = null;
  private url: string | null = null;
  private visibilityHandler: (() => void) | null = null; // 브라우저 탭 변경 시 재연결
  private currentCallbacks: ConnectSSEProps | null = null;

  private constructor() {}

  public static getInstance(): SSEManager {
    if (!SSEManager.instance) {
      SSEManager.instance = new SSEManager();
    }
    return SSEManager.instance;
  }

  // 브라우저 탭 변경 시 재연결
  private setupVisibilityHandler(): void {
    if (typeof document !== 'undefined') {
      this.visibilityHandler = () => {
        if (
          document.visibilityState === 'visible' &&
          !this.isConnected() &&
          this.url
        ) {
          this.connect({
            url: this.url,
            ...this.currentCallbacks,
          });
        }
      };
      document.addEventListener('visibilitychange', this.visibilityHandler);
    }
  }

  // 이벤트 리스너 설정
  private setupEventListeners(): void {
    if (!this.eventSource || !this.currentCallbacks) return;

    const handleEvent = (
      eventName: string,
      callback?: (data: SSEEventData) => void,
    ) => {
      this.eventSource?.addEventListener(eventName, (event: MessageEvent) => {
        try {
          if (!event.data) return;
          const data = JSON.parse(event.data) as SSEEventData;
          callback?.(data);
        } catch (error) {
          throw new Error(`${eventName} 이벤트 처리 중 오류:`, error);
        }
      });
    };

    this.eventSource.addEventListener('heartbeat', () => {});
    handleEvent('connect', this.currentCallbacks.onConnect);
    handleEvent('start', this.currentCallbacks.onStart);
    handleEvent('progress', this.currentCallbacks.onProgress);
    handleEvent('result', this.currentCallbacks.onResult);
    handleEvent('complete', this.currentCallbacks.onComplete);
    handleEvent('error', this.currentCallbacks.onError);
  }

  // 토큰 갱신
  private async handleRefreshToken(
    url: string,
    accessToken: string,
  ): Promise<void> {
    // refresh_token 쿠키 가져오기
    const refreshToken = await getCookie('refresh_token');

    // 토큰 갱신 요청
    const response = await fetch('/api/auth/refresh', {
      method: 'POST',
      credentials: 'include',
      body: JSON.stringify({ retryCount: 1 }),
      headers: {
        'Content-Type': 'application/json',
        Authorization: `Bearer ${accessToken}`,
        Cookie: `accessToken=${accessToken}; refresh_token=${refreshToken}`,
      },
    });

    if (response.status !== 200) {
      throw new Error('토큰 갱신 실패');
    }

    const data = await response.json();
    const newAccessToken = data.data;

    this.eventSource?.close();

    // 새로운 이벤트 소스 생성
    this.eventSource = new EventSourcePolyfill(url, {
      headers: {
        Accept: 'text/event-stream',
        Authorization: `Bearer ${newAccessToken}`,
      },
      withCredentials: true,
    });

    // 이벤트 리스너 설정
    this.setupEventListeners();
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
    this.currentCallbacks = {
      url,
      onConnect,
      onStart,
      onProgress,
      onResult,
      onComplete,
      onError,
    };

    if (this.eventSource && this.url === url) {
      return this.eventSource;
    }

    if (this.eventSource) {
      this.disconnect();
    }

    // 브라우저 탭 변경 시 재연결
    this.setupVisibilityHandler();

    // accessToken 가져오기
    const accessToken = await getCookie('accessToken');

    try {
      // 이벤트 소스 생성
      this.eventSource = new EventSourcePolyfill(url, {
        headers: {
          Accept: 'text/event-stream',
          Authorization: `Bearer ${accessToken}`,
        },
        withCredentials: true,
      });
      this.url = url;

      // 오류 처리
      this.eventSource.onerror = async (event: Event) => {
        const target = event.target as EventSourcePolyfill;
        if (target.readyState === EventSourcePolyfill.CLOSED) {
          try {
            await this.handleRefreshToken(url, accessToken);
          } catch (error) {
            console.error('토큰 갱신 중 오류:', error);
            onError?.(event);
          }
        } else {
          onError?.(event);
        }
      };

      this.setupEventListeners();
      return this.eventSource;
    } catch (error) {
      console.error('SSE 연결 생성 중 오류:', error);
      throw error;
    }
  }

  public disconnect(): void {
    if (this.eventSource) {
      this.eventSource.close();
      this.eventSource = null;
      this.url = null;
      this.currentCallbacks = null;
    }

    // 브라우저 탭 변경 시 재연결 해제
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
