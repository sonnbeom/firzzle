import { NextResponse } from 'next/server';

import {
  getServerCookie,
  removeServerCookie,
  setServerCookie,
} from '../cookies';

export async function POST() {
  try {
    const accessToken = (await getServerCookie('accessToken')).value;

    console.log('리프레쉬 accessToken: ', accessToken);

    const response = await fetch(
      `${process.env.NEXT_PUBLIC_API_BASE_URL}/auth/refresh`,
      {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        credentials: 'include',
      },
    );

    console.log('백엔드 리프레쉬 응답 상태: ', response.status);

    if (response.ok) {
      const { data } = await response.json();

      console.log('백엔드 리프레쉬 응답 데이터: ', data);

      // accessToken 쿠키 설정
      await setServerCookie('accessToken', data.accessToken);

      return NextResponse.json(
        { message: data.message },
        { status: response.status },
      );
    } else {
      const errorData = await response.json().catch(() => null);
      console.log('백엔드 리프레쉬 에러 데이터: ', errorData);

      removeServerCookie('accessToken');
      return NextResponse.json(
        { message: errorData.message },
        { status: errorData.status },
      );
    }
  } catch (error) {
    removeServerCookie('accessToken');
    return NextResponse.json({ message: { error } }, { status: 500 });
  }
}
