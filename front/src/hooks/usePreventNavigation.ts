import { useState, useEffect, useCallback } from 'react';

interface UsePreventNavigationReturn {
  showDialog: boolean;
  setShowDialog: (show: boolean) => void;
  confirmNavigation: () => void;
}

export const usePreventNavigation = (
  shouldPrevent: boolean,
): UsePreventNavigationReturn => {
  const [showDialog, setShowDialog] = useState(false);
  const [pendingUrl, setPendingUrl] = useState<string | null>(null);
  const [isBack, setIsBack] = useState(false);

  // 뒤로가기 처리 함수
  const handlePopState = useCallback((e: PopStateEvent) => {
    e.preventDefault();
    setIsBack(true);
    setShowDialog(true);
  }, []);

  // 뒤로가기 방지
  useEffect(() => {
    if (!shouldPrevent) return;

    window.history.pushState(null, '', window.location.href);
    window.addEventListener('popstate', handlePopState);

    return () => {
      window.removeEventListener('popstate', handlePopState);
    };
  }, [shouldPrevent, handlePopState]);

  // Link 클릭 방지
  useEffect(() => {
    if (!shouldPrevent) return;

    const handleClick = (e: MouseEvent) => {
      const target = e.target as HTMLElement;
      const linkElement = target.closest('a');

      if (linkElement && linkElement.getAttribute('href')) {
        e.preventDefault();
        const url = linkElement.getAttribute('href') || '';
        setIsBack(false);
        setPendingUrl(url);
        setShowDialog(true);
      }
    };

    document.addEventListener('click', handleClick, true);

    return () => {
      document.removeEventListener('click', handleClick, true);
    };
  }, [shouldPrevent]);

  // 이동 허용
  const confirmNavigation = useCallback(() => {
    setShowDialog(false);

    if (isBack) {
      // 뒤로가기 실행
      window.removeEventListener('popstate', handlePopState);
      window.history.back();
    } else if (pendingUrl) {
      // 일반 페이지 이동
      window.location.href = pendingUrl;
    }

    setIsBack(false);
    setPendingUrl(null);
  }, [isBack, pendingUrl, handlePopState]);

  // Dialog 취소
  const handleDialogClose = (open: boolean) => {
    setShowDialog(open);
    if (!open) {
      setIsBack(false);
      setPendingUrl(null);
      // Dialog가 닫히면 다음 뒤로가기를 다시 처리하도록 history state 초기화
      if (shouldPrevent) {
        window.history.pushState(null, '', window.location.href);
      }
    }
  };

  return {
    showDialog,
    setShowDialog: handleDialogClose,
    confirmNavigation,
  };
};
