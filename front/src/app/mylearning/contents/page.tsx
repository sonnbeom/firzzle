import { getContentList } from '@/api/content';
import SearchBar from '@/components/common/SearchBar';
import MyContentContainer from '@/components/myContents/MyContentContainer';

const MyContentsPage = async () => {
  // 서버 사이드에서 첫 페이지 데이터를 가져옵니다
  const { content } = await getContentList(1, 20);

  return (
    <div className='flex flex-col items-center gap-12'>
      <SearchBar />
      <MyContentContainer initialContents={content} />
    </div>
  );
};

export default MyContentsPage;
