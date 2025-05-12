import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog';
interface BasicDialogProps {
  isOpen: boolean;
  onOpenChange: (open: boolean) => void;
  title: string;
  description: string;
  children: React.ReactNode;
}

const BasicDialog = ({
  isOpen,
  onOpenChange,
  title,
  description,
  children,
}: BasicDialogProps) => {
  return (
    <Dialog open={isOpen} onOpenChange={onOpenChange}>
      <DialogContent className='sm:max-w-[450px]'>
        <DialogHeader className='text-center'>
          <DialogTitle className='text-center text-xl font-bold text-gray-950'>
            {title}
          </DialogTitle>
          <DialogDescription className='text-md text-gray-700'>
            {description}
          </DialogDescription>
        </DialogHeader>
        <div className='mt-6 flex justify-center gap-4'>{children}</div>
      </DialogContent>
    </Dialog>
  );
};

export default BasicDialog;
