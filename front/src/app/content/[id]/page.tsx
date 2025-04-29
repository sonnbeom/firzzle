import SummaryContainer from '@/components/summary/SummaryContainer';

const Summary = () => {
  const dummyData = {
    easyData: [
      {
        id: '1',
        title: '01 머신러닝 개요',
        description: '머신러닝의 개요를 알아보자',
        time: '01:35',
      },
    ],
    highData: [
      {
        id: '1',
        title: '01 머신러닝 개요',
        description: '머신러닝의 개요를 핵심만 알아보자',
        time: '01:35',
      },
    ],
  };

  return (
    <SummaryContainer
      easyData={dummyData.easyData}
      highData={dummyData.highData}
    />
  );
};

export default Summary;
