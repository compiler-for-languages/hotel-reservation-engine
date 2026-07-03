interface ErrorStateProps {
  title?: string;
  message: string;
  onRetry?: () => void;
}

export const ErrorState = ({ title = "Something went wrong", message, onRetry }: ErrorStateProps) => (
  <div className="rounded-lg border border-rose-200 bg-rose-50 p-4 text-sm text-rose-700">
    <p className="font-semibold">{title}</p>
    <p className="mt-1">{message}</p>
    {onRetry ? (
      <button type="button" onClick={onRetry} className="mt-3 rounded border border-rose-300 px-3 py-1 text-xs font-medium">
        Retry
      </button>
    ) : null}
  </div>
);