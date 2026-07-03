import { FullPageLoader } from "@/components/common/FullPageLoader";
import { useAuthBootstrap } from "@/hooks/useAuthBootstrap";
import { AppRouter } from "@/routes/AppRouter";

export default function App() {
  const { isAuthInitializing } = useAuthBootstrap();

  if (isAuthInitializing) {
    return <FullPageLoader label="Validating session..." />;
  }

  return <AppRouter />;
}
