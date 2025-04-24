import { Button } from '../ui/button';
import Icons from './Icons';

interface TabButtonProps {
  title: string;
  isActive: boolean;
  onClick: () => void;
  iconId?: 'snapbook' | 'content';
}

const TabButton = ({ title, isActive, onClick, iconId }: TabButtonProps) => {
  return (
    <div className='flex flex-col items-center gap-1'>
      <Button
        variant={isActive ? 'tabactive' : 'tabinactive'}
        onClick={onClick}
      >
        {iconId && (
          <Icons
            id={iconId}
            color={isActive ? 'text-blue-400' : 'text-gray-700'}
          />
        )}
        {title}
      </Button>

      {!iconId && (
        <hr
          className={`${isActive ? 'border-blue-400' : 'border-none'} w-full border`}
        />
      )}
    </div>
  );
};

export default TabButton;
