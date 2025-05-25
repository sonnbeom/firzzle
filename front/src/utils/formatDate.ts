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

// 로컬타입 변환
export const formatToLocalDate = (date: Date) => {
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`;
};
