import Image from 'next/image';
import Link from 'next/link';
import { getCookie } from '@/actions/auth';
import HeaderButton from './HeaderButton';

const Header = async () => {
  const accessToken = await getCookie('accessToken');
  return (
    <div className='sticky top-0 z-50 flex w-full items-center justify-between border-b border-gray-300 bg-white px-6 py-4 lg:py-5'>
      <Link
        href='/'
        className='relative h-[25px] w-[83px] md:h-[30px] md:w-[100px] lg:h-[35px] lg:w-[117px]'
      >
        <Image
          src='/assets/images/Firzzle.png'
          alt='logo'
          fill
          sizes='100vx'
          priority
          className='object-contain'
        />
      </Link>
      <HeaderButton accessToken={accessToken} />
    </div>
  );
};

export default Header;
