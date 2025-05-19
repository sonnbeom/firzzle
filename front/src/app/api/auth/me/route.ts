import { NextResponse, NextRequest } from 'next/server';
import { api } from '@/api/common/apiInstance';

export async function GET(request: NextRequest) {
  try {
    const response = await api.get('/auth/me');

    if (response.status === 'OK') {
      return NextResponse.json(response.data, { status: 200 });
    }

    return NextResponse.json({ message: response.message }, { status: 400 });
  } catch (error) {
    return NextResponse.json({ message: error.message }, { status: 500 });
  }
}
