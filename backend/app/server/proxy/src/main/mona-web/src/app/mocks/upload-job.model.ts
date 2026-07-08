/**
 * Mirrors the backend UploadJob entity. One row per upload, the source of truth for transfer and
 * parsing progress so the My Uploads page survives a refresh instead of losing in flight uploads
 */
export interface UploadJobModel {
  id: string;
  emailAddress: string;
  fileName: string;
  format: string;
  status: string;
  fileSize: number;
  uploadedBytes: number;
  total: number;
  parsed: number;
  persisted: number;
  failed: number;
  libraryId: number;
  libraryName: string;
  date: string;
  lastUpdated: string;
  errorMessage: string;
}
