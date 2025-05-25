import { Button } from '@/components/ui/button';
import { QuizCardProps } from '@/types/quiz';

const QuizCard = ({
  selected,
  questionSeq,
  text,
  options,
  onSelect,
}: QuizCardProps) => {
  return (
    <div className='rounded-[14px] border border-gray-50 bg-white px-8 py-2 shadow-sm transition-all md:py-4'>
      {/* 문제 번호 */}
      <div className='text-center text-[24px] font-bold text-gray-900 md:text-[30px]'>
        {questionSeq.toString().padStart(2, '0')}
      </div>

      {/* 문제 */}
      <p className='text-md mb-6 text-left font-medium text-gray-900 md:mb-10 md:text-lg'>
        {text}
      </p>

      {/* 버튼들 */}
      <div className='mb-4 flex flex-row gap-5'>
        {/* O 버튼*/}
        <div className='w-full'>
          <Button
            key={options[0].optionSeq}
            variant='outline'
            className={`relative flex w-full items-center justify-center py-4 text-lg font-semibold md:text-xl ${
              selected === options[0].optionSeq
                ? 'bg-blue-400 text-white hover:bg-blue-400'
                : 'border-blue-400 text-blue-400 hover:bg-blue-50 hover:text-blue-400'
            }`}
            onClick={() => onSelect(options[0].optionSeq)}
          >
            <span className='absolute left-2 text-2xl md:left-4 md:text-[36px]'>
              O
            </span>
            <span className='text-center'>맞아요</span>
          </Button>
        </div>

        {/* X 버튼*/}
        <div className='w-full'>
          <Button
            key={options[1].optionSeq}
            variant='outline'
            className={`relative flex w-full items-center justify-center border py-4 text-lg font-semibold md:text-xl ${
              selected === options[1].optionSeq
                ? 'border-red-500 bg-red-500 text-white hover:bg-red-500'
                : 'border-red-500 text-red-500 hover:bg-red-50 hover:text-red-500'
            }`}
            onClick={() => onSelect(options[1].optionSeq)}
          >
            <span className='absolute left-2 text-xl md:left-4 md:text-[36px]'>
              X
            </span>
            <span className='text-center'>아니에요</span>
          </Button>
        </div>
      </div>
    </div>
  );
};

export default QuizCard;
