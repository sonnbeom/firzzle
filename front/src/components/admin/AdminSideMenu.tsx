import Image from 'next/image';
import { Button } from '../ui/button';

const AdminSideMenu = () => {
  return (
    <div className='items-col flex h-full w-full flex-col gap-3 bg-blue-50 p-3'>
      <div className='relative h-[80px] w-[115px]'>
        <Image
          src='/assets/images/Firzzle.png'
          alt='logo'
          fill
          sizes='100vw,'
          className='object-contain'
        />
      </div>
      <Button
        variant='default'
        className='w-full justify-start bg-white text-left text-blue-400'
      >
        요약
      </Button>
      <Button variant='text' className='w-full justify-start bg-transparent'>
        인기 영상
      </Button>
      <hr className='w-full border-gray-200' />
    </div>
  );
};

export default AdminSideMenu;
