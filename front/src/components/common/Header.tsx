import Image from 'next/image';
import Link from 'next/link';
import { getCookie } from '@/actions/auth';
import HeaderButton from './HeaderButton';

const Header = async () => {
  const accessToken = await getCookie('accessToken');
  return (
    <div className='sticky top-0 z-50 flex w-full items-center justify-between border-b border-gray-100 bg-white px-3 py-4 lg:px-6 lg:py-5'>
      <Link href='/'>
        <Image
          src='/assets/images/Firzzle.png'
          alt='logo'
          width={75}
          height={25}
          className='object-contain'
        />
      </Link>
      <HeaderButton accessToken={accessToken} />
    </div>
  );
};

export default Header;
