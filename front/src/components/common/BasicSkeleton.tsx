import { Skeleton } from '../ui/skeleton';

interface BasicSkeletonProps {
  width: number;
  height: number;
  rounded?: number;
}

const BasicSkeleton = ({ width, height, rounded = 8 }: BasicSkeletonProps) => {
  return (
    <Skeleton
      style={{
        width: `${width}px`,
        height: `${height}px`,
        borderRadius: `${rounded}px`,
      }}
      className='bg-gray-100'
    />
  );
};

export default BasicSkeleton;
