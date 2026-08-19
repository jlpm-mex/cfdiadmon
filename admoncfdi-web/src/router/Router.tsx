import { createBrowserRouter } from "react-router-dom";

import HomePage from "../pages/HomePage.tsx";
import Cfdi from "../pages/CfdiPage.tsx"
import RootLayout from "../layouts/RootLayout.tsx";

export const router = createBrowserRouter([
  {
    path: "/",
    element: <RootLayout />,
    children: [
      {
        index: true,
        element: <HomePage />,
      },
      {
        path:"cfdis",
        element:<Cfdi/>
      }
    ],
  },
])