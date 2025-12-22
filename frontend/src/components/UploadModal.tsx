import { useState, useCallback } from "react";
import { Upload, FileText, X, CheckCircle2, Loader2 } from "lucide-react";
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import { Button } from "@/components/ui/button";
import { Progress } from "@/components/ui/progress";
import { cn } from "@/lib/utils";

interface UploadModalProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  onUpload: (files: File[]) => void;
}

interface UploadingFile {
  file: File;
  progress: number;
  status: "uploading" | "processing" | "complete" | "error";
}

export function UploadModal({ open, onOpenChange, onUpload }: UploadModalProps) {
  const [isDragging, setIsDragging] = useState(false);
  const [uploadingFiles, setUploadingFiles] = useState<UploadingFile[]>([]);

  const handleDragOver = useCallback((e: React.DragEvent) => {
    e.preventDefault();
    setIsDragging(true);
  }, []);

  const handleDragLeave = useCallback((e: React.DragEvent) => {
    e.preventDefault();
    setIsDragging(false);
  }, []);

  const simulateUpload = useCallback((files: File[]) => {
    const newFiles: UploadingFile[] = files.map((file) => ({
      file,
      progress: 0,
      status: "uploading" as const,
    }));

    setUploadingFiles(newFiles);

    // Simulate upload progress
    newFiles.forEach((uploadFile, index) => {
      let progress = 0;
      const interval = setInterval(() => {
        progress += Math.random() * 25;
        if (progress >= 100) {
          progress = 100;
          clearInterval(interval);
          setUploadingFiles((prev) =>
            prev.map((f, i) =>
              i === index ? { ...f, progress: 100, status: "processing" } : f
            )
          );
          // Simulate processing
          setTimeout(() => {
            setUploadingFiles((prev) =>
              prev.map((f, i) =>
                i === index ? { ...f, status: "complete" } : f
              )
            );
          }, 1000);
        } else {
          setUploadingFiles((prev) =>
            prev.map((f, i) => (i === index ? { ...f, progress } : f))
          );
        }
      }, 200);
    });

    // Call onUpload after simulation
    setTimeout(() => {
      onUpload(files);
      setTimeout(() => {
        setUploadingFiles([]);
        onOpenChange(false);
      }, 1500);
    }, 2500);
  }, [onUpload, onOpenChange]);

  const handleDrop = useCallback(
    (e: React.DragEvent) => {
      e.preventDefault();
      setIsDragging(false);
      const files = Array.from(e.dataTransfer.files).filter(
        (file) =>
          file.type === "application/pdf" ||
          file.type ===
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
      );
      if (files.length > 0) {
        simulateUpload(files);
      }
    },
    [simulateUpload]
  );

  const handleFileSelect = useCallback(
    (e: React.ChangeEvent<HTMLInputElement>) => {
      const files = Array.from(e.target.files || []);
      if (files.length > 0) {
        simulateUpload(files);
      }
    },
    [simulateUpload]
  );

  const formatFileSize = (bytes: number) => {
    if (bytes < 1024) return bytes + " B";
    if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + " KB";
    return (bytes / (1024 * 1024)).toFixed(1) + " MB";
  };

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="sm:max-w-lg">
        <DialogHeader>
          <DialogTitle className="font-display text-xl">
            Upload Documents
          </DialogTitle>
        </DialogHeader>

        <div className="space-y-4 py-4">
          {/* Drop Zone */}
          <div
            className={cn(
              "relative flex flex-col items-center justify-center rounded-xl border-2 border-dashed p-8 transition-all duration-200",
              isDragging
                ? "border-primary bg-accent"
                : "border-border hover:border-primary/50 hover:bg-accent/50"
            )}
            onDragOver={handleDragOver}
            onDragLeave={handleDragLeave}
            onDrop={handleDrop}
          >
            <div
              className={cn(
                "mb-4 flex h-14 w-14 items-center justify-center rounded-full transition-all",
                isDragging ? "bg-primary text-primary-foreground" : "bg-muted"
              )}
            >
              <Upload className="h-6 w-6" />
            </div>
            <p className="mb-1 text-sm font-medium text-foreground">
              Drag and drop your files here
            </p>
            <p className="mb-4 text-xs text-muted-foreground">
              Supports PDF and DOCX files up to 500+ pages
            </p>
            <label>
              <input
                type="file"
                className="hidden"
                accept=".pdf,.docx"
                multiple
                onChange={handleFileSelect}
              />
              <Button variant="outline" size="sm" asChild>
                <span className="cursor-pointer">Browse Files</span>
              </Button>
            </label>
          </div>

          {/* Uploading Files */}
          {uploadingFiles.length > 0 && (
            <div className="space-y-3">
              {uploadingFiles.map((item, index) => (
                <div
                  key={index}
                  className="animate-fade-in flex items-center gap-3 rounded-lg border border-border bg-card p-3"
                >
                  <div
                    className={cn(
                      "flex h-10 w-10 shrink-0 items-center justify-center rounded-md",
                      item.file.type === "application/pdf"
                        ? "bg-destructive/10 text-destructive"
                        : "bg-primary/10 text-primary"
                    )}
                  >
                    <FileText className="h-5 w-5" />
                  </div>
                  <div className="min-w-0 flex-1">
                    <p className="truncate text-sm font-medium">
                      {item.file.name}
                    </p>
                    <p className="text-xs text-muted-foreground">
                      {formatFileSize(item.file.size)}
                    </p>
                    {item.status === "uploading" && (
                      <Progress value={item.progress} className="mt-2 h-1" />
                    )}
                    {item.status === "processing" && (
                      <p className="mt-1 flex items-center gap-1.5 text-xs text-primary">
                        <Loader2 className="h-3 w-3 animate-spin" />
                        Processing & indexing...
                      </p>
                    )}
                  </div>
                  {item.status === "complete" && (
                    <CheckCircle2 className="h-5 w-5 shrink-0 text-green-500" />
                  )}
                  {item.status === "uploading" && (
                    <Button
                      variant="ghost"
                      size="icon"
                      className="h-7 w-7 shrink-0"
                    >
                      <X className="h-4 w-4" />
                    </Button>
                  )}
                </div>
              ))}
            </div>
          )}
        </div>
      </DialogContent>
    </Dialog>
  );
}
