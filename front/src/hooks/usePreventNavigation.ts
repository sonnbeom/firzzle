import { useRouter } from 'next/navigation';
import { useState, useCallback, useRef, useEffect } from 'react';

// 페이지 이동 타입 정의
type NavigationType = 'push' | 'back';

// 페이지 이동 시도 정보 저장 타입
interface PendingNavigationInfo {
  type: NavigationType;
  url?: string;
  options?: object;
}

interface UsePreventNavigationReturn {
  showDialog: boolean;
  setShowDialog: (show: boolean) => void;
  pendingNavigation: PendingNavigationInfo | null;
  setPendingNavigation: React.Dispatch<React.SetStateAction<PendingNavigationInfo | null>>;
  blockUnload: React.MutableRefObject<boolean>;
  router: ReturnType<typeof useRouter>;
  originalPushRef: React.MutableRefObject<((href: string, options?: object) => Promise<boolean>) | null>;
}

export const usePreventNavigation = (
  shouldPrevent: boolean,
): UsePreventNavigationReturn => {
  const router = useRouter();
  const [showDialog, setShowDialog] = useState(false);
  const blockUnload = useRef(false);
  const originalPushRef = useRef<
    ((href: string, options?: object) => Promise<boolean>) | null
  >(null);
  const [pendingNavigation, setPendingNavigation] =
    useState<PendingNavigationInfo | null>(null);

  useEffect(() => {
    if (!shouldPrevent) {
      blockUnload.current = false;
      return;
    }

    blockUnload.current = true;

    // 원래 라우터 함수 참조
    const originalPush = router.push.bind(router);
    originalPushRef.current = originalPush;

    // 라우터 오버라이드
    router.push = (href: string, options?: object) => {
      if (blockUnload.current) {
        setPendingNavigation({
          type: 'push',
          url: href,
          options,
        });
        setShowDialog(true);
        return Promise.resolve(false);
      }
      return originalPushRef.current?.(href, options) || Promise.resolve(false);
    };

    // 뒤로가기 감지 및 처리 함수
    const handlePopState = () => {
      if (shouldPrevent) {
        window.history.pushState(null, '', window.location.href);
        setPendingNavigation({ type: 'back' });
        setShowDialog(true);
      }
    };

    window.addEventListener('popstate', handlePopState);
    window.history.pushState(null, '', window.location.href);

    return () => {
      window.removeEventListener('popstate', handlePopState);
      if (originalPushRef.current) router.push = originalPushRef.current;
      blockUnload.current = false;
    };
  }, [shouldPrevent, router]);

  // 경고 다이얼로그 상태 관리
  const handleDialogChange = (open: boolean) => {
    setShowDialog(open);
    if (!open) {
      setPendingNavigation(null);
    }
  };

  const executeNavigation = useCallback(() => {
    if (!pendingNavigation) return;

    try {
      // 페이지 이탈 방지 해제
      blockUnload.current = false;

      if (pendingNavigation.type === 'push' && pendingNavigation.url) {
        // 다른 페이지로 이동
        if (originalPushRef.current) {
          const pushFn = originalPushRef.current;
          pushFn(pendingNavigation.url, pendingNavigation.options);
        }
      } else if (pendingNavigation.type === 'back') {
        // popstate 이벤트 리스너 제거 후 뒤로가기 실행
        const popstateListener = (e: PopStateEvent) => {
          e.preventDefault();
          window.removeEventListener('popstate', popstateListener);
          window.history.back();
        };
        
        window.addEventListener('popstate', popstateListener, { once: true });
        window.history.back();
      }
    } catch (error) {
      console.error('Navigation failed:', error);
      blockUnload.current = true; // 에러 발생 시 다시 방지 활성화
    }
  }, [pendingNavigation]);

  return {
    showDialog,
    setShowDialog,
    pendingNavigation,
    setPendingNavigation,
    blockUnload,
    router,
    originalPushRef,
  };
};
