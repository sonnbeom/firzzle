'use client';

import { useEffect, useRef } from 'react';
import { usePlayerStore } from 'stores/playerStore';
import { YouTubePlayer } from 'types/player';

interface PlayerFrameProps {
  playerId: string;
}

const PlayerFrame = ({ playerId }: PlayerFrameProps) => {
  const playerRef = useRef<YouTubePlayer | null>(null);
  const { setPlayerRef } = usePlayerStore();

  useEffect(() => {
    // YouTube IFrame Player API 스크립트 로드를 위한 태그 생성
    const tag = document.createElement('script');
    tag.src = 'https://www.youtube.com/iframe_api';

    // 스크립트 태그를 쿤서의 첫 번째 스크립트 태그 앞에 삽입
    const firstScriptTag = document.getElementsByTagName('script')[0];
    firstScriptTag.parentNode?.insertBefore(tag, firstScriptTag);

    window.onYouTubeIframeAPIReady = () => {
      const player = new window.YT.Player('youtube-player', {
        videoId: playerId,
        playerVars: {
          autoplay: 0, // 자동 재생 비활성화
          controls: 1, // 컨트롤 버튼 표시
        },
        events: {
          // 플레이어 준비 완료 시 실행
          onReady: (event) => {
            if (playerRef) {
              playerRef.current = event.target;
              setPlayerRef(playerRef);
            }
          },
        },
      });
    };

    return () => {
      // 컴포넌트 언마운트 시 플레이어 제거, playerRef 초기화
      if (playerRef?.current) {
        playerRef.current.destroy();
        playerRef.current = null;
      }
    };
  }, [playerId, setPlayerRef]);

  return (
    <div className='relative aspect-video w-full'>
      <div
        id='youtube-player'
        className='absolute top-0 left-0 h-full w-full'
      />
    </div>
  );
};

export default PlayerFrame;
