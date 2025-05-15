import { TransitionsResponse } from '@/types/chart';

// 기간별 기능 전환율 조회
export const getFunctionChangeRate = async (
  startDate: string,
  endDate: string,
): Promise<TransitionsResponse> => {
  const params = new URLSearchParams();
  params.append('startDate', startDate);
  params.append('endDate', endDate);
  params.append('endpoint', 'transitions');

  const response = await fetch(`/api/admin?${params}`);
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
): Promise<TransitionsResponse> => {
  const params = new URLSearchParams();
  params.append('startDate', startDate);
  params.append('endDate', endDate);
  params.append('endpoint', 'learning-rate');

  const response = await fetch(`/api/admin?${params}`);
  const data = await response.json();

  if (response.status !== 200) {
    throw new Error(data.message);
  }

  return data;
};

// 방문자 대비 로그인 전환률 조회
export const getLoginUserRate = async (
  startDate: string,
  endDate: string,
): Promise<TransitionsResponse> => {
  const params = new URLSearchParams();
  params.append('startDate', startDate);
  params.append('endDate', endDate);
  params.append('endpoint', 'login-rate');

  const response = await fetch(`/api/admin?${params}`);
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
): Promise<TransitionsResponse> => {
  const params = new URLSearchParams();
  params.append('startDate', startDate);
  params.append('endDate', endDate);
  params.append('endpoint', 'summary-level');

  const response = await fetch(`/api/admin?${params}`);
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
): Promise<TransitionsResponse> => {
  const params = new URLSearchParams();
  params.append('startDate', startDate);
  params.append('endDate', endDate);
  params.append('endpoint', 'snap-review');

  const response = await fetch(`/api/admin?${params}`);
  const data = await response.json();

  if (response.status !== 200) {
    throw new Error(data.message);
  }

  return data;
};
