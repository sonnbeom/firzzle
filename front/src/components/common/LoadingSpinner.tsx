const LoadingSpinner = () => {
  return (
    <div className='flex h-32 items-center justify-center'>
      <div className='h-8 w-8 animate-spin rounded-full border-4 border-gray-300 border-t-blue-600' />
    </div>
  );
};

export default LoadingSpinner;
