/**
 * Generic client-side "overlay" persistence used to layer create/update/delete
 * support on top of backend endpoints that are currently read-only (e.g.
 * Patients, Doctors). Overlay state is kept in localStorage so it survives
 * page refreshes; once the backend adds full CRUD endpoints, the
 * corresponding `services/*LocalService.ts` module can be deleted and its
 * callers pointed directly at `api/*Api.ts`.
 */
import { generateId } from '../utils/id';

interface OverlayState<T> {
  created: T[];
  updated: Record<string, Partial<T>>;
  deleted: string[];
}

function emptyState<T>(): OverlayState<T> {
  return { created: [], updated: {}, deleted: [] };
}

export class LocalOverlayStore<T extends { id: string }> {
  private readonly storageKey: string;

  constructor(storageKey: string) {
    this.storageKey = storageKey;
  }

  private read(): OverlayState<T> {
    const raw = localStorage.getItem(this.storageKey);
    if (!raw) return emptyState<T>();
    try {
      return JSON.parse(raw) as OverlayState<T>;
    } catch {
      return emptyState<T>();
    }
  }

  private write(state: OverlayState<T>): void {
    localStorage.setItem(this.storageKey, JSON.stringify(state));
  }

  /** Merge server-fetched records with local creates/updates/deletes. */
  merge(serverRecords: T[]): T[] {
    const state = this.read();
    const withoutDeleted = serverRecords.filter((r) => !state.deleted.includes(r.id));
    const withUpdates = withoutDeleted.map((r) =>
      state.updated[r.id] ? { ...r, ...state.updated[r.id] } : r,
    );
    return [...state.created, ...withUpdates];
  }

  create(record: Omit<T, 'id'> & { id?: string }): T {
    const state = this.read();
    const newRecord = { ...record, id: record.id ?? generateId() } as T;
    state.created = [newRecord, ...state.created];
    this.write(state);
    return newRecord;
  }

  update(id: string, patch: Partial<T>): void {
    const state = this.read();
    const localIndex = state.created.findIndex((r) => r.id === id);
    if (localIndex >= 0) {
      state.created[localIndex] = { ...state.created[localIndex], ...patch };
    } else {
      state.updated[id] = { ...state.updated[id], ...patch };
    }
    this.write(state);
  }

  remove(id: string): void {
    const state = this.read();
    const localIndex = state.created.findIndex((r) => r.id === id);
    if (localIndex >= 0) {
      state.created.splice(localIndex, 1);
    } else {
      state.deleted.push(id);
      delete state.updated[id];
    }
    this.write(state);
  }
}
