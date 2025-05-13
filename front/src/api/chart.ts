import { TransitionsResponse } from '@/types/chart';
import { externalApi } from './common/apiInstance';

// 기간별 기능 전환율 조회
export const getFunctionChangeRate = async (
  startDate: string,
  endDate: string,
) => {
  const { data } = await externalApi.get<TransitionsResponse>(
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
  const { data } = await externalApi.get<TransitionsResponse>(
    `/admin/strategy/conversion/transitions`,
    {
      params: {
        startDate,
        endDate,
      },
    },
  );
  return data;
};
