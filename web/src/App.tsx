import AuthProvider from './context/AuthProvider';
import Router from './routes/routers';

export default function App() {

  return (
    <>
      <AuthProvider>
        <Router />
      </AuthProvider>
    </>
  );
}
