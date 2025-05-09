import MyContentContainer from '@/components/myContents/MyContentContainer';

const MyContentsPage = async () => {
  return (
    <div className='flex flex-col items-center gap-12'>
      {/* <SearchBar
        value=''
        placeholder='검색어를 입력해주세요.'
        onChange={() => {}}
        onSubmit={() => {}}
      /> */}
      <MyContentContainer />
    </div>
  );
};

export default MyContentsPage;
