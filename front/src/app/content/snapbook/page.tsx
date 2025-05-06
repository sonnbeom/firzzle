import { getSnapReviews } from '@/api/snap';
import SnapList from '@/components/snapbook/SnapList';
import { SnapReviewListResponse } from '@/types/snapReview';

async function getSnapBookData(): Promise<SnapReviewListResponse> {
  try {
    const response = await getSnapReviews();
    return response.data;
  } catch (error) {
    console.error('Error fetching snap reviews:', error);
    return {
      content: [{
        dailySnapReviews: {},  // 빈 스냅리뷰 목록
        totalDays: 0  // 총 날짜 수
      }],
      p_pageno: 1,
      p_pagesize: 20,
      totalElements: 0,
      totalPages: 0,
      last: true,
      hasNext: false
    };
  }
}

const SnapBook = async () => {
  const initialData = await getSnapBookData();

  return <SnapList initialData={initialData} />;
};

export default SnapBook;
