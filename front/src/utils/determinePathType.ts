export const determinePathType = (path: string) => {
  const splitPath = path.split('/').pop();

  if (!isNaN(Number(splitPath))) {
    return 'SUMMARY';
  }

  if (splitPath === 'quiz') {
    return 'QUIZ';
  }

  if (splitPath === 'snapreview') {
    return 'SNAP_REVIEW';
  }

  if (splitPath === 'recommend') {
    return 'RECOMMEND';
  }

  return null;
};
