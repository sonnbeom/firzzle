import InputField from '@/components/common/InputField';
import { Button } from '@/components/ui/button';

const LoginForm = () => {
  return (
    <div className='w-full max-w-md rounded-2xl bg-white p-8 shadow-lg'>
      <h2 className='mb-6 text-center text-2xl font-bold'>관리자 로그인</h2>
      <form>
        <InputField
          label='이메일'
          id='email'
          type='email'
          placeholder='이메일을 입력하세요.'
        />
        <InputField
          label='비밀번호'
          id='password'
          type='password'
          placeholder='비밀번호를 입력하세요.'
        />
        <div className='mt-6'>
          <Button
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
