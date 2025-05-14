export const determinePathType = (path: string): string | null => {
  // URL에서 content 이후의 경로 추출
  let contentPath = path;
  try {
    if (path.includes('http')) {
      const urlObj = new URL(path);
      const parts = urlObj.pathname.split('content');
      contentPath = parts.length > 1 ? parts[1] : '';
    }
  } catch {
    // URL 파싱 실패시 원래 path 사용
    contentPath = path;
  }

  // 정규표현식을 사용하여 패턴 매칭
  const summaryPattern = /^\/\d+$/;
  const quizPattern = /^\/\d+\/quiz$/;
  const snapReviewPattern = /^\/\d+\/snapreview$/;

  if (summaryPattern.test(contentPath)) {
    return 'SUMMARY';
  } else if (quizPattern.test(contentPath)) {
    return 'QUIZ_READ';
  } else if (snapReviewPattern.test(contentPath)) {
    return 'SNAP_REVIEW_READ';
  }

  return null; // 매칭되는 패턴이 없는 경우
};
