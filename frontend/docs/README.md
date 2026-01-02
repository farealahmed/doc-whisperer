# Doc-Whisperer Frontend

This is the frontend application for Doc-Whisperer, a full-stack PDF/DOCX chat application. It allows users to upload documents and chat with them using an AI-powered backend.

## 🚀 Features

-   **Document Management**: Upload, view, and delete PDF and text documents.
-   **Interactive Chat**: Chat with your documents using an LLM-powered assistant.
-   **Responsive Design**: Built with a modern, responsive UI using Tailwind CSS and shadcn/ui.
-   **Real-time Updates**: Instant feedback on document processing and chat responses.

## 🛠️ Tech Stack

-   **Framework**: [React](https://react.dev/) with [TypeScript](https://www.typescriptlang.org/)
-   **Build Tool**: [Vite](https://vitejs.dev/)
-   **Styling**: [Tailwind CSS](https://tailwindcss.com/)
-   **UI Components**: [shadcn/ui](https://ui.shadcn.com/)
-   **Icons**: [Lucide React](https://lucide.dev/)
-   **Routing**: [React Router](https://reactrouter.com/)
-   **State Management/Data Fetching**: [TanStack Query](https://tanstack.com/query/latest)

## 📋 Prerequisites

-   Node.js (v18 or higher recommended)
-   npm (comes with Node.js)
-   The Doc-Whisperer Backend running on port 8080

## ⚡ Getting Started

1.  **Navigate to the frontend directory:**

    ```bash
    cd frontend
    ```

2.  **Install dependencies:**

    ```bash
    npm install
    ```

3.  **Run the development server:**

    ```bash
    npm run dev
    ```

    The application will be available at `http://localhost:5173`.

## 📜 Scripts

-   `npm run dev`: Starts the development server.
-   `npm run build`: Builds the application for production.
-   `npm run preview`: Preview the production build locally.
-   `npm run lint`: Runs ESLint to check for code quality issues.

## 📂 Project Structure

```
frontend/
├── src/
│   ├── components/     # Reusable UI components
│   │   ├── ui/         # shadcn/ui base components
│   │   └── ...         # Feature-specific components (ChatInterface, DocumentSidebar, etc.)
│   ├── hooks/          # Custom React hooks (e.g., use-toast)
│   ├── lib/            # Utility functions (utils.ts)
│   ├── pages/          # Application pages (Index, Welcome, NotFound)
│   ├── services/       # API integration (api.ts)
│   ├── types/          # TypeScript type definitions
│   ├── App.tsx         # Main application component with routing
│   └── main.tsx        # Entry point
├── public/             # Static assets
├── index.html          # HTML entry point
├── package.json        # Project dependencies and scripts
├── tailwind.config.ts  # Tailwind CSS configuration
├── tsconfig.json       # TypeScript configuration
└── vite.config.ts      # Vite configuration (includes proxy setup)
```

## 🔌 API Integration

The frontend is configured to proxy API requests to the backend. Ensure your backend is running on `http://localhost:8080`. The proxy configuration can be found in `vite.config.ts`.

```typescript
// vite.config.ts
server: {
  host: "::",
  port: 5173,
  proxy: {
    '/api': {
      target: 'http://localhost:8080',
      changeOrigin: true,
    }
  }
}
```
