'use client';

import { toast, ToasterProps } from 'sonner';
import { Toaster } from '../ui/sonner';

const styles = {
  default: { background: 'rgba(26, 26, 26, 0.8)', color: '#fff' },
  success: { background: 'rgba(22, 101, 52, 0.8)', color: '#fff' },
  warning: { background: 'rgba(146, 64, 14, 0.8)', color: '#fff' },
  error: { background: 'rgba(185, 28, 28, 0.8)', color: '#fff' },
} as const;

type BasicToasterProps = Omit<ToasterProps, 'theme' | 'duration'> & {
  duration?: number;
};

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

BasicToaster.success = (message: string) => toast(message, { style: styles.success, className: 'font-medium text-md' });
BasicToaster.error = (message: string) => toast(message, { style: styles.error, className: 'font-medium text-md' });
BasicToaster.warning = (message: string) => toast(message, { style: styles.warning, className: 'font-medium text-md' });
BasicToaster.default = (message: string) => toast(message, { style: styles.default, className: 'font-medium text-md' });

export default BasicToaster;
