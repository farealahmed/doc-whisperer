import { useState, useRef, useEffect } from "react";
import { Send, Sparkles, FileText, Loader2 } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Textarea } from "@/components/ui/textarea";
import { ScrollArea } from "@/components/ui/scroll-area";
import { ChatMessage } from "./ChatMessage";
import { Document, Message } from "@/types/document";
import { cn } from "@/lib/utils";

interface ChatInterfaceProps {
  /** The currently selected document context */
  document: Document | null;
  /** List of chat messages to display */
  messages: Message[];
  /** Callback to send a new message */
  onSendMessage: (content: string) => void;
  /** Whether the AI is currently processing a response */
  isLoading: boolean;
}

const suggestedQuestions = [
  "What is the main topic of this document?",
  "Summarize the key points",
  "What are the conclusions?",
  "List all mentioned figures or statistics",
];

/**
 * Chat Interface Component.
 * <p>
 * Displays the chat history and an input area for the user to ask questions.
 * Handles auto-scrolling to the latest message.
 * </p>
 */
export function ChatInterface({
  document,
  messages,
  onSendMessage,
  isLoading,
}: ChatInterfaceProps) {
  const [input, setInput] = useState("");
  const scrollAreaRef = useRef<HTMLDivElement>(null);
  const textareaRef = useRef<HTMLTextAreaElement>(null);

  useEffect(() => {
    // Auto-scroll to bottom on new messages
    if (scrollAreaRef.current) {
      const scrollContainer = scrollAreaRef.current.querySelector(
        "[data-radix-scroll-area-viewport]"
      );
      if (scrollContainer) {
        scrollContainer.scrollTop = scrollContainer.scrollHeight;
      }
    }
  }, [messages]);

  const handleSubmit = () => {
    if (input.trim() && !isLoading) {
      onSendMessage(input.trim());
      setInput("");
    }
  };

  const handleKeyDown = (e: React.KeyboardEvent) => {
    if (e.key === "Enter" && !e.shiftKey) {
      e.preventDefault();
      handleSubmit();
    }
  };

  if (!document) {
    return (
      <div className="flex flex-1 flex-col items-center justify-center bg-transparent p-8">
        <div className="mb-6 flex h-20 w-20 items-center justify-center rounded-2xl bg-accent">
          <FileText className="h-10 w-10 text-accent-foreground" />
        </div>
        <h2 className="mb-2 font-display text-2xl font-semibold text-foreground">
          Select a Document
        </h2>
        <p className="max-w-sm text-center text-muted-foreground">
          Choose a document from the sidebar or upload a new one to start asking
          questions
        </p>
      </div>
    );
  }

  return (
    <div className="flex flex-1 flex-col bg-transparent">
      {/* Header */}
      <div className="flex items-center gap-3 border-b border-border px-6 py-4">
        <div
          className={cn(
            "flex h-10 w-10 items-center justify-center rounded-lg",
            document.type === "pdf"
              ? "bg-destructive/10 text-destructive"
              : "bg-primary/10 text-primary"
          )}
        >
          <FileText className="h-5 w-5" />
        </div>
        <div>
          <h3 className="font-display font-semibold text-foreground">
            {document.name}
          </h3>
          <p className="text-xs text-muted-foreground">
            {document.pageCount
              ? `${document.pageCount} pages`
              : "Document loaded"}{" "}
            • Ask anything about this document
          </p>
        </div>
      </div>

      {/* Messages Area */}
      <ScrollArea ref={scrollAreaRef} className="flex-1 px-6">
        {messages.length === 0 ? (
          <div className="flex flex-col items-center justify-center py-16">
            <div className="mb-6 flex h-16 w-16 items-center justify-center rounded-2xl bg-gradient-to-br from-primary/20 to-primary/5">
              <Sparkles className="h-8 w-8 text-primary" />
            </div>
            <h3 className="mb-2 font-display text-xl font-semibold text-foreground">
              Start a Conversation
            </h3>
            <p className="mb-8 max-w-md text-center text-sm text-muted-foreground">
              Ask any question about "{document.name}" and get accurate answers
              with source references
            </p>

            {/* Suggested Questions */}
            <div className="grid max-w-lg gap-2 sm:grid-cols-2">
              {suggestedQuestions.map((question, index) => (
                <button
                  key={index}
                  onClick={() => onSendMessage(question)}
                  className="rounded-lg border border-border bg-card px-4 py-3 text-left text-sm text-foreground transition-all hover:border-primary/50 hover:bg-accent"
                >
                  {question}
                </button>
              ))}
            </div>
          </div>
        ) : (
          <div className="py-4">
            {messages.map((message) => (
              <ChatMessage key={message.id} message={message} />
            ))}
            {isLoading && (
              <div className="flex items-center gap-3 py-4">
                <div className="flex h-8 w-8 items-center justify-center rounded-full bg-muted">
                  <Loader2 className="h-4 w-4 animate-spin text-muted-foreground" />
                </div>
                <div className="flex items-center gap-2 text-sm text-muted-foreground">
                  <span className="animate-pulse-soft">
                    Searching document and generating response...
                  </span>
                </div>
              </div>
            )}
          </div>
        )}
      </ScrollArea>

      {/* Input Area */}
      <div className="border-t border-border p-4">
        <div className="relative mx-auto max-w-3xl">
          <Textarea
            ref={textareaRef}
            value={input}
            onChange={(e) => setInput(e.target.value)}
            onKeyDown={handleKeyDown}
            placeholder="Ask a question about this document..."
            className="min-h-[52px] resize-none pr-14"
            rows={1}
            disabled={isLoading}
          />
          <Button
            size="icon"
            className="absolute bottom-2 right-2"
            onClick={handleSubmit}
            disabled={!input.trim() || isLoading}
          >
            <Send className="h-4 w-4" />
          </Button>
        </div>
        <p className="mt-2 text-center text-[11px] text-muted-foreground">
          Answers are generated from document content. Verify important
          information.
        </p>
      </div>
    </div>
  );
}
