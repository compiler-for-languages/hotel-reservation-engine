import { useMemo, useState } from "react";
import type { ReactNode } from "react";
import { EmptyState } from "@/components/common/EmptyState";
import { ErrorState } from "@/components/common/ErrorState";
import { TableSkeleton } from "@/components/common/TableSkeleton";
import { cn } from "@/utils/cn";

interface DataTableColumn<T> {
  key: string;
  header: string;
  render: (row: T) => ReactNode;
  sortValue?: (row: T) => string | number;
  className?: string;
}

interface DataTableProps<T> {
  title?: string;
  data: T[];
  columns: DataTableColumn<T>[];
  rowKey: (row: T) => string | number;
  searchPlaceholder?: string;
  searchTextExtractor?: (row: T) => string;
  pageSize?: number;
  isLoading?: boolean;
  errorMessage?: string | null;
  onRetry?: () => void;
  emptyMessage?: string;
}

type SortDirection = "asc" | "desc";

export const DataTable = <T,>({
  title,
  data,
  columns,
  rowKey,
  searchPlaceholder = "Search...",
  searchTextExtractor,
  pageSize = 8,
  isLoading = false,
  errorMessage = null,
  onRetry,
  emptyMessage = "No records found for this section.",
}: DataTableProps<T>) => {
  const tableTitle = title ?? "Data table";
  const [search, setSearch] = useState("");
  const [page, setPage] = useState(1);
  const [sortKey, setSortKey] = useState<string | null>(null);
  const [sortDirection, setSortDirection] = useState<SortDirection>("asc");

  const filtered = useMemo(() => {
    const term = search.trim().toLowerCase();
    if (!term || !searchTextExtractor) {
      return data;
    }
    return data.filter((item) => searchTextExtractor(item).toLowerCase().includes(term));
  }, [data, search, searchTextExtractor]);

  const sorted = useMemo(() => {
    if (!sortKey) {
      return filtered;
    }

    const column = columns.find((item) => item.key === sortKey);
    if (!column?.sortValue) {
      return filtered;
    }

    return [...filtered].sort((a, b) => {
      const valueA = column.sortValue?.(a) ?? "";
      const valueB = column.sortValue?.(b) ?? "";

      if (valueA === valueB) {
        return 0;
      }

      if (sortDirection === "asc") {
        return valueA > valueB ? 1 : -1;
      }

      return valueA < valueB ? 1 : -1;
    });
  }, [columns, filtered, sortDirection, sortKey]);

  const pageCount = Math.max(1, Math.ceil(sorted.length / pageSize));
  const currentPage = Math.min(page, pageCount);

  const pagedData = useMemo(() => {
    const start = (currentPage - 1) * pageSize;
    return sorted.slice(start, start + pageSize);
  }, [currentPage, pageSize, sorted]);

  const onSort = (key: string) => {
    if (sortKey !== key) {
      setSortKey(key);
      setSortDirection("asc");
      return;
    }
    setSortDirection((prev) => (prev === "asc" ? "desc" : "asc"));
  };

  return (
    <div className="space-y-3 rounded-lg border border-slate-200 bg-white p-4 shadow-sm">
      <div className="flex flex-wrap items-center justify-between gap-3">
        <h3 className="text-base font-semibold text-slate-900">{title}</h3>
        <input
          aria-label={`${tableTitle} search`}
          value={search}
          onChange={(event) => {
            setSearch(event.target.value);
            setPage(1);
          }}
          className="w-full max-w-xs rounded-md border border-slate-300 px-3 py-2 text-sm"
          placeholder={searchPlaceholder}
        />
      </div>

      {isLoading ? <TableSkeleton /> : null}

      {!isLoading && errorMessage ? <ErrorState message={errorMessage} onRetry={onRetry} /> : null}

      {!isLoading && !errorMessage ? (
        <>
          <div className="space-y-3 md:hidden" aria-label={`${tableTitle} mobile list`}>
            {pagedData.map((row) => (
              <div key={rowKey(row)} className="rounded-md border border-slate-200 p-3">
                {columns.map((column) => (
                  <div key={column.key} className="flex items-start justify-between gap-3 py-1 text-sm">
                    <span className="text-slate-500">{column.header}</span>
                    <span className="text-right text-slate-700">{column.render(row)}</span>
                  </div>
                ))}
              </div>
            ))}
          </div>

          <div className="hidden overflow-x-auto md:block">
            <table className="min-w-full divide-y divide-slate-200 text-sm" aria-label={tableTitle}>
              <thead>
                <tr>
                  {columns.map((column) => (
                    <th key={column.key} className={cn("px-3 py-2 text-left font-semibold text-slate-600", column.className)}>
                      {column.sortValue ? (
                        <button
                          className="inline-flex items-center gap-1"
                          type="button"
                          aria-label={`Sort by ${column.header}`}
                          onClick={() => onSort(column.key)}
                        >
                          {column.header}
                          {sortKey === column.key ? (sortDirection === "asc" ? "▲" : "▼") : ""}
                        </button>
                      ) : (
                        column.header
                      )}
                    </th>
                  ))}
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100">
                {pagedData.map((row) => (
                  <tr key={rowKey(row)}>
                    {columns.map((column) => (
                      <td key={column.key} className={cn("whitespace-nowrap px-3 py-2 text-slate-700", column.className)}>
                        {column.render(row)}
                      </td>
                    ))}
                  </tr>
                ))}
              </tbody>
            </table>
          </div>

          {pagedData.length === 0 ? <EmptyState message={emptyMessage} /> : null}
        </>
      ) : null}

      <div className="flex items-center justify-between text-xs text-slate-500">
        <span>
          Page {currentPage} of {pageCount}
        </span>
        <div className="space-x-2">
          <button
            type="button"
            aria-label="Previous page"
            onClick={() => setPage((prev) => Math.max(1, prev - 1))}
            disabled={currentPage === 1}
            className="rounded border border-slate-300 px-2 py-1 disabled:opacity-50"
          >
            Prev
          </button>
          <button
            type="button"
            aria-label="Next page"
            onClick={() => setPage((prev) => Math.min(pageCount, prev + 1))}
            disabled={currentPage >= pageCount}
            className="rounded border border-slate-300 px-2 py-1 disabled:opacity-50"
          >
            Next
          </button>
        </div>
      </div>
    </div>
  );
};