import { create } from 'zustand';

interface ChatState {
  currentExamSeq: string | null;
  solvedCount: number;
  setCurrentExamSeq: (currentExamSeq: string | null) => void;
  setSolvedCount: (solvedCount: number) => void;
}

export const useChatStore = create<ChatState>()((set) => ({
  currentExamSeq: null,
  solvedCount: 0,

  setCurrentExamSeq: (currentExamSeq) => set({ currentExamSeq }),
  setSolvedCount: (solvedCount) => set({ solvedCount }),
}));
