import { useState } from 'react';
import {
  getEducateChangeRate,
  getFunctionChangeRate,
  getLoginUserRate,
  getLikeSnapReviewRate,
  getSummaryLevelRate,
} from '@/api/chart';
import { TransitionsResponse } from '@/types/chart';

const useStrategyData = () => {
  const [loginRateData, setLoginRateData] =
    useState<TransitionsResponse | null>(null);
  const [educationStartData, setEducationStartData] =
    useState<TransitionsResponse | null>(null);
  const [functionChangeData, setFunctionChangeData] =
    useState<TransitionsResponse | null>(null);
  const [selectedChartData, setSelectedChartData] =
    useState<TransitionsResponse | null>(null);

  const fetchData = async (formattedStart: string, formattedEnd: string) => {
    try {
      const [loginRate, educationStart, functionChange] = await Promise.all([
        getLoginUserRate(formattedStart, formattedEnd),
        getEducateChangeRate(formattedStart, formattedEnd),
        getFunctionChangeRate(formattedStart, formattedEnd),
      ]);

      setLoginRateData(loginRate);
      setEducationStartData(educationStart);
      setFunctionChangeData(functionChange);
    } catch (error) {
      console.error('데이터 불러오기 실패:', error);
    }
  };

  const fetchSelectedData = async (
    option: string,
    formattedStart: string,
    formattedEnd: string,
  ) => {
    try {
      const data =
        option === '요약노트'
          ? await getSummaryLevelRate(formattedStart, formattedEnd)
          : await getLikeSnapReviewRate(formattedStart, formattedEnd);
      setSelectedChartData(data);
    } catch (error) {
      console.error('선택 차트 데이터 불러오기 실패:', error);
    }
  };

  return {
    data: {
      loginRateData,
      educationStartData,
      functionChangeData,
      selectedChartData,
    },
    actions: {
      fetchData,
      fetchSelectedData,
    },
  };
};

export default useStrategyData;
