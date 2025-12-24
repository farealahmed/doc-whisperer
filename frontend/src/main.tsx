import { createRoot } from "react-dom/client";
import App from "./App.tsx";
import "./index.css";

/**
 * Application Entry Point.
 * <p>
 * This file bootstraps the React application by mounting the root component
 * into the DOM element with id "root".
 * It also imports global CSS styles (Tailwind directives).
 * </p>
 */
createRoot(document.getElementById("root")!).render(<App />);
