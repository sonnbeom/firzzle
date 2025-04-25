import { SVGProps } from 'react';

interface IconsProps extends SVGProps<SVGSVGElement> {
  id:
    | 'arrow-left'
    | 'arrow-right'
    | 'content'
    | 'menu'
    | 'newchat'
    | 'search'
    | 'snapbook'
    | 'upload'
    | 'write'
    | 'arrow-down'
    | 'arrow-up';
  size?: number;
  color?: string;
}

const Icons = ({
  id,
  size = 24,
  color = 'text-gray-950',
  ...props
}: IconsProps) => {
  return (
    <svg
      width={size}
      height={size}
      viewBox='0 0 24 24'
      className={color}
      {...props}
    >
      <use href={`/assets/icons/_sprite.svg#${id}`} />
    </svg>
  );
};

export default Icons;
