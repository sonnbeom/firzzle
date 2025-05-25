import { RefObject } from 'react';
import { create } from 'zustand';
import { YouTubePlayer } from 'types/player';

interface PlayerState {
  playerRef: RefObject<YouTubePlayer> | null;
  setPlayerRef: (ref: RefObject<YouTubePlayer>) => void;
}

export const usePlayerStore = create<PlayerState>((set) => ({
  playerRef: null,
  setPlayerRef: (ref) => set({ playerRef: ref }),
}));
