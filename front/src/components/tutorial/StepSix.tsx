// components/StepSix.tsx
import React from 'react';

const lectures = [
  {
    id: 1,
    title:
      '인공지능의 역사부터 최신 기술 트렌드까지 한눈에 이해하는 AI 입문 강의',
    image: '/assets/images/lecture1.png',
  },
  {
    id: 2,
    title: '딥러닝 실무 적용 사례와 함께 배우는 핵심 알고리즘 완전 정복 코스',
    image: '/assets/images/lecture2.png',
  },
  {
    id: 3,
    title: 'ChatGPT와 자연어처리의 미래, 생성형 AI 활용법 총정리',
    image: '/assets/images/lecture3.png',
  },
];

const experts = [
  {
    id: 1,
    name: '홍길동',
    title: 'AI 리서처',
    company: 'OpenAI Korea',
    followers: 1200,
    likes: 340,
    image: '/assets/images/expertise1.png',
  },
  {
    id: 2,
    name: '김유진',
    title: '데이터 사이언티스트',
    company: 'DeepTech',
    followers: 950,
    likes: 275,
    image: '/assets/images/expertise2.png',
  },
  {
    id: 3,
    name: '박지민',
    title: '머신러닝 엔지니어',
    company: 'AI Factory',
    followers: 1100,
    likes: 320,
    image: '/assets/images/expertise3.png',
  },
];

const StepSix: React.FC = () => {
  return (
    <div className='container mx-auto my-8 px-4'>
      <div className='rounded-lg border border-gray-100 bg-white p-6 shadow-sm'>
        <div className='mb-4 flex items-start'>
          <div className='mr-4 flex h-12 w-12 items-center justify-center rounded-full bg-blue-400 font-semibold text-white'>
            6
          </div>
          <div className='w-full'>
            <h3 className='mb-2 text-xl font-bold'>관련 컨텐츠 추천</h3>
            <p className='mb-4 text-gray-600'>
              학습한 내용과 관련된 강의와 전문가가 자동으로 추천됩니다. 추천
              강의를 클릭하면 바로 해당 강의로 이동할 수 있습니다. 관련 주제로
              학습을 이어가며 지식의 폭을 넓혀보세요.
            </p>

            {/* 강의 추천 */}
            <div className='mb-8'>
              <div className='mb-4 flex items-center justify-between'>
                <h4 className='text-xl font-semibold text-gray-900'>
                  인공지능에 관련된 강의를 추천해드릴게요
                </h4>
              </div>

              <div className='mt-3 flex justify-center gap-4'>
                {lectures.map((lecture) => (
                  <div key={lecture.id} className='w-1/3 text-center'>
                    <div className='overflow-hidden rounded-xl border border-gray-200 shadow-sm'>
                      <div className='aspect-[16/9] w-full'>
                        <img
                          src={lecture.image}
                          alt={lecture.title}
                          className='h-full w-full object-cover'
                        />
                      </div>
                    </div>
                    <div className='mt-2 text-sm leading-snug font-medium break-words text-gray-800'>
                      {lecture.title}
                    </div>
                  </div>
                ))}
              </div>
            </div>

            {/* 전문가 추천 */}
            <div>
              <div className='mb-4 flex items-center justify-between'>
                <h4 className='text-xl font-semibold text-gray-900'>
                  인공지능 전문가와 대화해보세요
                </h4>
              </div>

              <div className='mt-3 flex justify-center gap-4'>
                {experts.map((expert) => (
                  <div
                    key={expert.id}
                    className='w-1/3 rounded-xl border border-gray-200 p-4 text-center shadow-sm'
                  >
                    <div className='mx-auto mb-3 aspect-square w-24 overflow-hidden rounded-full border border-gray-300'>
                      <img
                        src={expert.image}
                        alt={expert.name}
                        className='h-full w-full object-cover'
                      />
                    </div>
                    <div className='text-sm font-semibold'>{expert.name}</div>
                    <div className='text-xs text-gray-500'>
                      {expert.title} · {expert.company}
                    </div>
                    <div className='mt-2 text-xs text-gray-600'>
                      팔로워 {expert.followers.toLocaleString()} · 좋아요{' '}
                      {expert.likes.toLocaleString()}
                    </div>
                  </div>
                ))}
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default StepSix;
