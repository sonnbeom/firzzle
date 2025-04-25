import { ReactNode } from 'react';

interface FeatureCardProps {
  icon: ReactNode;
  title: string;
  description: string;
}

const FeatureCard = ({ icon, title, description }: FeatureCardProps) => {
  return (
    <div className='rounded-2xl bg-white p-8 text-center shadow-md transition-all duration-300 hover:-translate-y-1 hover:shadow-xl'>
      <div className='bg-primary-50 mx-auto mb-6 flex h-16 w-16 items-center justify-center rounded-xl'>
        <div className='text-primary-500 h-8 w-8'>{icon}</div>
      </div>

      <h3 className='mb-4 text-2xl font-semibold text-gray-800'>{title}</h3>
      <p className='text-gray-600'>{description}</p>
    </div>
  );
};

export { FeatureCard };
