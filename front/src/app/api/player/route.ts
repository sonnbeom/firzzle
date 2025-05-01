import { NextResponse } from 'next/server';

export async function GET(request: Request) {
  const { searchParams } = new URL(request.url);
  const url = searchParams.get('url');

  if (!url) {
    return NextResponse.json({ error: 'URL is required' }, { status: 400 });
  }

  try {
    // Extract video ID from YouTube URL
    const videoId = url.match(
      /(?:youtube\.com\/(?:[^\/]+\/.+\/|(?:v|e(?:mbed)?)\/|.*[?&]v=)|youtu\.be\/)([^"&?\/\s]{11})/,
    )?.[1];

    if (!videoId) {
      return NextResponse.json(
        { error: 'Invalid YouTube URL' },
        { status: 400 },
      );
    }

    // Use YouTube oEmbed API
    const response = await fetch(
      `https://www.youtube.com/oembed?url=https://www.youtube.com/watch?v=${videoId}&format=json`,
    );

    if (!response.ok) {
      throw new Error('Failed to fetch video information');
    }

    const data = await response.json();

    return NextResponse.json({
      videoId,
      title: data.title,
    });
  } catch (error) {
    console.error('Error in player route:', error);
    return NextResponse.json(
      { error: 'Failed to process YouTube URL' },
      { status: 500 },
    );
  }
}
