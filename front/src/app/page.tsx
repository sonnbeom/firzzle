import CtaSection from '@/components/home/CtaSection';
import FeaturesSection from '@/components/home/FeaturesSection';
import HeroSection from '@/components/home/HeroSection';

// import Footer from '@/components/home/Footer';

const Home = () => {
  return (
    <main className='flex h-full flex-col'>
      <HeroSection />
      <FeaturesSection />
      <CtaSection />
      {/* <Footer /> */}
    </main>
  );
};

export default Home;
