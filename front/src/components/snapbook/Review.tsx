import Image from 'next/image';
import { Frame } from '@/types/snapReview';

interface ReviewProps {
  images: (Frame & { description: string | null })[];
  title: string;
  date: string;
}

const Review = ({ images, title, date }: ReviewProps) => {
  return (
    <div className='mx-auto max-w-3xl rounded-lg bg-blue-50 p-4'>
      <div className='space-y-4'>
        <div
          className={`grid grid-cols-1 gap-3 ${images.length > 1 ? 'sm:grid-cols-2' : ''}`}
        >
          {images.map((image, index) => (
            <div
              key={index}
              className='group relative aspect-video w-full overflow-hidden bg-white'
            >
              <div className='relative h-full w-full'>
                <Image
                  src={image.src}
                  alt='리뷰사진'
                  fill
                  sizes={
                    images.length > 1 ? '(max-width: 768px) 100vw, 50vw' : '100vw'
                  }
                  className='object-contain transition-transform duration-300 group-hover:scale-110'
                />
              </div>

              <div className='absolute inset-0 flex flex-col justify-center bg-black/60 p-4 opacity-0 transition-opacity duration-300 group-hover:opacity-100'>
                <p className='sm:text-md text-center text-sm font-semibold text-white'>
                  {image.description}
                </p>
              </div>
            </div>
          ))}
        </div>
        <div className='text-center'>
          <h2 className='text-sm font-semibold text-gray-950 sm:text-lg'>
            {title}
          </h2>
          <p className='mt-2 text-sm text-gray-600'>{date}</p>
        </div>
      </div>
    </div>
  );
};

export default Review;
