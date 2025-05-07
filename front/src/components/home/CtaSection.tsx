import OAuthButton from './OAuthButton';

const CtaSection = () => {
  return (
    <section className='blue-400 bg-blue-400 py-24'>
      <div className='container mx-auto px-4'>
        <div className='mx-auto max-w-3xl space-y-8 text-center'>
          <h2 className='md:display-xl text-4xl leading-tight font-bold text-white'>
            지금 firzzle AI와 함께
            <br />
            학습 효율을 높여보세요
          </h2>

          <p className='text-xl text-white/90'>
            AI 기반 학습 경험의 혁신을 직접 경험해 보세요.
          </p>

          <OAuthButton
            url='https://kauth.kakao.com/oauth/authorize?'
            oauth='kakao'
            title='카카오로 시작하기'
            className='bg-white py-3 text-blue-400'
          />
        </div>
      </div>
    </section>
  );
};

export default CtaSection;
