import { Routes, Route } from "react-router-dom";
import LandingPage from "../features/landing/pages/LandingPage";
import LoginPage from "../features/auth/forms/LoginForm";
import RegisterPage from "../features/auth/forms/RegisterForm";
import VerificationForm from "../features/auth/forms/VerificationForm";
import ChoosePlanPage from "../features/subscription/page/ChoosePlanPage";
import PaymentSuccessPage from "../features/payment/page/PaymentSuccessPage";
import DashboardLayout from "../layouts/DashboardLayout";
import Spinner from "../components/ui/Spinner";
import ProtectedRoute from "../components/ProtectedRoute";
import DashboardPage from "../features/dashboard/page/DashboardPage";
import MySubscriptionPage from "../features/dashboard/page/MySubscriptionPage";
import PaymentsAndInvoicesPage from "../features/dashboard/page/PaymentsAndInvoicesPage";
import AccountSuspendedPage from "../features/subscription/page/AccountSuspendedPage";

export default function AppRouter() {
  return (
    <Routes>
      <Route path="/" element={<LandingPage />} />
      <Route path="/login" element={<LoginPage />} />
      <Route path="/verify-email" element={<VerificationForm />} />
      <Route path="/register" element={<RegisterPage />} />
      <Route path="/choose-plan" element={<ChoosePlanPage />} />
      <Route path="/spinner" element={<Spinner />} />
      
      {/* DASHBOARD */}
      {/* Routes protégées */}
      <Route element={<ProtectedRoute />}>
        <Route path="/payment/success" element={<PaymentSuccessPage />} />
        <Route path="/suspended" element={<AccountSuspendedPage />} />
        <Route path="/dashboard" element={<DashboardLayout />}>
        {/* Route */}
          <Route index element={<DashboardPage />}/>
          <Route path="subscription" element={<MySubscriptionPage />}/>
          <Route path="payment" element={<PaymentsAndInvoicesPage />} />
        </Route>     
      </Route>
    </Routes>
  );
}

