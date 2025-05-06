import { useState, useMemo } from 'react';
import { submitQuizAnswers } from '@/api/quiz';
import {
  QuizData,
  QuizSubmitRequest,
  QuizSubmitResponse,
  QuizAnswerProps,
  QuizCardProps,
  QuizOption,
} from '@/types/quiz';

// 퀴즈 답변 props 생성 유틸리티 함수
const getAnswerProps = (
  question: QuizData['questions'][0],
  index: number,
  questionResults?: QuizSubmitResponse['questionResults'],
): QuizAnswerProps => {
  // API 응답의 explanation 찾기
  const explanation =
    questionResults?.find(
      (result) => result.questionSeq === question.questionSeq,
    )?.explanation || '';

  return {
    questionSeq: index + 1,
    text: question.text,
    correct: question.userAnswer?.isCorrect || false,
    explanation: explanation,
    timestamp: question.timestamp,
    selectedOption: question.options.find(
      (opt): opt is QuizOption =>
        opt.optionSeq === question.userAnswer?.selectedOptionSeq,
    ),
    correctOption: question.userAnswer?.isCorrect
      ? question.options.find(
          (opt): opt is QuizOption =>
            opt.optionSeq === question.userAnswer?.selectedOptionSeq,
        )
      : undefined,
  };
};

// 퀴즈 카드 props 생성 유틸리티 함수
const getCardProps = (
  question: QuizData['questions'][0],
  index: number,
  selected: Array<number | null>,
  handleSelect: (index: number, optionSeq: number) => void,
): QuizCardProps => {
  return {
    questionSeq: index + 1,
    text: question.text,
    options: question.options,
    selected: selected[index],
    onSelect: (optionSeq) => handleSelect(index, optionSeq),
  };
};

export const useQuiz = (quizData: QuizData, contentSeq: string) => {
  // 이미 푸 문제인지 확인
  const isAlreadySubmitted = useMemo(() => {
    return (
      quizData.submission !== null &&
      quizData.questions.every(
        (q) => q.userAnswer !== null && q.userAnswer !== undefined,
      )
    );
  }, [quizData.submission, quizData.questions]);

  // 초기 선택 상태 설정
  const initialSelected = useMemo(() => {
    if (isAlreadySubmitted) {
      return quizData.questions.map((q) => {
        if (!q.userAnswer) return null;
        return q.options.findIndex(
          (opt) => opt.optionSeq === q.userAnswer?.selectedOptionSeq,
        );
      });
    }
    return new Array(quizData.questions.length).fill(null);
  }, [quizData.questions, isAlreadySubmitted]);

  const [selected, setSelected] =
    useState<Array<number | null>>(initialSelected);
  const [showAnswer, setShowAnswer] = useState(isAlreadySubmitted);
  const [quizResult, setQuizResult] = useState<QuizSubmitResponse | null>(
    isAlreadySubmitted && quizData.submission
      ? {
          submission: {
            seq: quizData.submission.submissionSeq,
            contentSeq: quizData.contentSeq,
            correctAnswers: quizData.submission.correctAnswers,
            totalQuestions: quizData.submission.totalQuestions,
            scorePercentage: quizData.submission.scorePercentage,
            indate: quizData.submission.indate,
          },
          questionResults: quizData.questions.map((q) => ({
            questionSeq: q.questionSeq,
            question: q.text,
            selectedAnswer: q.userAnswer
              ? q.userAnswer.selectedOptionSeq.toString()
              : '',
            correctAnswer: q.userAnswer?.isCorrect
              ? q.userAnswer.selectedOptionSeq.toString()
              : '',
            isCorrect: q.userAnswer?.isCorrect || false,
            explanation: '',
          })),
        }
      : null,
  );

  // 답변이 하나라도 선택되었는지 확인
  const hasAnswered = selected.some((s) => s !== null);

  // 모든 문제에 답변을 선택했는지 여부
  const isCompleted = selected.every((s) => s !== null);

  // 답변 선택 처리
  const handleSelect = (index: number, optionSeq: number) => {
    if (showAnswer) return;
    setSelected((prev) => {
      const newSelected = [...prev];
      newSelected[index] = optionSeq;
      return newSelected;
    });
  };

  // 퀴즈 제출 처리
  const handleSubmit = async () => {
    if (!isCompleted) return;

    const request: QuizSubmitRequest = {
      answers: quizData.questions.map((question, index) => {
        const selectedOption = question.options[selected[index] || 0];
        return {
          questionSeq: question.questionSeq,
          selectedAnswer: (selectedOption?.optionSeq || 0).toString(),
        };
      }),
    };

    try {
      const response = await submitQuizAnswers(contentSeq, request);
      setQuizResult(response);
      quizData.questions = quizData.questions.map((q) => {
        const result = response.questionResults.find(
          (r) => r.questionSeq === q.questionSeq,
        );
        if (result) {
          q.userAnswer = {
            selectedOptionSeq: parseInt(result.selectedAnswer),
            isCorrect: result.isCorrect,
          };
        }
        return q;
      });
      window.scrollTo(0, 0);
      setShowAnswer(true);
    } catch (error) {
      console.error('Error submitting quiz:', error);
    }
  };

  return {
    selected,
    showAnswer,
    quizResult,
    isCompleted,
    hasAnswered,
    handleSelect,
    handleSubmit,
    getAnswerProps,
    getCardProps,
  };
};
