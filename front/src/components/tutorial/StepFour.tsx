import { memo } from 'react';

const EXAMPLE_QUIZ = {
  number: '01',
  question:
    '머신러닝에서 모델이 학습 데이터에 과도하게 맞춰져 새로운 데이터에 대한 예측 성능이 떨어지는 현상을 과소적합 (underfitting)이라고 한다.',
} as const;

const StepNumber = memo(function StepNumber() {return (
  <div
    className='mr-4 flex h-12 w-12 items-center justify-center rounded-full bg-blue-400 font-semibold text-white'
    aria-hidden='true'
  >
    4
  </div>
);});

const QuizButton = memo(
  ({ type, children }: { type: 'O' | 'X'; children: React.ReactNode }) => (
    <button
      type='button'
      className={`flex flex-1 items-center justify-center rounded-lg border py-4 font-medium transition-all hover:bg-opacity-10 ${type === 'O' ? 'border-blue-600 text-blue-600 hover:bg-blue-600' : 'border-red-400 text-red-400 hover:bg-red-400'}`}
      aria-label={`${type === 'O' ? '맞음' : '틀림'} 선택하기`}
    >
      <span className='mr-3 text-xl font-bold'>{type}</span> {children}
    </button>
  )
);

QuizButton.displayName = 'QuizButton';

const StepFour = () => {
  return (
    <div className='container mx-auto my-8 px-4'>
      <div className='rounded-lg border border-gray-100 bg-white p-6 shadow-sm'>
        <div className='mb-4 flex items-start'>
          <StepNumber />

          <div className='w-full'>
            <h3 className='mb-2 text-xl font-bold'>OX 퀴즈로 지식 체크</h3>
            <p className='mb-4 text-gray-600'>
              영상 내용을 기반으로 생성된 OX 퀴즈를 풀어 학습 내용을 확인할 수
              있습니다. 각 문제는 영상의 주요 개념과 관련되어 있으며, 답변
              후에는 정답 여부와 해설, 그리고 관련 영상 구간의 타임스탬프가
              제공됩니다. 틀린 부분은 다시 확인하여 완벽하게 학습하세요!
            </p>

            <div className='rounded-lg bg-gray-50 p-6 shadow-sm transition-all hover:bg-gray-100'>
              <div className='mb-8 text-center'>
                <div
                  className='mb-2 text-3xl font-bold text-blue-600'
                  aria-label={`문제 ${EXAMPLE_QUIZ.number}번`}
                >
                  {EXAMPLE_QUIZ.number}
                </div>
                <p className='text-gray-700'>{EXAMPLE_QUIZ.question}</p>
              </div>

              <div className='flex gap-4'>
                <QuizButton type='O'>맞아요</QuizButton>
                <QuizButton type='X'>아니에요</QuizButton>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default memo(StepFour);
