import OAuthButton from './OAuthButton';

const CtaSection = () => {
  return (
    <section className='bg-blue-400 py-16 md:py-24'>
      <div className='container mx-auto max-w-3xl space-y-6 text-center md:space-y-8'>
        <h2 className='text-2xl leading-tight font-bold text-white md:text-4xl'>
          지금 firzzle AI와 함께
          <br />
          학습 효율을 높여보세요
        </h2>

        <p className='text-lg text-white/90 md:text-xl'>
          AI 기반 학습 경험의 혁신을 직접 경험해 보세요.
        </p>

        <OAuthButton
          url='https://kauth.kakao.com/oauth/authorize?'
          oauth='kakao'
          title='카카오톡으로 시작하기'
          className='bg-white text-blue-400 md:py-3'
        />
      </div>
    </section>
  );
};

export default CtaSection;
