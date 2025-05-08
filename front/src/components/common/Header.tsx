import Image from 'next/image';
import Link from 'next/link';
import HeaderButton from './HeaderButton';

const Header = () => {
  return (
    <div className='flex w-full items-center justify-between border-b border-gray-300 px-6 py-3 md:py-4 lg:py-6'>
      <Link
        href='/'
        className='relative h-full w-[60px] md:w-[80px] lg:w-[100px]'
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
      <HeaderButton />
    </div>
  );
};

export default Header;
