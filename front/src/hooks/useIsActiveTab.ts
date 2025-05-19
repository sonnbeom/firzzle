import { usePathname } from 'next/navigation';

export const useIsActiveTab = () => {
  const pathname = usePathname();

  // pathname에서 contentSeq 추출
  const contentSeq = pathname.split('/').filter(Boolean)[1];

  const isActive = (path: string, exact: boolean = false) => {
    if (exact) {
      return pathname === path;
    }
    return pathname.startsWith(path);
  };

  return { contentSeq, isActive };
};
