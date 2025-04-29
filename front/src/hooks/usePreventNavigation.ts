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
  handleDialogChange: (open: boolean) => void;
  pendingNavigation: PendingNavigationInfo | null;
}

/**
 * 페이지 이탈 방지를 위한 커스텀 훅
 * @param shouldPrevent 이탈 방지 여부를 결정하는 조건
 * @returns 다이얼로그 관련 상태와 핸들러
 */
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

  // 뒤로가기 감지 및 처리 함수
  const handlePopState = useCallback(() => {
    if (shouldPrevent) {
      window.history.pushState(null, '', window.location.href);
      setPendingNavigation({ type: 'back' });
      setShowDialog(true);
    }
  }, [shouldPrevent]);

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
      setPendingNavigation({
        type: 'push',
        url: href,
        options,
      });
      setShowDialog(true);
      return Promise.resolve(false);
    };

    window.addEventListener('popstate', handlePopState);
    window.history.pushState(null, '', window.location.href);

    return () => {
      window.removeEventListener('popstate', handlePopState);
      if (originalPushRef.current) router.push = originalPushRef.current;
      blockUnload.current = false;
    };
  }, [shouldPrevent, router, handlePopState]);

  // 경고 다이얼로그 상태 관리
  const handleDialogChange = (open: boolean) => {
    setShowDialog(open);
    if (!open) {
      setPendingNavigation(null);
    }
  };

  return {
    showDialog,
    handleDialogChange,
    pendingNavigation,
  };
};
