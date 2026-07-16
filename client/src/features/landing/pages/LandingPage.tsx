import Navbar from "../../../components/Navbar";
import Hero from "../components/Hero";
import Pricing from "../components/Pricing";
import Footer from "../../../components/Footer";

export default function LandingPage() {
  return (
    <div className="min-h-screen bg-[var(--bg-page)]">
      <Navbar />
      <Hero />
      <Pricing />
      <Footer />
    </div>
  );
}