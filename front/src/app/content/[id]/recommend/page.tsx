import Expert from '@/components/recommend/Expert';
import Lecture from '@/components/recommend/Lecture';

const Recommend = async () => {
  return (
    <div className='relative w-full px-2 sm:px-4'>
      <Lecture />
      <Expert />
    </div>
  );
};

export default Recommend;
