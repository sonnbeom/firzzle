export const determinePathType = (path: string) => {
  const splitPath = path.split('/').pop();

  if (!isNaN(Number(splitPath))) {
    return 'SUMMARY';
  }

  if (splitPath === 'quiz') {
    return 'QUIZ_READ';
  }

  if (splitPath === 'snapreview') {
    return 'SNAP_REVIEW_READ';
  }
};
