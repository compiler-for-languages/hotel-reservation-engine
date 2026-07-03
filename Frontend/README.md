# Hotel Reservation Engine - Frontend

A production-grade, enterprise-oriented React application for managing hotel operations.

## Tech Stack
- **Framework:** React 19 + Vite
- **Language:** TypeScript
- **Styling:** Tailwind CSS (Enterprise Theme: Monologue Notes)
- **State Management:** 
  - **Server State:** TanStack Query (v5)
  - **Client State:** Zustand
- **Forms:** React Hook Form + Zod
- **API Client:** Axios
- **Routing:** React Router DOM

## Prerequisites
- Node.js (v18+)
- Backend Spring Boot service running at `http://localhost:8080` (default)

## Installation

1. Clone the repository.
2. Navigate to the `Frontend/` directory.
3. Install dependencies:
   ```bash
   npm install
   ```
4. Create a `.env` file based on `.env.example`:
   ```bash
   cp .env.example .env
   ```

## Development
To start the development server:
```bash
npm run dev
```

## Production Build
To generate a production-ready build:
```bash
npm run build
```
The output will be in the `dist/` directory.

## Architecture
This project follows a **Feature-Based Architecture**:
- `src/services/`: Direct mapping to Spring Boot controllers.
- `src/types/`: TypeScript interfaces mirroring Backend DTOs.
- `src/store/`: Zustand stores for session and UI state.
- `src/routes/`: RBAC-guarded routing logic.
- `src/layouts/`: Global enterprise shell (Navbar + Sidebar).

## API Integration Guide
Every controller has a dedicated service file. All requests use the `apiClient` instance which automatically:
- Attaches the JWT Bearer token.
- Handles global `401 Unauthorized` (Logout + Redirect).
- Handles global `403 Forbidden` (Dashboard Redirect + Toast).
