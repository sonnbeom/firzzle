import Lecture from '@/components/recommend/Lecture';

const Recommend = async () => {
  return (
    <div className='relative min-h-screen w-full px-2 sm:px-4'>
      <div className='space-y-10 pb-20'>
        <Lecture />
      </div>
    </div>
  );
};

export default Recommend;
