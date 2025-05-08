// 쿠키 조회
export const getCookie = (name: string): string => {
  if (typeof document === 'undefined') return '';

  const value = `; ${document.cookie}`;
  const parts = value.split(`; ${name}=`);
  if (parts.length === 2) {
    return parts.pop()?.split(';').shift() || '';
  }
  return '';
};

// 쿠키 삭제
export const removeCookie = (name: string) => {
  if (typeof document === 'undefined') return;
  document.cookie = `${name}=; expires=Thu, 01 Jan 1970 00:00:00 GMT; path=/`;
};

// 쿠키 설정
export const setCookie = (
  name: string,
  value: string,
  options: {
    httpOnly?: boolean;
    secure?: boolean;
    sameSite?: 'strict' | 'lax' | 'none';
    path?: string;
    expires?: number;
  } = {},
) => {
  if (typeof document === 'undefined') return;

  let cookie = `${name}=${value}`;

  if (options.expires) {
    const date = new Date();
    date.setTime(date.getTime() + options.expires * 1000);
    cookie += `; expires=${date.toUTCString()}`;
  }

  if (options.path) {
    cookie += `; path=${options.path}`;
  }

  if (options.httpOnly) {
    cookie += '; HttpOnly';
  }

  if (options.secure) {
    cookie += '; Secure';
  }

  if (options.sameSite) {
    cookie += `; SameSite=${options.sameSite}`;
  }

  console.log('cookie: ', cookie);
  document.cookie = cookie;
};
