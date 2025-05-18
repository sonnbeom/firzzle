import Image from 'next/image';
import { SnapReview } from '@/types/snapReview';

interface SnapCardProps {
  data: SnapReview;
  priority?: boolean;
}

const SnapCard = ({ data, priority = false }: SnapCardProps) => {
  return (
    <div className='group mx-auto h-[220px] max-w-[240px] overflow-hidden rounded-lg bg-white shadow-md transition-all lg:h-[260px]'>
      <div className='relative aspect-video w-full'>
        <Image
          src={data.thumbnailUrl}
          alt={data.contentTitle}
          fill
          sizes='(max-width: 640px) 50vw, (max-width: 768px) 33vw, (max-width: 1024px) 25vw, 20vw'
          quality={60}
          className='object-cover'
          loading={priority ? 'eager' : 'lazy'}
          priority={priority}
          // 성능 최적화를 위한 blur, blurURL
          placeholder='blur'
          blurDataURL='data:image/jpeg;base64,/9j/4AAQSkZJRgABAQAAAQABAAD/4gHYSUNDX1BST0ZJTEUAAQEAAAHIAAAAAAQwAABtbnRyUkdCIFhZWiAH4AABAAEAAAAAAABhY3NwAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAQAA9tYAAQAAAADTLQAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAlkZXNjAAAA8AAAACRyWFlaAAABFAAAABRnWFlaAAABKAAAABRiWFlaAAABPAAAABR3dHB0AAABUAAAABRyVFJDAAABZAAAAChnVFJDAAABZAAAAChiVFJDAAABZAAAAChjcHJ0AAABjAAAADxtbHVjAAAAAAAAAAEAAAAMZW5VUwAAAAgAAAAcAHMAUgBHAEJYWVogAAAAAAAAb6IAADj1AAADkFhZWiAAAAAAAABimQAAt4UAABjaWFlaIAAAAAAAACSgAAAPhAAAts9YWVogAAAAAAAA9tYAAQAAAADTLXBhcmEAAAAAAAQAAAACZmYAAPKnAAANWQAAE9AAAApbAAAAAAAAAABtbHVjAAAAAAAAAAEAAAAMZW5VUwAAACAAAAAcAEcAbwBvAGcAbABlACAASQBuAGMALgAgADIAMAAxADb/2wBDABQODxIPDRQSEBIXFRQdHx4eHRoaHSQtJSEwMT8xOzMvLy4zWEBLNE42SlU9RVtYXnN+h4eFi0pPjG2Hj4z/2wBDAR4eHh0cHRkZHSQgICQkJCQkJCQkJCQkJCQkJCQkJCQkJCQkJCQkJCQkJCQkJCQkJCQkJCQkJCQkJCQkJCQkJCT/wAARCAAIAAoDASIAAhEBAxEB/8QAFQABAQAAAAAAAAAAAAAAAAAAAAb/xAAUEAEAAAAAAAAAAAAAAAAAAAAA/8QAFQEBAQAAAAAAAAAAAAAAAAAAAAX/xAAUEQEAAAAAAAAAAAAAAAAAAAAA/9oADAMBAAIRAxEAPwCdABmX/9k='
          fetchPriority={priority ? 'high' : 'auto'}
        />
      </div>

      <div className='flex h-[120px] flex-col p-4'>
        <span className='text-md mb-2 font-semibold text-gray-950 md:text-xl'>
          스냅 {data.frameCount || 0}컷
        </span>
        <h3 className='line-clamp-2 text-lg text-gray-950'>
          {data.contentTitle}
        </h3>
      </div>
    </div>
  );
};

export default SnapCard;
