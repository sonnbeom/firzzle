import { setCookie } from '@/actions/auth';

export const login = async (accessToken: string) => {
  if (accessToken) {
    window.history.replaceState({}, document.title, window.location.pathname);
    setCookie('accessToken', accessToken);
    return '/content';
  } else {
    return '/';
  }
};
