export const formatDate = (date: string) => {
  const yyyymmdd = date.split('T')[0];
  return yyyymmdd;
};
