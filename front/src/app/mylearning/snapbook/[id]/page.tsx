import SnapBookContent from '@/components/snapbook/SnapBookContent';

interface PageProps {
  params: Promise<{
    id: string;
  }>;
}

const SnapBookDetailPage = async ({ params }: PageProps) => {
  const { id } = await params;

  return (
    <div className='container mx-auto px-4'>
      <SnapBookContent contentSeq={id} />
    </div>
  );
};

export default SnapBookDetailPage;
