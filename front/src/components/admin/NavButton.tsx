'use client';

import Link from 'next/link';
import { usePathname } from 'next/navigation';
import { Button } from '../ui/button';

interface NavButtonProps {
  href: string;
  children: React.ReactNode;
}

const NavButton = ({ href, children }: NavButtonProps) => {
  const pathname = usePathname();
  const isActive = pathname === href;

  return (
    <Link href={href} className='flex-1 lg:w-full lg:flex-none'>
      <Button
        variant={isActive ? 'default' : 'text'}
        className={
          'h-full w-full justify-center px-2 text-sm lg:justify-start lg:px-4 lg:text-base ' +
          (isActive ? 'bg-white text-blue-400' : 'bg-transparent')
        }
      >
        {children}
      </Button>
    </Link>
  );
};

export default NavButton;
