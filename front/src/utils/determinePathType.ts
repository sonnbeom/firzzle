const determinePathType = (path: string): string | null => {
  // 정규표현식을 사용하여 패턴 매칭
  const summaryPattern = /^\/\d+$/;
  const quizPattern = /^\/\d+\/quiz$/;
  const snapReviewPattern = /^\/\d+\/snapreview$/;

  if (summaryPattern.test(path)) {
    return 'SUMMARY';
  } else if (quizPattern.test(path)) {
    return 'QUIZ_READ';
  } else if (snapReviewPattern.test(path)) {
    return 'SNAP_REVIEW_READ';
  }

  return null; // 매칭되는 패턴이 없는 경우
};
