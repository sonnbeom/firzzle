import { externalApi } from './common/apiInstance';

export const postContents = async () => {
  return await externalApi.post('/contents');
};
