import { ReactNode } from 'react';

interface ContentCardProps {
  title: string;
  description?: string;
  children?: ReactNode;
}

const ContentCard = ({ title, description, children }: ContentCardProps) => {
  return (
    <div className='flex flex-col gap-3 rounded-lg p-6 shadow-sm'>
      <div className='flex flex-col gap-1'>
        <p className='text-2xl font-bold'>{title}</p>
        {description && <p className='text-xl text-gray-700'>{description}</p>}
      </div>
      {children}
    </div>
  );
};

export default ContentCard;
