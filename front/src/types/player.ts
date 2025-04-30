// YouTube 플레이어 타입
export interface YouTubePlayer {
  seekTo: (seconds: number, allowSeekAhead: boolean) => void;
  destroy: () => void;
  playVideo: () => void;
  pauseVideo: () => void;
  stopVideo: () => void;
  getCurrentTime: () => number;
  getDuration: () => number;
  getPlayerState: () => number;
}

declare global {
  interface Window {
    YT: {
      Player: new (
        elementId: string,
        options: {
          videoId: string;
          playerVars?: {
            autoplay?: 0 | 1;
            controls?: 0 | 1;
          };
          events?: {
            onReady?: (event: { target: YouTubePlayer }) => void;
          };
        },
      ) => YouTubePlayer;
    };
    onYouTubeIframeAPIReady: () => void;
  }
}
