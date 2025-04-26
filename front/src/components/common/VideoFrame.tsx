const VideoFrame = () => {
  return (
    <div className='relative aspect-video w-full'>
      <iframe
        className='absolute top-0 left-0 h-full w-full'
        src='https://www.youtube.com/embed/VIDEO_ID'
        title='YouTube video player'
        allow='accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture'
        allowFullScreen
      />
    </div>
  );
};

export default VideoFrame;
