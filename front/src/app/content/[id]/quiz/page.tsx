import QuizContainer from '@/components/quiz/QuizContainer';

interface PageProps {
  params: Promise<{
    id: string;
  }>;
}

const Quiz = async ({ params }: PageProps) => {
  const { id } = await params;
  return <QuizContainer contentSeq={id} />;
};

export default Quiz;
