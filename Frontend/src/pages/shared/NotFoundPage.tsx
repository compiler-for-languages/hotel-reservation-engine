import { Link } from "react-router-dom";

export default function NotFoundPage() {
  return (
    <div className="flex min-h-screen items-center justify-center bg-slate-100 p-4">
      <div className="space-y-3 rounded-lg border border-slate-200 bg-white p-8 text-center shadow-sm">
        <h1 className="text-2xl font-semibold text-slate-900">Page not found</h1>
        <p className="text-sm text-slate-600">The requested page does not exist.</p>
        <Link to="/login" className="inline-block rounded-md bg-slate-900 px-4 py-2 text-sm font-medium text-white">
          Go to Login
        </Link>
      </div>
    </div>
  );
}