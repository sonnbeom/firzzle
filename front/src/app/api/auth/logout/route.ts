import { NextResponse } from 'next/server';
import { getCookie, removeCookie } from '@/actions/auth';

export async function POST() {
  try {
    const accessToken = getCookie('accessToken');

    if (!accessToken) {
      return NextResponse.json(
        { message: 'No access token found' },
        { status: 401 },
      );
    }

    const response = await fetch(
      `${process.env.NEXT_PUBLIC_API_BASE_URL}/auth/logout`,
      {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          Authorization: `Bearer ${accessToken}`,
        },
        credentials: 'include',
      },
    );

    console.log(response);

    if (!response.ok) {
      const errorData = await response.text();
      return NextResponse.json(
        { message: errorData },
        { status: response.status },
      );
    }

    // Remove the access token cookie
    removeCookie('accessToken');

    return NextResponse.json({ message: 'Logged out successfully' });
  } catch (error) {
    console.error('Logout error:', error);
    return NextResponse.json(
      { message: 'Internal server error' },
      { status: 500 },
    );
  }
}
