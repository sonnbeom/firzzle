import Link from 'next/link';
import { Button } from '../ui/button';

interface OAuthButtonProps {
  url: string;
  oauth: 'kakao' | 'google';
  title: string;
  className?: string;
}

const OAuthButton = ({ url, oauth, title, className }: OAuthButtonProps) => {
  let CLIENT_ID = '';
  let REDIRECT_URI = '';

  if (oauth === 'kakao') {
    CLIENT_ID = process.env.NEXT_PUBLIC_KAKAO_CLIENT_ID;
    REDIRECT_URI = process.env.NEXT_PUBLIC_KAKAO_REDIRECT_URI;
  }

  if (oauth === 'google') {
    CLIENT_ID = process.env.NEXT_PUBLIC_GOOGLE_CLIENT_ID;
    REDIRECT_URI = process.env.NEXT_PUBLIC_GOOGLE_REDIRECT_URI;
  }

  return (
    <Link
      href={`${url}?client_id=${CLIENT_ID}&redirect_uri=${REDIRECT_URI}&response_type=code`}
    >
      <Button size='lg' className={`${className}`}>
        {title}
      </Button>
    </Link>
  );
};

export default OAuthButton;
