import Image from 'next/image';
import { Button } from '@/components/ui/button';

const HeroSection = () => {
  return (
    <section className='bg-gradient-to-r from-[#E8EDFF] to-[#F8F9FF] py-16 md:py-24'>
      {/* Decorative elements */}
      <div className='-top-- absolute right-20 h-64 w-64 rounded-full bg-blue-300/30 blur-3xl'></div>
      <div className='-top-- absolute -left-4 h-80 w-80 rounded-full bg-blue-200/20 blur-3xl'></div>

      <div className='container mx-auto px-4'>
        <div className='flex flex-col items-center gap-12 md:flex-row'>
          <div className='flex-1 space-y-8'>
            <h1 className='text-lg leading-tight font-bold tracking-tight text-gray-950 md:text-6xl'>
              영상 학습을
              <br />
              <span className='text-6xl font-bold text-blue-400'>
                AI로 더 스마트하게
              </span>
            </h1>

            <p className='font-regular max-w-xl text-xl text-gray-600'>
              firzzle AI는 영상 콘텐츠를 지능적으로 분석하여 요약, 퀴즈,
              인사이트를 제공하는 차세대 학습 도구입니다. 이제 몰입도 높은 학습
              경험을 경험해 보세요.
            </p>

            <div className='flex flex-wrap gap-4 pt-4'>
              <Button size='lg' className='px-6'>
                카카오톡으로 시작하기
              </Button>
              <Button
                size='lg'
                variant='outline'
                className='bg-transparent px-6'
              >
                기능 둘러보기
              </Button>
            </div>
          </div>

          <div className='relative flex-1'>
            <div className='shadow-primary-500/20 relative z-10 overflow-hidden rounded-xl shadow-2xl'>
              <Image
                src='/assets/images/dashboard-mockup.png'
                alt='firzzle AI 대시보드'
                width={600}
                height={450}
                className='h-auto w-full'
              />
            </div>
          </div>
        </div>
      </div>
    </section>
  );
};

export default HeroSection;
