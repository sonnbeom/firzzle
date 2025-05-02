import { getSnapReviews } from '@/api/snap';
import SnapList from '@/components/snapbook/SnapList';
import { DateGroup } from '@/types/snapReview';

async function getSnapBookData(): Promise<{ data: DateGroup[] }> {
  try {
    const response = await getSnapReviews();
    return response;
  } catch (error) {
    console.error('Error fetching snap reviews:', error);
    return { data: [] };
  }
}

const SnapBook = async () => {
  const snapBookData = await getSnapBookData();

  return <SnapList snapLists={snapBookData} />;
};

export default SnapBook;
