import { useEffect, useState } from 'react';
import { Swiper, SwiperSlide } from 'swiper/react';
import { Navigation } from 'swiper/modules';
import 'swiper/css';
import 'swiper/css/navigation';
import MovieCard from './MovieCard';

const FeaturedSection = ({ movies, theaters }) => {
  const [isLoggedIn, setIsLoggedIn] = useState(false);

  useEffect(() => {
    // Check if userID is present in localStorage
    const userID = localStorage.getItem('userID');
    if (userID) {
      setIsLoggedIn(true);
    }
  }, []);

  if (!isLoggedIn) {
    return (
      <div className="flex flex-col items-center mt-8 pb-12">
        <h1 className="text-5xl font-bold mb-4">Upcoming Releases</h1>
        <p className="text-xl mb-6">Register now for only $20 per year to view upcoming releases and purchase tickets before public announcement.</p>
      </div>
    );
  }

  return (
    <div className="flex flex-col items-center mt-8 pb-12">
      <h1 className="text-5xl font-bold mb-4">Upcoming Releases</h1>
      <p className="text-xl mb-6">As a registered user, you can buy tickets for these films before public announcement</p>
      <Swiper
        modules={[Navigation]}
        centeredSlides={false}
        spaceBetween={30}
        slidesPerView={3}
        loop={true}
        navigation
        pagination={false}
        style={{
          padding: '0 40px',
          width: '70%',
          maxWidth: '1200px',
        }}
      >
        {movies.map((movie) => (
          <SwiperSlide key={movie.id}>
            <MovieCard movie={movie} theaters={theaters} />
          </SwiperSlide>
        ))}
      </Swiper>
      <div
        className="w-full mt-8"
        style={{
          borderTop: '4px solid #854d0e', // Set the line color and thickness
          maxWidth: '70%', // Limit line width to align with the Swiper
        }}
      ></div>
    </div>
  );
};

export default FeaturedSection;
