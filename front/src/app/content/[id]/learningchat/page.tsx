import LearningChatContent from '@/components/learningChat/LearningChatContent';

interface PageProps {
  params: Promise<{ id: string }>;
}

const LearningChatPage = async ({ params }: PageProps) => {
  const { id } = await params;
  return <LearningChatContent contentId={id} />;
};

export default LearningChatPage;
