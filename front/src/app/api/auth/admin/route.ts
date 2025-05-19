import { NextRequest, NextResponse } from 'next/server';
import { setCookie } from '@/actions/auth';
import { api } from '@/api/common/apiInstance';
import { AdminLoginRequest, AdminLoginResponse } from '@/types/auth';

// 관리자 로그인
export async function POST(request: NextRequest) {
  try {
    const loginRequest = (await request.json()) as AdminLoginRequest;
    const response = await api.post<AdminLoginResponse, AdminLoginRequest>(
      '/auth/admin/login',
      {
        body: loginRequest,
      },
    );

    if (response.status === 'OK') {
      await setCookie('accessToken', response.data.accessToken);
      return NextResponse.json(response.data, { status: 200 });
    }

    return NextResponse.json({ message: response.message }, { status: 400 });
  } catch (error) {
    return NextResponse.json({ message: error.message }, { status: 500 });
  }
}
