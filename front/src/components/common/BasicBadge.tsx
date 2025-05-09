'use client';

import { Badge } from '@/components/ui/badge';

interface BasicBadgeProps {
  text: string;
  color: string;
}

const BasicBadge = ({ text, color }: BasicBadgeProps) => {
  return (
    <Badge
      variant='secondary'
      className='text-md rounded-full px-3 py-1 font-medium text-white'
      style={{ backgroundColor: `${color}` }}
    >
      {text}
    </Badge>
  );
};

export default BasicBadge;
