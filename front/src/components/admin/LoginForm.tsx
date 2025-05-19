'use client';

import { FormEvent } from 'react';
import { adminLogin } from '@/api/auth';
import InputField from '@/components/common/InputField';
import { Button } from '@/components/ui/button';
import { AdminLoginRequest } from '@/types/auth';

const LoginForm = () => {
  const handleSubmit = async (e: FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    const formData = new FormData(e.currentTarget);
    const username = formData.get('username') as string;
    const password = formData.get('password') as string;

    const loginRequest: AdminLoginRequest = { username, password };
    await adminLogin(loginRequest);
  };

  return (
    <div className='w-full max-w-md rounded-2xl bg-white p-8 shadow-lg'>
      <h2 className='mb-6 text-center text-2xl font-bold'>관리자 로그인</h2>
      <form onSubmit={handleSubmit}>
        <InputField
          label='아이디'
          id='username'
          name='username'
          type='text'
          placeholder='아이디를 입력하세요.'
        />
        <InputField
          label='비밀번호'
          id='password'
          name='password'
          type='password'
          placeholder='비밀번호를 입력하세요.'
        />
        <div className='mt-6'>
          <Button
            type='submit'
            variant='default'
            className='sm:text-md w-full cursor-pointer text-sm md:text-lg'
            size='lg'
          >
            로그인
          </Button>
        </div>
      </form>
    </div>
  );
};

export default LoginForm;
