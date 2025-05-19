import { memo } from 'react';

const StepOne = () => {
  const EXAMPLE_URL = 'https://example.com/lecture/video';

  const StepNumber = memo(function StepNumber() {
    return (
      <div
        className='mr-4 flex h-12 w-12 items-center justify-center rounded-full bg-blue-400 font-semibold text-white'
        aria-hidden='true'
      >
        1
      </div>
    );
  });

  const UrlInput = memo(function UrlInput() {
    return (
      <input
        type='text'
        placeholder={EXAMPLE_URL}
        readOnly
        className='pointer-events-none mr-2 flex-grow cursor-default rounded-md border border-gray-300 bg-gray-100 p-3 text-gray-700'
        aria-label='학습할 영상 URL 입력칸'
      />
    );
  });

  const SubmitButton = memo(function SubmitButton() {
    return (
      <button
        type='button'
        className='rounded-md bg-blue-400 px-4 py-3 font-medium text-white transition-colors hover:bg-blue-500 focus:ring-2 focus:ring-blue-400 focus:ring-offset-2 focus:outline-none'
        aria-label='영상 URL 입력 확인'
      >
        확인
      </button>
    );
  });

  return (
    <div className='container mx-auto my-8 px-4'>
      <div className='rounded-lg border border-gray-100 bg-white p-6 shadow-sm transition-shadow hover:shadow-md'>
        <div className='mb-4 flex items-start'>
          <StepNumber />
          <div className='w-full'>
            <h3 className='mb-2 text-xl font-bold text-gray-900'>
              학습할 영상 URL 입력
            </h3>
            <p className='mb-4 text-gray-600'>
              학습하고 싶은 온라인 강의 URL을 입력해주세요. URL을 입력하면 AI가
              영상을 분석하여 학습 자료를 준비합니다.
              <br />
              유튜브 등 원하는 플랫폼의 영상을 학습할 수 있어요.
            </p>

            <form
              className='flex items-center'
              onSubmit={(e) => e.preventDefault()}
            >
              <UrlInput />
              <SubmitButton />
            </form>
          </div>
        </div>
      </div>
    </div>
  );
};

export default memo(StepOne);
