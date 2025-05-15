// 기간별 기능 전환율 조회
export const getFunctionChangeRate = async (
  startDate: string,
  endDate: string,
) => {
  const response = await fetch(
    `/api/admin?endpoint=transitions&startDate=${startDate}&endDate=${endDate}`,
  );
  const data = await response.json();

  if (response.status !== 200) {
    throw new Error(data.message);
  }

  return data;
};

// 기간별 학습 전환율 조회
export const getEducateChangeRate = async (
  startDate: string,
  endDate: string,
) => {
  const response = await fetch(
    `/api/admin?endpoint=learning-rate&startDate=${startDate}&endDate=${endDate}`,
  );
  const data = await response.json();

  if (response.status !== 200) {
    throw new Error(data.message);
  }

  return data;
};

// 방문자 대비 로그인 전환률 조회
export const getLoginUserRate = async (startDate: string, endDate: string) => {
  const response = await fetch(
    `/api/admin?endpoint=login-rate&startDate=${startDate}&endDate=${endDate}`,
  );
  const data = await response.json();

  if (response.status !== 200) {
    throw new Error(data.message);
  }

  return data;
};

// 요약 단계별 요약 선호 비율 - Dropdown
export const getSummaryLevelRate = async (
  startDate: string,
  endDate: string,
) => {
  const response = await fetch(
    `/api/admin?endpoint=summary-level&startDate=${startDate}&endDate=${endDate}`,
  );
  const data = await response.json();

  if (response.status !== 200) {
    throw new Error(data.message);
  }

  return data;
};

// 스냅리뷰 작성 선호도 측정 - Dropdown
export const getLikeSnapReviewRate = async (
  startDate: string,
  endDate: string,
) => {
  const response = await fetch(
    `/api/admin?endpoint=snap-review&startDate=${startDate}&endDate=${endDate}`,
  );
  const data = await response.json();

  if (response.status !== 200) {
    throw new Error(data.message);
  }

  return data;
};
