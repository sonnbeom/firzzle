import { ReactNode } from 'react';

interface FeatureCardProps {
  icon: ReactNode;
  title: string;
  description: string;
}

const FeatureCard = ({ icon, title, description }: FeatureCardProps) => {
  return (
    <div className='flex flex-col items-center gap-4 rounded-2xl bg-white p-6 text-center shadow-md transition-all duration-300'>
      <div className='text-gray-900'>{icon}</div>
      <h3 className='text-xl font-semibold text-gray-800 md:text-2xl'>
        {title}
      </h3>
      <p className='text-gray-600'>{description}</p>
    </div>
  );
};

export { FeatureCard };
