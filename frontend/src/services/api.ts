import { Document } from "@/types/document";

const API_BASE = "/api";

/**
 * Helper to transform raw API responses into typed objects.
 * Specifically converts date strings back to Date objects.
 */
const transformDocument = (doc: any): Document => ({
  ...doc,
  uploadedAt: new Date(doc.uploadedAt),
});

/**
 * Service layer for interacting with the backend REST API.
 * <p>
 * This object contains all the methods required to communicate with the Spring Boot backend:
 * <ul>
 *   <li>Document Management (List, Upload, Delete)</li>
 *   <li>Chat Interaction (Send Question)</li>
 * </ul>
 * All methods return Promises and throw Errors on failure.
 * </p>
 */
export const api = {
  // --- Document Operations ---

  /**
   * Fetches the list of all uploaded documents.
   * GET /api/documents
   * @returns Promise<Document[]> List of documents
   */
  getDocuments: async (): Promise<Document[]> => {
    const response = await fetch(`${API_BASE}/documents`);
    if (!response.ok) throw new Error("Failed to fetch documents");
    const data = await response.json();
    return data.map(transformDocument);
  },

  /**
   * Uploads a new file to the system.
   * POST /api/documents (Multipart Form Data)
   * <p>
   * This triggers the backend ingestion process (text extraction + vectorization).
   * </p>
   * @param file The browser File object selected by the user
   * @returns Promise<Document> The created document metadata
   */
  uploadDocument: async (file: File): Promise<Document> => {
    const formData = new FormData();
    formData.append("file", file);

    const response = await fetch(`${API_BASE}/documents`, {
      method: "POST",
      body: formData,
    });
    
    if (!response.ok) throw new Error("Failed to upload document");
    const data = await response.json();
    return transformDocument(data);
  },

  /**
   * Deletes a document and its associated vector embeddings.
   * DELETE /api/documents/:id
   * @param id The UUID of the document to delete
   */
  deleteDocument: async (id: string): Promise<void> => {
    const response = await fetch(`${API_BASE}/documents/${id}`, {
      method: "DELETE",
    });
    if (!response.ok) throw new Error("Failed to delete document");
  },

  // --- Chat Operations ---

  /**
   * Sends a question to the AI assistant.
   * POST /api/chat
   * <p>
   * The backend uses RAG to find relevant context from the uploaded documents
   * and generates an answer using the LLM.
   * </p>
   * @param question The user's question string
   * @returns Promise<string> The plain text answer from the AI
   */
  chat: async (question: string): Promise<string> => {
    const response = await fetch(`${API_BASE}/chat`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify({ question }),
    });

    if (!response.ok) throw new Error("Failed to send message");
    const data = await response.json();
    return data.answer;
  },
};
