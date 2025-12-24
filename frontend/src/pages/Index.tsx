import { useState, useCallback, useEffect } from "react";
import { DocumentSidebar } from "@/components/DocumentSidebar";
import { ChatInterface } from "@/components/ChatInterface";
import { UploadModal } from "@/components/UploadModal";
import { Document, Message } from "@/types/document";
import { useToast } from "@/hooks/use-toast";
import { api } from "@/services/api";

/**
 * Main Application Page.
 * <p>
 * This component acts as the primary controller for the application state.
 * It manages:
 * <ul>
 *   <li>The list of documents (fetched from backend)</li>
 *   <li>The currently selected document</li>
 *   <li>The chat history (messages)</li>
 *   <li>Upload modal visibility</li>
 * </ul>
 * It composes the Sidebar, Chat Interface, and Upload Modal components.
 * </p>
 */
const Index = () => {
  // --- State Management ---
  const [documents, setDocuments] = useState<Document[]>([]);
  const [selectedDocument, setSelectedDocument] = useState<Document | null>(null);
  const [searchQuery, setSearchQuery] = useState("");
  const [uploadModalOpen, setUploadModalOpen] = useState(false);
  const [messages, setMessages] = useState<Message[]>([]);
  const [isLoading, setIsLoading] = useState(false);
  const { toast } = useToast();

  /**
   * Initial Data Fetching.
   * Loads the list of documents when the component mounts.
   */
  useEffect(() => {
    loadDocuments();
  }, []);

  const loadDocuments = async () => {
    try {
      const docs = await api.getDocuments();
      setDocuments(docs);
    } catch (error) {
      toast({
        title: "Error",
        description: "Failed to load documents",
        variant: "destructive",
      });
    }
  };

  /**
   * Handles document selection from the sidebar.
   * Resets the chat history when a new document is selected.
   */
  const handleSelectDocument = useCallback((doc: Document) => {
    setSelectedDocument(doc);
    setMessages([]); // Reset messages when switching documents
  }, []);

  /**
   * Handles file uploads via the UploadModal.
   * Uploads files to the backend and updates the document list.
   */
  const handleUpload = useCallback(
    async (files: File[]) => {
      try {
        const uploadPromises = files.map((file) => api.uploadDocument(file));
        const newDocs = await Promise.all(uploadPromises);
        
        setDocuments((prev) => [...prev, ...newDocs]);
        toast({
          title: "Documents uploaded",
          description: `${files.length} document${files.length > 1 ? "s" : ""} indexed successfully`,
        });
        loadDocuments(); // Reload to get fresh list
      } catch (error) {
        toast({
          title: "Upload failed",
          description: "One or more files failed to upload",
          variant: "destructive",
        });
      }
    },
    [toast]
  );

  /**
   * Handles document deletion.
   * Removes the document from the list and clears selection if needed.
   */
  const handleDeleteDocument = useCallback(
    async (id: string) => {
      try {
        await api.deleteDocument(id);
        setDocuments((prev) => prev.filter((doc) => doc.id !== id));
        if (selectedDocument?.id === id) {
          setSelectedDocument(null);
          setMessages([]);
        }
        toast({
          title: "Document deleted",
          description: "The document has been removed from the index",
        });
      } catch (error) {
        toast({
          title: "Delete failed",
          description: "Could not delete document",
          variant: "destructive",
        });
      }
    },
    [selectedDocument, toast]
  );

  /**
   * Handles sending a message in the chat interface.
   * 1. Adds the user's message to the state immediately (Optimistic UI).
   * 2. Calls the backend API to get the AI response.
   * 3. Adds the AI's response to the state.
   */
  const handleSendMessage = useCallback(async (content: string) => {
    // Add user message
    const userMessage: Message = {
      id: `msg-${Date.now()}`,
      role: "user",
      content,
      timestamp: new Date(),
    };
    setMessages((prev) => [...prev, userMessage]);
    setIsLoading(true);

    try {
      const answer = await api.chat(content, selectedDocument?.id);
      
      const aiMessage: Message = {
        id: `msg-${Date.now()}-ai`,
        role: "assistant",
        content: answer,
        timestamp: new Date(),
      };
      setMessages((prev) => [...prev, aiMessage]);
    } catch (error) {
      toast({
        title: "Chat failed",
        description: "Could not get response from AI",
        variant: "destructive",
      });
    } finally {
      setIsLoading(false);
    }
  }, [selectedDocument, toast]);

  return (
    <div className="flex h-screen w-full overflow-hidden bg-gradient-to-br from-background via-background to-accent/20">
      {/* Sidebar for document management */}
      <DocumentSidebar
        documents={documents}
        selectedDocument={selectedDocument}
        onSelectDocument={handleSelectDocument}
        onUploadClick={() => setUploadModalOpen(true)}
        onDeleteDocument={handleDeleteDocument}
        searchQuery={searchQuery}
        onSearchChange={setSearchQuery}
      />

      {/* Main Chat Interface */}
      <ChatInterface
        document={selectedDocument}
        messages={messages}
        onSendMessage={handleSendMessage}
        isLoading={isLoading}
      />

      {/* Modal for file uploads */}
      <UploadModal
        open={uploadModalOpen}
        onOpenChange={setUploadModalOpen}
        onUpload={handleUpload}
      />
    </div>
  );
};

export default Index;
