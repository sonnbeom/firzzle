'use client';

import { useState } from 'react';
import Icons from './Icons';

const SideMenuButton = () => {
  const [isMenuOpen, setIsMenuOpen] = useState(true);
  const handleMenuOpen = () => {
    setIsMenuOpen(!isMenuOpen);
  };

  return (
    <button onClick={handleMenuOpen} className='py-2'>
      <Icons id='menu' />
    </button>
  );
};

export default SideMenuButton;
