'use client';

import { toast, ToasterProps } from 'sonner';
import { Toaster } from '../ui/sonner';

const styles = {
  default: { background: 'rgba(26, 26, 26, 0.8)', color: '#fff' },
  success: { background: 'rgba(241, 243, 255, 0.8)', color: '#fff' },
  warning: { background: 'rgba(146, 64, 14, 0.8)', color: '#fff' },
  error: { background: 'rgba(185, 28, 28, 0.8)', color: '#fff' },
} as const;

type BasicToasterProps = Omit<ToasterProps, 'theme' | 'duration'> & {
  duration?: number;
};

interface ToastOptions {
  duration?: number;
  id?: string;
  persistent?: boolean; // 사용자가 토스트를 직접 닫을 수 있는지 여부
  children?: React.ReactNode;
  closeButton?: boolean; // 닫기 버튼 표시 여부
}

const BasicToaster = ({ duration = 1500, ...props }: BasicToasterProps) => {
  return (
    <Toaster
      theme='dark'
      position='bottom-center'
      duration={duration}
      {...props}
    />
  );
};

BasicToaster.success = (message: string, options?: ToastOptions) =>
  toast(message, {
    id: options?.id,
    style: styles.success,
    className: 'font-medium text-md flex flex-col gap-2',
    duration: options?.persistent ? Infinity : options?.duration,
    onDismiss: (toast) => {
      // 사용자가 직접 닫아야 함
      if (options?.persistent) {
        return false;
      }
    },
    description: options?.children,
    dismissible: options?.closeButton ?? false,
  });

BasicToaster.error = (message: string, options?: ToastOptions) =>
  toast(message, {
    id: options?.id,
    style: styles.error,
    className: 'font-medium text-md',
    duration: options?.duration,
    description: options?.children,
    dismissible: options?.closeButton ?? false,
  });

BasicToaster.warning = (message: string, options?: ToastOptions) =>
  toast(message, {
    id: options?.id,
    style: styles.warning,
    className: 'font-medium text-md',
    duration: options?.duration,
    description: options?.children,
    dismissible: options?.closeButton ?? false,
  });

BasicToaster.default = (message: string, options?: ToastOptions) =>
  toast(message, {
    id: options?.id,
    style: styles.default,
    className: 'font-medium text-md',
    duration: options?.duration,
    description: options?.children,
    dismissible: options?.closeButton ?? false,
  });

export default BasicToaster;
