import SearchBar from '@/components/common/SearchBar';
import MyContentContainer from '@/components/myContents/MyContentContainer';

const MyContentsPage = async () => {
  return (
    <div className='flex flex-col items-center gap-12'>
      <SearchBar />
      <MyContentContainer />
    </div>
  );
};

export default MyContentsPage;
