import { AxiosError } from "axios";

const TECHNICAL_ERROR_PATTERN =
  /could not execute statement|sqlstate|sql exception|sqlexception|org\.hibernate|hibernateexception|jdbc|postgresql|mysql|constraint violation|foreign key constraint|violates foreign key|violates unique constraint|duplicate key|data integrity|referential integrity|org\.springframework\.dao|detached entity|persistence exception|constraint |jwt expired|token expired|expired.*milliseconds/i;

const isTechnicalErrorMessage = (message: string) => TECHNICAL_ERROR_PATTERN.test(message);

const normalizeApiMessage = (message: string) => {
  const trimmed = message.trim();
  if (!trimmed || isTechnicalErrorMessage(trimmed)) {
    return null;
  }
  return trimmed.endsWith(".") ? trimmed : `${trimmed}.`;
};

export const getApiErrorMessage = (error: unknown, fallback: string) => {
  if (error instanceof AxiosError) {
    const message = error.response?.data?.message;
    if (typeof message === "string") {
      const normalized = normalizeApiMessage(message);
      if (normalized) {
        return normalized;
      }
    }
  }

  return fallback.endsWith(".") ? fallback : `${fallback}.`;
};
