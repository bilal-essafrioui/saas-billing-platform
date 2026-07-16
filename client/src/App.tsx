import './App.css'
import AppRouter from './routes/AppRouter'
import AppToaster from './components/ui/Toaster';

function App() {
  return (
    <>
      <AppToaster />
      <AppRouter />;
    </>
  );
}

export default App
