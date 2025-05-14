import { TransitionsResponse } from '@/types/chart';
import { api } from './common/apiInstance';

// 기간별 기능 전환율 조회
export const getFunctionChangeRate = async (
  startDate: string,
  endDate: string,
) => {
  const { data } = await api.get<TransitionsResponse>(
    `/admin/strategy/transitions`,
    {
      params: {
        startDate,
        endDate,
      },
    },
  );
  return data;
};

// 기간별 학습 전환율 조회
export const getEducateChangeRate = async (
  startDate: string,
  endDate: string,
) => {
  const { data } = await api.get<TransitionsResponse>(
    `/admin/strategy/learning-rate`,
    {
      params: {
        startDate,
        endDate,
      },
    },
  );
  return data;
};

// 방문자 대비 로그인 전환률 조회
export const getLoginUserRate = async (startDate: string, endDate: string) => {
  const { data } = await api.get<TransitionsResponse>(
    `/admin/strategy/login-rate`,
    {
      params: {
        startDate,
        endDate,
      },
    },
  );
  return data;
};

// 요약 단계별 요약 선호 비율 - Dropdown
export const getSummaryLevelRate = async (
  startDate: string,
  endDate: string,
) => {
  const { data } = await api.get<TransitionsResponse>(
    `/admin/learning/summary-level`,
    {
      params: {
        startDate,
        endDate,
      },
    },
  );
  return data;
};

// 스냅리뷰 작성 선호도 측정 - Dropdown
export const getLikeSnapReviewRate = async (
  startDate: string,
  endDate: string,
) => {
  const { data } = await api.get<TransitionsResponse>(
    `/admin/learning/snap-review`,
    {
      params: {
        startDate,
        endDate,
      },
    },
  );
  return data;
};
