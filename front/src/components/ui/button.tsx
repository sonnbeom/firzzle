import { Slot } from '@radix-ui/react-slot';
import { cva, type VariantProps } from 'class-variance-authority';
import * as React from 'react';
import { cn } from 'utils/lib/utils';

const buttonVariants = cva(
  "inline-flex items-center justify-center gap-2 whitespace-nowrap rounded-md text-base lg:text-lg cursor-pointer font-medium transition-all disabled:pointer-events-none [&_svg]:pointer-events-none [&_svg:not([class*='size-'])]:size-4 shrink-0 [&_svg]:shrink-0 outline-none aria-invalid:ring-destructive/20 dark:aria-invalid:ring-destructive/40 aria-invalid:border-destructive",
  {
    variants: {
      variant: {
        default: 'bg-blue-400 text-white',
        disabled: 'bg-gray-200 text-white cursor-not-allowed',
        outline:
          'border border-blue-400 bg-white text-blue-400 hover:bg-blue-400 hover:text-white',
        secondary: 'bg-blue-300 text-white',
        ghost: 'bg-gray-50 text-gray-700',
        text: 'bg-white text-gray-950 hover:bg-gray-50',
      },
      size: {
        default: 'px-5 lg:px-[26px] py-[6px] has-[>svg]:px-3',
        sm: 'rounded-sm gap-1 px-5 py-1 has-[>svg]:px-2',
        lg: 'px-[26px] lg:px-8 py-2 has-[>svg]:px-4',
        icon: 'size-9',
      },
    },
    defaultVariants: {
      variant: 'default',
      size: 'default',
    },
  },
);

function Button({
  className,
  variant,
  size,
  asChild = false,
  ...props
}: React.ComponentProps<'button'> &
  VariantProps<typeof buttonVariants> & {
    asChild?: boolean;
  }) {
  const Comp = asChild ? Slot : 'button';

  return (
    <Comp
      data-slot='button'
      className={cn(buttonVariants({ variant, size, className }))}
      {...props}
    />
  );
}

export { Button, buttonVariants };
