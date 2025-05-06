import Link from 'next/link';
import Icons from './Icons';

interface TabButtonProps {
  title: string;
  isActive: boolean;
  route: string;
  iconId?: 'snapbook' | 'content';
}

const TabButton = ({ title, isActive, route, iconId }: TabButtonProps) => {
  return (
    <Link href={route} className='flex flex-col items-center gap-1'>
      <button
        className={`${isActive ? 'font-semibold text-blue-400' : 'font-medium text-gray-700 hover:bg-gray-50'} text-xl`}
      >
        {iconId && (
          <Icons
            id={iconId}
            color={isActive ? 'text-blue-400' : 'text-gray-700'}
          />
        )}
        {title}
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
