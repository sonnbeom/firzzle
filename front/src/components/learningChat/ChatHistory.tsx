import ChatBubble from './ChatBubble';

const ChatHistory = () => {
  const dummyData = [
    {
      id: '1',
      userId: '1',
      text: '머신러닝이 뭐야?',
      mode: '학습모드',
    },
    {
      id: '2',
      userId: '0',
      text: '머신러닝(Machine Learning)은 컴퓨터가 명시적으로 프로그래밍되지 않아도 데이터에서 스스로 학습하여 문제를 해결하는 기술이야.\n 조금 더 쉽게 말하면: 경험(데이터)을 통해 성능을 향상시키는 컴퓨터 알고리즘이라고 할 수 있어.',
      mode: '학습모드',
    },
    {
      id: '3',
      userId: '0',
      text: '머신러닝과 딥러닝은 어떻게 다른가요?',
      mode: '시험모드',
    },
    {
      id: '4',
      userId: '1',
      text: '머신러닝은 데이터를 기반으로 학습하여 예측하거나 분류하는 인공지능의 한 분야입니다. 딥러닝은 머신러닝의 하위 분야로, 인공신경망을 이용해 복잡한 패턴을 자동으로 학습합니다. 즉, 딥러닝은 머신러닝의 한 방식이며, 특히 이미지나 자연어 처리처럼 데이터가 크고 복잡한 문제에 적합합니다.',
      mode: '시험모드',
    },
  ];

  return (
    <div className='flex h-full w-full flex-col gap-4'>
      {dummyData.map((item) => (
        <ChatBubble key={item.id} userId={item.userId} text={item.text} />
      ))}
    </div>
  );
};

export default ChatHistory;
