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
    // YouTube IFrame Player API 스크립트가 이미 로드되었는지 확인
    if (!window.YT) {
      // YouTube IFrame Player API 스크립트 로드
      const tag = document.createElement('script');
      tag.src = 'https://www.youtube.com/iframe_api';
      // 스크립트 태그를 첫 번째 스크립트 태그 앞에 삽입
      const firstScriptTag = document.getElementsByTagName('script')[0];
      firstScriptTag.parentNode?.insertBefore(tag, firstScriptTag);
    }

    // 플레이어 초기화 함수
    const initializePlayer = () => {
      if (playerRef.current) {
        playerRef.current.destroy();
        playerRef.current = null;
      }

      const player = new window.YT.Player('youtube-player', {
        videoId: playerId,
        playerVars: {
          autoplay: 0, // 자동 재생 비활성화
          controls: 1, // 컨트롤 버튼 표시
        },
        events: {
          onReady: (event) => {
            if (playerRef) {
              playerRef.current = event.target;
              setPlayerRef(playerRef);
            }
          },
        },
      });
    };

    // API가 이미 로드된 경우 바로 초기화
    if (window.YT) {
      initializePlayer();
    } else {
      // API가 로드되기를 기다림
      window.onYouTubeIframeAPIReady = initializePlayer;
    }

    return () => {
      if (playerRef.current) {
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
