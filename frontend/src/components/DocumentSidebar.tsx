import { FileText, Upload, Search, Trash2 } from "lucide-react";
import { Input } from "@/components/ui/input";
import { Button } from "@/components/ui/button";
import { ScrollArea } from "@/components/ui/scroll-area";
import { Document } from "@/types/document";
import { cn } from "@/lib/utils";

interface DocumentSidebarProps {
  /** List of available documents */
  documents: Document[];
  /** Currently active document */
  selectedDocument: Document | null;
  /** Callback when a document is clicked */
  onSelectDocument: (doc: Document) => void;
  /** Callback to trigger upload modal */
  onUploadClick: () => void;
  /** Callback to delete a document */
  onDeleteDocument: (id: string) => void;
  /** Current search filter text */
  searchQuery: string;
  /** Callback to update search filter */
  onSearchChange: (query: string) => void;
}

/**
 * Sidebar Component for Document Management.
 * <p>
 * Displays a list of uploaded documents with search functionality.
 * Allows users to select, upload, and delete documents.
 * </p>
 */
export function DocumentSidebar({
  documents,
  selectedDocument,
  onSelectDocument,
  onUploadClick,
  onDeleteDocument,
  searchQuery,
  onSearchChange,
}: DocumentSidebarProps) {
  const filteredDocs = documents.filter((doc) =>
    doc.name.toLowerCase().includes(searchQuery.toLowerCase())
  );

  const formatFileSize = (bytes: number) => {
    if (bytes < 1024) return bytes + " B";
    if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + " KB";
    return (bytes / (1024 * 1024)).toFixed(1) + " MB";
  };

  const formatDate = (date: Date) => {
    return new Intl.DateTimeFormat("en-US", {
      month: "short",
      day: "numeric",
    }).format(date);
  };

  return (
    <div className="flex h-full w-72 flex-col border-r border-border bg-card/80 backdrop-blur-sm">
      {/* Header */}
      <div className="flex items-center justify-between border-b border-border px-4 py-4">
        <h2 className="font-display text-lg font-semibold text-foreground">
          Documents
        </h2>
        <Button
          size="sm"
          onClick={onUploadClick}
          className="gap-1.5"
        >
          <Upload className="h-4 w-4" />
          Upload
        </Button>
      </div>

      {/* Search */}
      <div className="px-4 py-3">
        <div className="relative">
          <Search className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
          <Input
            placeholder="Search documents..."
            value={searchQuery}
            onChange={(e) => onSearchChange(e.target.value)}
            className="pl-9"
          />
        </div>
      </div>

      {/* Document List */}
      <ScrollArea className="flex-1 px-2">
        <div className="space-y-1 py-2">
          {filteredDocs.length === 0 ? (
            <div className="flex flex-col items-center justify-center py-12 text-center">
              <FileText className="mb-3 h-10 w-10 text-muted-foreground/50" />
              <p className="text-sm text-muted-foreground">
                {searchQuery ? "No documents found" : "No documents yet"}
              </p>
              {!searchQuery && (
                <p className="mt-1 text-xs text-muted-foreground/70">
                  Upload a PDF or DOCX to get started
                </p>
              )}
            </div>
          ) : (
            filteredDocs.map((doc, index) => (
              <div
                key={doc.id}
                className={cn(
                  "group flex cursor-pointer items-center gap-3 rounded-lg px-3 py-2.5 transition-all duration-200",
                  "hover:bg-accent",
                  selectedDocument?.id === doc.id
                    ? "bg-accent text-accent-foreground"
                    : "text-foreground"
                )}
                style={{ animationDelay: `${index * 50}ms` }}
                onClick={() => onSelectDocument(doc)}
              >
                <div
                  className={cn(
                    "flex h-9 w-9 shrink-0 items-center justify-center rounded-md",
                    doc.type === "pdf"
                      ? "bg-destructive/10 text-destructive"
                      : "bg-primary/10 text-primary"
                  )}
                >
                  <FileText className="h-4 w-4" />
                </div>
                <div className="min-w-0 flex-1">
                  <p className="truncate text-sm font-medium">{doc.name}</p>
                  <p className="text-xs text-muted-foreground">
                    {formatFileSize(doc.size)} • {formatDate(doc.uploadedAt)}
                  </p>
                </div>
                <Button
                  variant="ghost"
                  size="icon"
                  className="h-7 w-7 text-muted-foreground opacity-0 transition-opacity hover:text-destructive group-hover:opacity-100"
                  onClick={(e) => {
                    e.stopPropagation();
                    onDeleteDocument(doc.id);
                  }}
                >
                  <Trash2 className="h-4 w-4" />
                </Button>
              </div>
            ))
          )}
        </div>
      </ScrollArea>

      {/* Footer Stats */}
      <div className="border-t border-border px-4 py-3">
        <p className="text-xs text-muted-foreground">
          {documents.length} document{documents.length !== 1 ? "s" : ""} indexed
        </p>
      </div>
    </div>
  );
}
