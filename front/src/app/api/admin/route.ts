import { NextRequest, NextResponse } from 'next/server';
import { api } from '@/api/common/apiInstance';
import { TransitionsResponse } from '@/types/chart';

export async function GET(request: NextRequest) {
  const searchParams = request.nextUrl.searchParams;
  const startDate = searchParams.get('startDate');
  const endDate = searchParams.get('endDate');
  const endpoint = searchParams.get('endpoint');

  if (!startDate || !endDate || !endpoint) {
    return NextResponse.json(
      { message: '필수 파라미터가 누락되었습니다.' },
      { status: 400 },
    );
  }

  try {
    let apiEndpoint = '';

    // 엔드포인트 매핑
    switch (endpoint) {
      case 'transitions':
        apiEndpoint = '/admin/strategy/transitions';
        break;
      case 'learning-rate':
        apiEndpoint = '/admin/strategy/learning-rate';
        break;
      case 'login-rate':
        apiEndpoint = '/admin/strategy/login-rate';
        break;
      case 'summary-level':
        apiEndpoint = '/admin/learning/summary-level';
        break;
      case 'snap-review':
        apiEndpoint = '/admin/learning/snap-review';
        break;
      default:
        return NextResponse.json(
          { message: '잘못된 엔드포인트입니다.' },
          { status: 400 },
        );
    }

    const response = await api.get<TransitionsResponse>(apiEndpoint, {
      params: {
        startDate,
        endDate,
      },
    });

    if (response.status === 'OK') {
      return NextResponse.json(response.data, { status: 200 });
    }

    return NextResponse.json(
      { message: '데이터 조회에 실패하였습니다.' },
      { status: 400 },
    );
  } catch (error) {
    return NextResponse.json({ message: error.message }, { status: 500 });
  }
}
