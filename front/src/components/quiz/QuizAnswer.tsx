import { QuizAnswerProps } from '@/types/quiz';
import TimeStamp from '../common/TimeStamp';

const QuizAnswer = ({
  questionSeq,
  text,
  correct,
  explanation,
  timestamp,
}: QuizAnswerProps) => {
  return (
    <div className='space-y-6 py-4 md:py-6'>
      {/* 문제 번호 + 질문 */}
      <div className='flex items-start gap-4'>
        {/* 번호 */}
        <div className='text-[24px] font-bold text-gray-900 md:text-[30px]'>
          {questionSeq ? questionSeq.toString().padStart(2, '0') : '00'}
        </div>

        {/* 질문 */}
        <p className='text-md font-medium text-gray-900 md:text-lg'>{text}</p>
      </div>

      {/* 정답 여부 + 해설 */}
      <div className='rounded-xl border border-gray-50 bg-white px-6 py-3 shadow-sm transition-all md:py-[16px]'>
        <div
          className={`mb-2 text-center text-lg font-bold md:mb-[18px] md:text-xl ${correct ? 'text-blue-400' : 'text-red-500'}`}
        >
          {correct ? '정답이에요!' : '오답이에요!'}
        </div>
        <p className='text-md mb-4 leading-relaxed text-gray-900 md:text-lg'>
          {explanation}
        </p>
        <div className='flex justify-end'>
          <TimeStamp time={timestamp} type='review' />
        </div>
      </div>
    </div>
  );
};

export default QuizAnswer;
