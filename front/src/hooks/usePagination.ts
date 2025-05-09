import { useState, useMemo } from 'react';

// 페이지네이션 훅의 입력 파라미터
interface UsePaginationProps<T> {
  items: T[];
  itemsPerPage: number;
}

//페이지네이션 훅의 반환값
interface UsePaginationReturn<T> {
  currentPage: number;
  totalPages: number;
  visibleItems: T[];
  canGoPrev: boolean;
  canGoNext: boolean;
  showPagination: boolean;
  handlePrevPage: () => void;
  handleNextPage: () => void;
}

// 페이지네이션 기능을 제공하는 커스텀 훅
export function usePagination<T>({
  items,
  itemsPerPage,
}: UsePaginationProps<T>): UsePaginationReturn<T> {
  const [currentPage, setCurrentPage] = useState(1);
  const totalPages = Math.ceil(items.length / itemsPerPage);

  // 메모이제이션
  const visibleItems = useMemo(() => {
    const startIndex = (currentPage - 1) * itemsPerPage;
    return items.slice(startIndex, startIndex + itemsPerPage);
  }, [items, currentPage, itemsPerPage]);

  // 이전 페이지로 이동
  const handlePrevPage = () => {
    if (currentPage > 1) {
      setCurrentPage(currentPage - 1);
    }
  };

  // 다음 페이지로 이동
  const handleNextPage = () => {
    if (currentPage < totalPages) {
      setCurrentPage(currentPage + 1);
    }
  };

  // 페이지네이션 UI 표시 여부
  const showPagination = items.length > itemsPerPage;
  const canGoPrev = currentPage > 1;
  const canGoNext = currentPage < totalPages;

  return {
    currentPage,
    totalPages,
    visibleItems,
    canGoPrev,
    canGoNext,
    showPagination,
    handlePrevPage,
    handleNextPage,
  };
}
