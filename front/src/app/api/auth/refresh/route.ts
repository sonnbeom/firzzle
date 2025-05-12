import { NextResponse } from 'next/server';

import {
  getServerCookie,
  removeServerCookie,
  setServerCookie,
} from '../cookies';

export async function POST() {
  try {
    const accessToken = await getServerCookie('accessToken');

    const response = await fetch(
      `${process.env.NEXT_PUBLIC_API_URL}/auth/refresh`,
      {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          Authorization: `Bearer ${accessToken.value}`,
        },
      },
    );

    if (response.ok) {
      const { data } = await response.json();

      // accessToken 쿠키 설정
      await setServerCookie('accessToken', data.accessToken);

      return NextResponse.json(
        { message: data.message },
        { status: response.status },
      );
    } else {
      removeServerCookie('accessToken');
      return NextResponse.json(
        { message: response.statusText },
        { status: response.status },
      );
    }
  } catch (error) {
    removeServerCookie('accessToken');
    return NextResponse.json({ message: { error } }, { status: 500 });
  }
}
