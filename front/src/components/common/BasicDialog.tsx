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
      <DialogContent>
        <DialogHeader>
          <DialogTitle>{title}</DialogTitle>
          <DialogDescription>{description}</DialogDescription>
        </DialogHeader>
        <div className='mt-4 flex justify-end space-x-4'>{children}</div>
      </DialogContent>
    </Dialog>
  );
};

export default BasicDialog;
