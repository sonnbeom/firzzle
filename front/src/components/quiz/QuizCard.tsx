import { Button } from '@/components/ui/button';

interface QuizCardProps {
  selected: 'O' | 'X' | null;
  quizNo: string;
  question: string;
  onSelect: (value: 'O' | 'X') => void;
}

const QuizCard = ({ selected, quizNo, question, onSelect }: QuizCardProps) => {
  return (
    <div className='rounded-[14px] border border-gray-50 bg-white px-8 py-2 shadow-sm transition-all md:py-4'>
      {/* 문제 번호 */}
      <div className='text-center text-[24px] font-bold text-gray-900 md:text-[30px]'>
        {quizNo.padStart(2, '0')}
      </div>

      {/* 문제 */}
      <p className='text-md mb-6 text-left font-medium text-gray-900 md:mb-10 md:text-lg'>
        {question}
      </p>

      {/* 버튼들 */}
      <div className='mb-4 flex flex-row gap-5'>
        {/* O 버튼*/}
        <div className='w-full'>
          <Button
            variant='outline'
            className={`relative flex w-full items-center justify-center py-4 text-lg font-semibold md:text-xl ${
              selected === 'O'
                ? 'bg-blue-400 text-white hover:bg-blue-400'
                : 'border-blue-400 text-blue-400 hover:bg-blue-50 hover:text-blue-400'
            }`}
            onClick={() => onSelect('O')}
          >
            <span className='absolute left-4 text-2xl md:text-[36px]'>O</span>
            <span className='text-center'>맞아요</span>
          </Button>
        </div>

        {/* X 버튼*/}
        <div className='w-full'>
          <Button
            variant='outline'
            className={`relative flex w-full items-center justify-center border py-4 text-lg font-semibold md:text-xl ${
              selected === 'X'
                ? 'border-red-500 bg-red-500 text-white hover:bg-red-500'
                : 'border-red-500 text-red-500 hover:bg-red-50 hover:text-red-500'
            }`}
            onClick={() => onSelect('X')}
          >
            <span className='absolute left-4 text-2xl md:text-[36px]'>X</span>
            <span className='text-center'>아니에요</span>
          </Button>
        </div>
      </div>
    </div>
  );
};

export default QuizCard;
