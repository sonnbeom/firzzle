import { Button } from '../ui/button';

interface TabButtonProps {
  title: string;
  isActive: boolean;
  onClick: () => void;
}

const TabButton = ({ title, isActive, onClick }: TabButtonProps) => {
  return (
    <div className='flex flex-col items-center gap-1'>
      <Button
        variant={isActive ? 'tabactive' : 'tabinactive'}
        onClick={onClick}
      >
        {title}
      </Button>
      <hr
        className={`${isActive ? 'border-blue-400' : 'border-none'} w-full border`}
      />
    </div>
  );
};

export default TabButton;
