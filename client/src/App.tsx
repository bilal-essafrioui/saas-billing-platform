import './App.css'
import AppRouter from './routes/AppRouter'
import AppToaster from './components/ui/Toaster';
import AuthProvider from './app/providers/AuthProvider';

function App() {
  return (
    <>
      <AppToaster />
      <AuthProvider>
        <AppRouter />
      </AuthProvider>
    </>
  );
}

export default App
