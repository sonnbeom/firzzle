'use client';

import Link from 'next/link';
import { DateGroup } from '@/types/snapReview';
import { useInfiniteScroll } from 'hooks/useInfiniteScroll';
import { ITEMS_PER_PAGE } from 'utils/const';
import SnapCard from './SnapCard';

interface SnapListProps {
  snapLists: { data: DateGroup[] };
}

function SnapList({ snapLists }: SnapListProps) {
  const { visibleData: visibleGroups, observerTarget } = useInfiniteScroll({
    initialData: snapLists.data || [],
    itemsPerPage: ITEMS_PER_PAGE,
  });

  return (
    <div className='MB container mx-auto px-4'>
      {visibleGroups.map((group) => (
        <div key={group.date} className='mb-16'>
          <h2 className='mb-4 text-lg text-gray-950 md:text-xl'>
            {group.date}
          </h2>
          <div className='grid grid-cols-2 gap-4 md:grid-cols-4'>
            {group.items.map((item, index) => (
              <Link
                key={`${item.id}-${index}`}
                href={`/content/snapbook/${item.id}`}
                className='block'
              >
                <SnapCard data={item} />
              </Link>
            ))}
          </div>
        </div>
      ))}
      <div ref={observerTarget} />
    </div>
  );
}

export default SnapList;
