import { useState, useCallback } from 'react';

interface UsePaginationProps {
  itemsPerPage: number;
  totalItems: number;
  onPageChange: (page: number) => void;
}

interface UsePaginationReturn {
  currentPage: number;
  totalPages: number;
  canGoPrev: boolean;
  canGoNext: boolean;
  showPagination: boolean;
  handlePrevPage: () => void;
  handleNextPage: () => void;
}

export function usePagination({
  itemsPerPage,
  totalItems,
  onPageChange,
}: UsePaginationProps): UsePaginationReturn {
  const [currentPage, setCurrentPage] = useState(1);
  const totalPages = Math.ceil(totalItems / itemsPerPage);

  const handlePrevPage = useCallback(() => {
    if (currentPage > 1) {
      const newPage = currentPage - 1;
      setCurrentPage(newPage);
      onPageChange(newPage);
    }
  }, [currentPage, onPageChange]);

  const handleNextPage = useCallback(() => {
    if (currentPage < totalPages) {
      const newPage = currentPage + 1;
      setCurrentPage(newPage);
      onPageChange(newPage);
    }
  }, [currentPage, totalPages, onPageChange]);

  const showPagination = totalItems > itemsPerPage;
  const canGoPrev = currentPage > 1;
  const canGoNext = currentPage < totalPages;

  return {
    currentPage,
    totalPages,
    canGoPrev,
    canGoNext,
    showPagination,
    handlePrevPage,
    handleNextPage,
  };
}
