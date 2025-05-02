import { getQuiz } from '@/api/quiz';
import QuizContainer from '@/components/quiz/QuizContainer';

interface PageProps {
  params: Promise<{
    id: string;
  }>;
}

const Quiz = async ({ params }: PageProps) => {
  const { id } = await params;
  const response = await getQuiz(id);

  return <QuizContainer quizContents={response.data} contentId={id} />;
};

export default Quiz;
