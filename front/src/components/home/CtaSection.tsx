import { Button } from '@/components/ui/button';

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

          <Button
            size='lg'
            variant='secondary'
            className='bg-white px-8 py-6 text-lg font-semibold text-blue-400 transition-transform'
          >
            카카오톡으로 시작하기
          </Button>
        </div>
      </div>
    </section>
  );
};

export default CtaSection;
