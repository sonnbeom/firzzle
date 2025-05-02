import { Skeleton } from '../ui/skeleton';
interface BasicSkeletonProps {
  width: number;
  height: number;
  rounded?: number;
}

const BasicSkeleton = ({ width, height, rounded }: BasicSkeletonProps) => {
  return (
    <Skeleton
      className={`w-[${width}px] h-[${height}px] rounded-[${rounded}px]`}
    />
  );
};

export default BasicSkeleton;
