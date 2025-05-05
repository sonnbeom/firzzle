import { getQuiz } from '@/api/quiz';
import QuizContainer from '@/components/quiz/QuizContainer';

interface PageProps {
  params: Promise<{
    id: string;
  }>;
}

const Quiz = async ({ params }: PageProps) => {
  const { id } = await params;
  const quizData = await getQuiz(id);

  return <QuizContainer quizData={quizData} contentSeq={id} />;
};

export default Quiz;
