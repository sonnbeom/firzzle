'use client';

import {
  Drawer,
  DrawerContent,
  DrawerHeader,
  DrawerTitle,
} from '@/components/ui/drawer';
import LearningChatContent from './LearningChatContent';

interface LearningChatDrawerProps {
  isOpen: boolean;
  onClose: () => void;
  contentId: string;
}

const LearningChatDrawer = ({
  isOpen,
  onClose,
  contentId,
}: LearningChatDrawerProps) => {
  return (
    <Drawer open={isOpen} onOpenChange={onClose}>
      <DrawerContent className='h-[90vh]'>
        <DrawerHeader>
          <DrawerTitle></DrawerTitle>
        </DrawerHeader>
        <LearningChatContent contentId={contentId} />
      </DrawerContent>
    </Drawer>
  );
};

export default LearningChatDrawer;
