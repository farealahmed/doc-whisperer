export interface Document {
  id: string;
  name: string;
  type: 'pdf' | 'docx';
  size: number;
  uploadedAt: Date;
  pageCount?: number;
}

export interface Message {
  id: string;
  role: 'user' | 'assistant';
  content: string;
  timestamp: Date;
  sources?: {
    page: number;
    excerpt: string;
  }[];
}

export interface Conversation {
  id: string;
  documentId: string;
  messages: Message[];
  createdAt: Date;
}
