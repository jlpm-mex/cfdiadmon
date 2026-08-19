import { Outlet } from "react-router-dom";
import Header from "../components/Header"
import { Toaster } from "sonner";

function RootLayout() {
  return (
    <>
      <main>
        <Header/>
        <Toaster position="top-right" richColors closeButton />
        <Outlet />
      </main>
    </>
  );
}

export default RootLayout