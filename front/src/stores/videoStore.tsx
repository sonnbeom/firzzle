import { RefObject } from 'react';
import { create } from 'zustand';
import { YouTubePlayer } from 'types/video';

interface VideoState {
  videoId: string;
  playerRef: RefObject<YouTubePlayer> | null;
  setPlayerRef: (ref: RefObject<YouTubePlayer>) => void;
}

const useVideoStore = create<VideoState>((set) => ({
  videoId: '',
  playerRef: null,
  setPlayerRef: (ref) => set({ playerRef: ref }),
}));

export default useVideoStore;
