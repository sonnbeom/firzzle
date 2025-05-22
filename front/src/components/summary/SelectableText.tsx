'use client';

import { MouseEvent, ReactNode, useState } from 'react';
import BasicPopOver from '../common/BasicPopOver';
import Icons from '../common/Icons';

interface SelectableTextProps {
  children: ReactNode;
}

const SelectableText = ({ children }: SelectableTextProps) => {
  const dummyData =
    '컴퓨터가 명시적으로 프로그래밍되지 않아도 데이터에서 스스로 학습하여 문제를 해결하는 기술';

  // 선택된 텍스트 관리
  const [text, setText] = useState('');
  const [definition, setDefinition] = useState('');

  // 러닝챗 버튼 관리
  const [showButton, setShowButton] = useState<boolean>(false);
  const [buttonPosition, setButtonPosition] = useState({ x: 0, y: 0 });

  // 텍스트 선택 핸들러
  const handleTextSelection = (e: MouseEvent<HTMLDivElement>) => {
    const selection = window.getSelection();
    const selectedText = selection?.toString().trim();

    if (selectedText && selection) {
      // 선택된 텍스트의 범위
      const range = selection.getRangeAt(0);
      // 선택된 텍스트의 위치
      const rect = range.getBoundingClientRect();

      setText(selectedText);
      setShowButton(true);
      setButtonPosition({
        x: rect.right,
        y: rect.top + rect.height / 2,
      });
    } else {
      setShowButton(false);
    }
  };

  // 러닝챗 버튼 클릭 핸들러
  const handleGetDefinition = () => {
    setDefinition(dummyData);
    setText('');
  };

  return (
    <>
      <div onMouseUp={handleTextSelection}>{children}</div>

      {showButton && (
        <div
          className='fixed'
          style={{
            left: `${buttonPosition.x - 5}px`,
            top: `${buttonPosition.y + 3}px`,
            transform: 'translate(10px, -50%)',
          }}
        >
          <BasicPopOver
            trigger={
              <button
                className='rounded-full bg-white'
                onClick={handleGetDefinition}
              >
                <Icons id='arrow-right' color='text-blue-200' />
              </button>
            }
            content={<p className='text-gray-950'>{definition}</p>}
          />
        </div>
      )}
    </>
  );
};

export default SelectableText;
