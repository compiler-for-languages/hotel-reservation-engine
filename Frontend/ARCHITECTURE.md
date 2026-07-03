# Frontend Architecture Documentation

## 1. Directory Structure
The project follows a **Feature-Based Architecture**, ensuring high cohesion and low coupling.

```text
src/
├── components/   # Reusable Atomic UI (DataTable, Modal, etc.)
├── hooks/        # Custom React hooks (authBootstrap, mutations)
├── layouts/      # Application shells (AppLayout, AuthLayout)
├── pages/        # Role-specific screens grouped by domain
├── routes/       # Route definitions and RBAC guards
├── services/     # Axios-based API services (one per controller)
├── store/        # Zustand stores for client-side state
├── types/        # TypeScript DTOs and Enums (matching Swagger)
└── utils/        # Shared utilities (formatters, jwt, storage)
```

## 2. API Integration Layer
### Reusable Axios Instance (`apiClient.ts`)
- **Base URL:** Driven by `VITE_API_BASE_URL`.
- **JWT Injection:** Interceptor automatically adds `Authorization: Bearer <token>` from local storage or Zustand.
- **Global Error Handling:**
  - `401 Unauthorized`: Triggers a full session clear and redirects to `/login`.
  - `403 Forbidden`: Redirects the user to their role-specific dashboard with a toast warning.

## 3. State Management Strategy
- **Server State:** Handled by `@tanstack/react-query`. Caching, retries, and data lifecycle are managed at the component level using service methods.
- **Client State:** Handled by `zustand`.
  - `authStore.ts`: Manages session token and the `UserResponseDTO` object.
  - `uiStore.ts`: Manages sidebar collapse state and theme configuration.

## 4. Security & Role-Based Access Control (RBAC)
- **Protected Routes:** `ProtectedRoute.tsx` ensures only authenticated users can enter the app shell.
- **Role Guards:** `RoleRoute.tsx` compares the current user's role against an `allowedRoles` array to permit access to specific modules (ADMIN, CUSTOMER, RECEPTIONIST).

## 5. Design System
- **Colors & Spacing:** Tailwind CSS is extended with custom tokens defined in `src/index.css`.
- **Responsive Tables:** `DataTable.tsx` automatically switches to a Card-based layout on mobile devices.
