export const formatDate = (dateString: string) => {
  return dateString.split(' ')[0];
};

export const formatDateToKorean = (dateString: string) => {
  return new Date(formatDate(dateString)).toLocaleDateString('ko-KR', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
  });
};
