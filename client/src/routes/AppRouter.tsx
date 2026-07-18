import { Routes, Route } from "react-router-dom";
import LandingPage from "../features/landing/pages/LandingPage";
import LoginPage from "../features/auth/forms/LoginForm";
import RegisterPage from "../features/auth/forms/RegisterForm";
import VerificationForm from "../features/auth/forms/VerificationForm";
import ChoosePlanPage from "../features/subscription/page/ChoosePlanPage";
import PaymentSuccessPage from "../features/payment/page/PaymentSuccessPage";

export default function AppRouter() {
  return (
    <Routes>
      <Route path="/" element={<LandingPage />} />
      <Route path="/login" element={<LoginPage />} />
      <Route path="/verify-email" element={<VerificationForm />} />
      <Route path="/register" element={<RegisterPage />} />
      <Route path="/choose-plan" element={<ChoosePlanPage />} />
      <Route path="/payment/success" element={<PaymentSuccessPage />} />
    </Routes>
  );
}

