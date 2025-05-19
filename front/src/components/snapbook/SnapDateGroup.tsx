import Link from 'next/link';
import { SnapReview } from '@/types/snapReview';
import { formatDate, formatDateToKorean } from '@/utils/formatDate';
import SnapCard from './SnapCard';

interface SnapDateGroupProps {
  reviews: SnapReview[];
  isPriorityPage: boolean;
}

const SnapDateGroup = ({ reviews, isPriorityPage }: SnapDateGroupProps) => {
  // 날짜별로 리뷰 그룹화
  const reviewsByDate = reviews.reduce(
    (acc, review) => {
      const date = formatDate(review.indate);
      if (!acc[date]) {
        acc[date] = [];
      }
      acc[date].push(review);
      return acc;
    },
    {} as { [date: string]: SnapReview[] },
  );

  // 날짜별로 정렬
  const sortedDates = Object.entries(reviewsByDate).sort(([dateA], [dateB]) =>
    dateB.localeCompare(dateA),
  );
  return (
    <>
      {sortedDates.map(([date, dateReviews]) => (
        <div key={date} className='mb-16'>
          <h2 className='mb-4 text-lg text-gray-950 md:text-xl'>
            {formatDateToKorean(date)}
          </h2>
          <div className='grid grid-cols-3 gap-4 md:grid-cols-4'>
            {dateReviews.map((review, index) => (
              <Link
                key={review.contentSeq}
                href={`/mylearning/snapbook/${review.contentSeq}`}
                className='block'
              >
                <SnapCard
                  data={review}
                  priority={isPriorityPage && index < 4}
                />
              </Link>
            ))}
          </div>
        </div>
      ))}
    </>
  );
};

export default SnapDateGroup;
