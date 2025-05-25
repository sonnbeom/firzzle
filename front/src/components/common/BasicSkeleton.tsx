import { cn } from '@/utils/lib/utils';
import { Skeleton } from '../ui/skeleton';

interface BasicSkeletonProps {
  className: string;
}

const BasicSkeleton = ({ className }: BasicSkeletonProps) => {
  return <Skeleton className={cn('bg-gray-100', 'rounded-lg', className)} />;
};

export default BasicSkeleton;
