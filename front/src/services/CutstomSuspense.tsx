'use client';

import { useIsMounted } from '@toss/react';
import { ComponentProps, Suspense } from 'react';

interface CustomSuspenseProps {
  props: ComponentProps<typeof Suspense>;
}

// 서버 사이드 렌더링 과정에서는 Suspense가 동작하지 않도록 커스텀
const CustomSuspense = ({ props }: CustomSuspenseProps) => {
  const { fallback } = props;
  const isMounted = useIsMounted(); // 서버 사이드/클라리언트 사이드 렌더링 구분

  // 클라이언트 사이드 렌더링의 경우 Suspense 컴포넌트 사용
  if (isMounted) {
    return <Suspense {...props} />;
  }

  // 서버 사이드 렌더링의 경우 fallback 컴포넌트 사용
  return <>{fallback}</>;
};

export default CustomSuspense;
