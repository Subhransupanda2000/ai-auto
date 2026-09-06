export interface ErrorResponse {
  timestamp: string;
  status: number;
  error: string;
  message: string;
  path: string;
  details?: string[] | null;
}

export interface ApiError {
  status: number;
  message: string;
  details?: string[] | null;
}
