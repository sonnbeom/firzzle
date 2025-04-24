import Link from 'next/link';

interface SideMenuCardProps {
  id: string;
  thumbnail: string;
  title: string;
  date: string;
}

const SideMenuCard = ({ id, thumbnail, title, date }: SideMenuCardProps) => {
  return (
    <Link
      href={`/content/${id}`}
      className='flex w-full gap-4 sm:h-[65px] md:h-[85px]'
    >
      {/* Next/Image로 바꾸기 */}
      <img
        src={thumbnail}
        alt={title}
        className='rounded sm:w-[100px] md:w-[150px]'
      />
      <div className='flex flex-col justify-between py-2'>
        <p className='line-clamp-2 font-medium text-gray-950'>{title}</p>
        <p className='text-sm text-gray-700'>{date}</p>
      </div>
    </Link>
  );
};

export default SideMenuCard;
