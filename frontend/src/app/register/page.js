import Footer from "@/components/Footer";
import Register from "@/components/Register";

const RegisterPage = () => {
  return (
  <div className="flex flex-col min-h-screen">
    <div className="flex-grow">
      <Register />
    </div>
  <Footer />
  </div>
  );
}

export default RegisterPage;