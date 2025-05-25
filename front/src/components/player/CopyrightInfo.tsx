const CopyrightInfo = () => {
  return (
    <section className='flex flex-col text-center text-xs text-gray-500 md:text-sm'>
      <p>
        본 서비스는 유튜브 영상에서 자막을 추출하여 비상업적·학습 목적에 한해
        제공합니다.
      </p>
      <p>
        해당 영상과 자막의 저작권은 원저작자(유튜브 채널 운영자)에게 있습니다.
      </p>
      <p>
        추출된 자막은 AI기술을 적용하여 요약, 분석 또는 대화형 응답 기능으로
        가공될 수 있으며, 이는 참고용으로만 제공됩니다.
      </p>
    </section>
  );
};

export default CopyrightInfo;
