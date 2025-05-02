import { PlayerInfo } from '@/types/player';
import { internalApi } from './common/apiInstance';

export const getPlayer = async (url: string) => {
  return await internalApi.get<PlayerInfo>(`/player?url=${url}`);
};
