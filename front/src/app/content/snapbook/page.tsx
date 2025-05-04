import { getSnapReviews } from '@/api/snap';
import SnapList from '@/components/snapbook/SnapList';
import { DateGroup } from '@/types/snapReview';

async function getSnapBookData(): Promise<DateGroup[]> {
  try {
    const response = await getSnapReviews();
    return response.data;
  } catch (error) {
    console.error('Error fetching snap reviews:', error);
    return [];
  }
}

const SnapBook = async () => {
  const data = await getSnapBookData();
  console.log(data);

  return <SnapList snapLists={{ data }} />;
};

export default SnapBook;
