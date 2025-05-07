import Link from 'next/link';
import Icons from './Icons';

interface TabButtonProps {
  title: string;
  isActive: boolean;
  route: string;
  iconId?: 'snapbook' | 'content';
  className?: string;
}

const TabButton = ({
  title,
  isActive,
  route,
  iconId,
  className,
}: TabButtonProps) => {
  return (
    <Link
      href={route}
      className={`flex w-full flex-col items-center gap-1 p-2 hover:bg-gray-50 ${className}`}
    >
      <button
        className={`${isActive ? 'font-semibold text-blue-400' : 'font-medium text-gray-700'} flex cursor-pointer items-center gap-2 text-xl`}
      >
        {iconId && (
          <Icons
            id={iconId}
            color={isActive ? 'text-blue-400' : 'text-gray-700'}
          />
        )}
        <p className='whitespace-nowrap'>{title}</p>
      </button>

      {!iconId && (
        <hr
          className={`${isActive ? 'border-blue-400' : 'border-none'} w-full border`}
        />
      )}
    </Link>
  );
};

export default TabButton;
