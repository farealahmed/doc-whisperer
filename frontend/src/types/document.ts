/**
 * Represents the metadata of a document in the system.
 * <p>
 * This interface mirrors the backend's Document entity structure.
 * It is used for displaying document lists and handling file selection.
 * </p>
 */
export interface Document {
  /** Unique identifier (UUID) */
  id: string;
  /** Original filename (e.g., "report.pdf") */
  name: string;
  /** MIME type (e.g., "application/pdf") */
  type: string;
  /** File size in bytes */
  size: number;
  /** Timestamp when the document was uploaded */
  uploadedAt: Date;
  /** Number of pages (optional, may be null if not extracted) */
  pageCount?: number;
}

/**
 * Represents a single message in a chat conversation.
 * <p>
 * Used to render the chat history between the user and the AI assistant.
 * </p>
 */
export interface Message {
  /** Unique ID for the message (client-generated for optimistic UI) */
  id: string;
  /** The sender of the message: 'user' or 'assistant' */
  role: 'user' | 'assistant';
  /** The text content of the message */
  content: string;
  /** When the message was sent/received */
  timestamp: Date;
  /**
   * Optional citations/sources used by the AI to generate the answer.
   * Useful for RAG (Retrieval-Augmented Generation) transparency.
   */
  sources?: {
    page: number;
    excerpt: string;
  }[];
}

/**
 * Represents a full conversation session associated with a specific document.
 * <p>
 * (Currently reserved for future use to support multiple chat sessions per document)
 * </p>
 */
export interface Conversation {
  id: string;
  documentId: string;
  messages: Message[];
  createdAt: Date;
}
