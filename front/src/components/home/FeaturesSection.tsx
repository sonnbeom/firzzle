import {
  FileText,
  MessageSquare,
  ClipboardCheck,
  Image as ImageIcon,
  PlayCircle,
  BarChart3,
} from 'lucide-react';
import { FeatureCard } from '@/components/home/FeatureCard';

const FeaturesSection = () => {
  const features = [
    {
      icon: <FileText />,
      title: 'AI 요약노트',
      description:
        '영상 콘텐츠를 자동으로 분석하여 핵심 내용을 요약하고, 주제별로 타임스탬프와 함께 정리해 드립니다. 원하는 난이도에 맞게 요약본을 확인할 수 있습니다.',
    },
    {
      icon: <MessageSquare />,
      title: '러닝챗',
      description:
        '영상 내용에 관한 모든 질문에 즉시 답변해주는 AI 챗봇 서비스입니다. 요약 내용을 드래그하거나 직접 질문을 입력하여 심화 학습이 가능합니다.',
    },
    {
      icon: <ClipboardCheck />,
      title: '1:1 퀴즈 대결',
      description:
        '학습한 내용을 OX 퀴즈로 재미있게 테스트하세요. 문제 해설과 관련 영상 타임스탬프를 통해 복습까지 한번에 가능합니다.',
    },
    {
      icon: <ImageIcon />,
      title: '스냅리뷰',
      description:
        '영상의 핵심 장면을 인생네컷 형식으로 캡처하고, 각 장면에 대한 개인적인 인사이트를 기록할 수 있습니다. SNS 공유 기능으로 학습 성과를 친구들과 나눠보세요.',
    },
    {
      icon: <PlayCircle />,
      title: '추천 강의',
      description:
        '현재 학습 중인 영상과 연관된 추가 콘텐츠를 AI가 자동으로 추천해 드립니다. 다양한 플랫폼의 우수한 콘텐츠를 한눈에 확인하세요.',
    },
    {
      icon: <BarChart3 />,
      title: '학습 통계',
      description:
        '개인 학습 활동과 성과를 한눈에 파악할 수 있습니다. 시청한 영상 목록, 러닝챗 사용 이력, 퀴즈 응시 내역 등을 통해 자신의 학습 패턴을 분석해 보세요.',
    },
  ];

  return (
    <section id='features' className='bg-white py-24'>
      <div className='container mx-auto px-4'>
        <div className='mx-auto mb-16 max-w-3xl text-center'>
          <h2 className='mb-4 text-4xl font-bold text-gray-900'>
            학습 경험을 혁신하는 핵심 기능
          </h2>
          <p className='text-xl text-gray-600'>
            firzzle AI의 강력한 기능으로 모든 영상 학습을 더 효율적이고
            효과적으로 만들어 보세요.
          </p>
        </div>

        <div className='grid grid-cols-1 gap-8 md:grid-cols-2 lg:grid-cols-3'>
          {features.map((feature, index) => (
            <FeatureCard
              key={index}
              icon={feature.icon}
              title={feature.title}
              description={feature.description}
            />
          ))}
        </div>
      </div>
    </section>
  );
};

export default FeaturesSection;
