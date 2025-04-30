import Image from 'next/image';
import { ReactNode } from 'react';
import useVideoStore from 'stores/videoStore';
import { formatTimeStamp } from 'utils/formatTimeStamp';
import Icons from './Icons';

// 타임스탬프 타입
// 기본 타입
interface TimeStampBaseProps {
  time: number;
  children: ReactNode;
}

// MM:SS 타입
interface TimeTextProps extends TimeStampBaseProps {
  type: 'time';
}

// '복습하러가기' 타입
interface ReviewLinkProps extends TimeStampBaseProps {
  type: 'review';
}

// 이미지 타입
interface ImageProps extends TimeStampBaseProps {
  type: 'image';
  imageUrl: string;
  width: number;
  height: number;
}

type TimeStampProps = TimeTextProps | ReviewLinkProps | ImageProps;

const TimeStamp = (props: TimeStampProps) => {
  const playerRef = useVideoStore((state) => state.playerRef);

  // 타임스탬프 클릭 시 플레이어 이동
  const handleClick = () => {
    if (playerRef?.current) {
      playerRef.current.seekTo(props.time, true);
    }
  };

  switch (props.type) {
    case 'time':
      return (
        <button
          className='text-gray-700 hover:text-blue-300'
          onClick={handleClick}
        >
          {formatTimeStamp(props.time)}
        </button>
      );

    case 'review':
      return (
        <button
          className='flex cursor-pointer justify-center gap-1'
          onClick={handleClick}
        >
          <span className='font-semibold text-blue-300'>복습하러가기</span>
          <Icons id='arrow-right' color='text-blue-300' />
        </button>
      );

    case 'image':
      return (
        <button
          className={`relative cursor-pointer w-[${props.width}px] h-[${props.height}px]`}
          onClick={handleClick}
        >
          <Image
            src={props.imageUrl}
            alt='스냅 이미지'
            fill
            sizes='100vw'
            className='object-cover'
          />
        </button>
      );

    default:
      return null;
  }
};

export default TimeStamp;
