'use client';

import { useEffect, useState } from 'react';

import {
  getEducateChangeRate,
  getFunctionChangeRate,
  getLoginUserRate,
  getLikeSnapReviewRate,
  getSummaryLevelRate,
} from '@/api/chart';
import CurveGraphCard from '@/components/admin/CurveGraphCard';
import DateRangeSelector from '@/components/admin/DateRangeSelector';
import BasicDropDown from '@/components/common/BasicDropDown';
import { TransitionsResponse, DateRangeData } from '@/types/chart';

const StrategyBoardPage = () => {
  const [startDate, setStartDate] = useState<Date>(new Date());
  const [endDate, setEndDate] = useState<Date>(new Date());

  // 상단 3개 차트 데이터
  const [loginRateData, setLoginRateData] =
    useState<TransitionsResponse | null>(null);
  const [educationStartData, setEducationStartData] =
    useState<TransitionsResponse | null>(null);
  const [functionChangeData, setFunctionChangeData] =
    useState<TransitionsResponse | null>(null);

  // 하단 선택형 차트 데이터
  const [selectedOption, setSelectedOption] = useState<string>('요약노트'); // '요약노트'
  const [selectedChartData, setSelectedChartData] =
    useState<TransitionsResponse | null>(null);

  // 드롭다운 옵션 변경 시
  const handleOptionChange = async (option: string) => {
    setSelectedOption(option);
    try {
      const data =
        option === '요약노트' // '요약노트'
          ? await getSummaryLevelRate(
              startDate.toISOString(),
              endDate.toISOString(),
            )
          : await getLikeSnapReviewRate(
              startDate.toISOString(),
              endDate.toISOString(),
            );
      setSelectedChartData(data);
    } catch (error) {
      console.error('선택 차트 데이터 불러오기 실패:', error);
    }
  };

  // 날짜 변경 시 모든 데이터 업데이트
  const handleDateChange = async ({
    startDate: newStart,
    endDate: newEnd,
    formattedStart,
    formattedEnd,
  }: DateRangeData) => {
    setStartDate(newStart);
    setEndDate(newEnd);

    try {
      const [loginData, educationData, functionData] = await Promise.all([
        getLoginUserRate(formattedStart, formattedEnd),
        getEducateChangeRate(formattedStart, formattedEnd),
        getFunctionChangeRate(formattedStart, formattedEnd),
      ]);

      setLoginRateData(loginData);
      setEducationStartData(educationData);
      setFunctionChangeData(functionData);

      // 선택된 차트 데이터도 업데이트
      const selectedData =
        selectedOption === '요약노트'
          ? await getSummaryLevelRate(formattedStart, formattedEnd)
          : await getLikeSnapReviewRate(formattedStart, formattedEnd);
      setSelectedChartData(selectedData);
    } catch (error) {
      console.error('데이터 불러오기 실패:', error);
    }
  };

  // 초기 데이터 로드
  useEffect(() => {
    handleDateChange({
      startDate: new Date(),
      endDate: new Date(),
      formattedStart: new Date().toISOString(),
      formattedEnd: new Date().toISOString(),
    });
  }, []);

  return (
    <div className='flex flex-col gap-6 p-6'>
      <DateRangeSelector
        onChange={handleDateChange}
        initialStartDate={startDate}
        initialEndDate={endDate}
      />

      {/* 상단 3개 차트 */}
      <div className='grid grid-cols-1 gap-6 lg:grid-cols-3'>
        {loginRateData && (
          <CurveGraphCard
            title='로그인 완료율'
            description='전체 방문자 수 대비 로그인 완료한 사용자 수'
            dataSets={[
              {
                label: '로그인 완료율',
                data: loginRateData.map(({ date, transitions }) => {
                  const visit = transitions.VISIT;
                  const login = transitions.LOGIN;
                  const rate = visit ? (login / visit) * 100 : 0;
                  return { x: date, y: rate };
                }),
              },
            ]}
          />
        )}

        {educationStartData && (
          <CurveGraphCard
            title='학습 시작률'
            description='학습 시작 전환율'
            dataSets={[
              {
                label: '학습 시작률',
                data: educationStartData.map(({ date, transitions }) => {
                  return { x: date, y: transitions.START_LEARNING || 0 };
                }),
              },
            ]}
          />
        )}

        {functionChangeData && (
          <CurveGraphCard
            title='기능 전환율'
            description='기능별 전환율'
            dataSets={[
              {
                label: 'SUMMARY=>QUIZ',
                data: functionChangeData.map(({ date, transitions }) => {
                  return {
                    x: date,
                    y: transitions['SUMMARY\u003EQUIZ_READ'] || 0,
                  };
                }),
              },
              {
                label: 'QUIZ=>SNAP_REVIEW',
                data: functionChangeData.map(({ date, transitions }) => {
                  return {
                    x: date,
                    y: transitions['QUIZ_READ\u003ESNAP_REVIEW_READ'] || 0,
                  };
                }),
              },
              {
                label: 'SNAP_REVIEW=>RECOMMEND',
                data: functionChangeData.map(({ date, transitions }) => {
                  return {
                    x: date,
                    y: transitions['SNAP_REVIEW_READ=>RECOMMEND'] || 0,
                  };
                }),
              },
            ]}
          />
        )}
      </div>

      {/* 하단 선택형 차트 */}
      <div className='flex flex-col gap-4'>
        <BasicDropDown
          items={[
            { value: '요약노트', label: '요약노트' },
            { value: '스냅리뷰', label: '스냅리뷰' },
          ]}
          defaultValue={selectedOption}
          onChange={handleOptionChange}
        />

        {selectedChartData && (
          <CurveGraphCard
            dataSets={
              selectedOption === '요약노트'
                ? [
                    {
                      label: '',
                      data: selectedChartData.map(({ date, transitions }) => ({
                        x: date,
                        y: transitions.DIFFICULT || 0,
                      })),
                    },
                    {
                      label: '',
                      data: selectedChartData.map(({ date, transitions }) => ({
                        x: date,
                        y: transitions.EASY || 0,
                      })),
                    },
                  ]
                : [
                    {
                      label: '',
                      data: selectedChartData.map(({ date, transitions }) => ({
                        x: date,
                        y:
                          (transitions.SNAP_REVIEW_INPUT /
                            transitions.START_LEARNING) *
                            100 || 0,
                      })),
                    },
                  ]
            }
          />
        )}
      </div>
    </div>
  );
};

export default StrategyBoardPage;
