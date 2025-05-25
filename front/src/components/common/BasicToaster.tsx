'use client';

import { ReactNode } from 'react';
import { toast, Toaster, ToasterProps } from 'sonner';

const commonStyles = {
  color: 'white',
  alignItems: 'center',
  textAlign: 'center',
  justifyContent: 'center',
  fontSize: '14px',
  minHeight: 'auto',
  height: 'auto',
} as const;

const styles = {
  default: {
    ...commonStyles,
    background: 'rgba(52, 52, 55, 0.8)',
  },
  success: {
    ...commonStyles,
    background: 'rgba(50, 78, 239, 0.8)',
  },
  warning: {
    ...commonStyles,
    background: 'rgba(236, 178, 6, 0.8)',
  },
  error: {
    ...commonStyles,
    background: 'rgba(237, 21, 21, 0.8)',
  },
} as const;

type BasicToasterProps = Omit<ToasterProps, 'theme' | 'duration'> & {
  duration?: number;
};

interface ToastOptions {
  duration?: number;
  id?: string;
  persistent?: boolean; // 사용자가 토스트를 직접 닫을 수 있는지 여부
  children?: ReactNode;
  closeButton?: boolean; // 닫기 버튼 표시 여부
}

const BasicToaster = ({ duration = 1500, ...props }: BasicToasterProps) => {
  return (
    <Toaster
      theme='system'
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
    duration: options?.persistent ? Infinity : options?.duration,
    onDismiss: () => {
      // 사용자가 직접 닫아야 함
      if (options?.persistent) {
        return false;
      }
    },
    description: options?.children,
    closeButton: options?.closeButton ?? false,
  });

BasicToaster.error = (message: string, options?: ToastOptions) =>
  toast(message, {
    id: options?.id,
    style: styles.error,
    duration: options?.duration,
    description: options?.children,
    closeButton: options?.closeButton ?? false,
  });

BasicToaster.warning = (message: string, options?: ToastOptions) =>
  toast(message, {
    id: options?.id,
    style: styles.warning,
    duration: options?.duration,
    description: options?.children,
    closeButton: options?.closeButton ?? false,
  });

BasicToaster.default = (message: string, options?: ToastOptions) =>
  toast(message, {
    id: options?.id,
    style: styles.default,
    duration: options?.duration,
    description: options?.children,
    closeButton: options?.closeButton ?? false,
  });

export default BasicToaster;
