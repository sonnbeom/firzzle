import SummaryCard from './SummaryCard';

const SummaryContent = () => {
  const dummyData = [
    {
      id: '1',
      title: '01 머신러닝 개요',
      description: '머신러닝의 개요를 알아보자',
      time: '01:35',
    },
  ];

  return (
    <div className='flex w-full flex-col gap-7 px-2'>
      {dummyData &&
        dummyData.length > 0 &&
        dummyData.map((item) => (
          <SummaryCard
            key={item.id}
            title={item.title}
            description={item.description}
            time={item.time}
          />
        ))}
    </div>
  );
};

export default SummaryContent;
