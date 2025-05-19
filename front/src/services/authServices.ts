import { setCookie } from '@/actions/auth';

export const login = async (accessToken: string) => {
  if (accessToken) {
    console.log('로그인 서비스', accessToken);
    window.history.replaceState({}, document.title, window.location.pathname);
    setCookie('accessToken', accessToken);
    return '/content';
  } else {
    return '/';
  }
};
