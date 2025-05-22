import { useState } from 'react';
import {
  getEducateChangeRate,
  getFunctionChangeRate,
  getLoginUserRate,
  getLikeSnapReviewRate,
  getSummaryLevelRate,
} from '@/api/chart';
import BasicToaster from '@/components/common/BasicToaster';
import { TransitionsResponse } from '@/types/chart';

const useStrategyData = () => {
  const [isMainLoading, setIsMainLoading] = useState(false);
  const [isSelectedLoading, setIsSelectedLoading] = useState(false);
  const [loginRateData, setLoginRateData] =
    useState<TransitionsResponse | null>(null);
  const [educationStartData, setEducationStartData] =
    useState<TransitionsResponse | null>(null);
  const [functionChangeData, setFunctionChangeData] =
    useState<TransitionsResponse | null>(null);
  const [selectedChartData, setSelectedChartData] =
    useState<TransitionsResponse | null>(null);

  const fetchData = async (formattedStart: string, formattedEnd: string) => {
    if (isMainLoading) return;
    setIsMainLoading(true);
    try {
      // API 호출을 순차적으로 처리하여 서버 부하 감소
      const loginRate = await getLoginUserRate(formattedStart, formattedEnd);
      await new Promise((resolve) => setTimeout(resolve, 100));

      const educationStart = await getEducateChangeRate(
        formattedStart,
        formattedEnd,
      );
      await new Promise((resolve) => setTimeout(resolve, 100));

      const functionChange = await getFunctionChangeRate(
        formattedStart,
        formattedEnd,
      );

      setLoginRateData(loginRate);
      setEducationStartData(educationStart);
      setFunctionChangeData(functionChange);
    } catch (error) {
      BasicToaster.error(error.message, {
        id: 'strategy',
        duration: 2000,
      });
      setLoginRateData(null);
      setEducationStartData(null);
      setFunctionChangeData(null);
    } finally {
      setIsMainLoading(false);
    }
  };

  const fetchSelectedData = async (
    option: string,
    formattedStart: string,
    formattedEnd: string,
  ) => {
    if (isSelectedLoading) return;
    setIsSelectedLoading(true);
    try {
      await new Promise((resolve) => setTimeout(resolve, 200));
      const data =
        option === '요약노트'
          ? await getSummaryLevelRate(formattedStart, formattedEnd)
          : await getLikeSnapReviewRate(formattedStart, formattedEnd);
      setSelectedChartData(data);
    } catch (error) {
      BasicToaster.error(error.message, {
        id: 'strategy',
        duration: 2000,
      });
      setSelectedChartData(null);
    } finally {
      setIsSelectedLoading(false);
    }
  };

  return {
    data: {
      loginRateData,
      educationStartData,
      functionChangeData,
      selectedChartData,
    },
    actions: { fetchData, fetchSelectedData },
    isLoading: isMainLoading || isSelectedLoading,
  };
};

export default useStrategyData;
