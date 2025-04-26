import React from 'react';
import { Button } from '../ui/button';

interface ShareProps {
  isOpen: boolean;
  onClose: () => void;
  url?: string;
}

const Share = ({ isOpen, onClose, url = 'https://firzzleai.photobook.com' }: ShareProps) => {
  if (!isOpen) return null;

  return (
    <div className='fixed inset-0 z-50 flex items-center justify-center bg-black/50'>
      <div className='relative w-full max-w-md rounded-md bg-white p-6 shadow-lg'>
        <button
          onClick={onClose}
          className='absolute right-4 top-4 text-gray-500 hover:text-gray-700'
        >
          ✕
        </button>
        <div className='mb-6 text-center'>
          <h2 className='text-lg text-gray-800 font-medium'>공유하기</h2>
        </div>
        <div className='space-y-6 p-4'>
          <p className='text-center text-lg text-black'>
            링크를 갖고 있는 모든 사용자가 방문할 수 있습니다.
          </p>
          <div className='flex space-x-2'>
            <input
              readOnly
              value={url}
              className='flex-1 rounded border border-blue-100 px-3 py-2'
            />
            <Button
              variant='default'
              onClick={() => {
                navigator.clipboard.writeText(url);
              }}
            >
              링크 복사
            </Button>
          </div>
        </div>

      </div>
    </div>
  );
};

export default Share;