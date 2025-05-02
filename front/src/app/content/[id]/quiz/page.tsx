import QuizContainer from '@/components/quiz/QuizContainer';
import { getQuiz } from '@/api/quiz';

interface PageProps {
  params: Promise<{
    id: string;
  }>;
}

const Quiz = async ({ params }: PageProps) => {
  const { id } = await params;
  const response = await getQuiz(id);
  console.log('Quiz response:', response);

  return <QuizContainer quizContents={response.data} contentId={id} />;
};

export default Quiz;
