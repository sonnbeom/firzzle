const ContentLayout = ({ children }: { children: React.ReactNode }) => {
  return (
    <div>
      <div>메뉴버튼</div>
      {children}
    </div>
  );
};

export default ContentLayout;
