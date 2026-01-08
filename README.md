# 🤖 Doc Whisperer

**Doc Whisperer** is an AI-powered document assistant that lets you upload PDF and DOCX files and ask questions about their content using natural language. Built with Retrieval-Augmented Generation (RAG) technology, it combines semantic search with local LLM inference to provide accurate, context-aware answers.

![Tech Stack](https://img.shields.io/badge/Java-21-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.9-green)
![React](https://img.shields.io/badge/React-18.3-blue)
![TypeScript](https://img.shields.io/badge/TypeScript-5.8-blue)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue)

---

## 📋 Table of Contents

- [Features](#-features)
- [Architecture](#-architecture)
- [Technology Stack](#-technology-stack)
- [Prerequisites](#-prerequisites)
- [Installation & Setup](#-installation--setup)
- [Running the Application](#-running-the-application)
- [How It Works](#-how-it-works)
- [API Documentation](#-api-documentation)
- [Testing](#-testing)
- [Project Structure](#-project-structure)
- [Configuration](#-configuration)
- [Troubleshooting](#-troubleshooting)

---

## ✨ Features

- **📄 Document Upload**: Support for PDF and DOCX files (up to 50MB)
- **💬 Intelligent Chat**: Ask questions about your documents in natural language
- **🔍 Semantic Search**: Uses vector embeddings for accurate content retrieval
- **🤖 Local LLM**: Powered by Ollama (llama3) - runs entirely on your machine
- **📚 Multi-Document Support**: Upload and query multiple documents
- **⚡ Real-time Responses**: Fast, context-aware answers
- **🎨 Modern UI**: Clean, responsive interface built with React and Tailwind CSS
- **🔒 Privacy-First**: All processing happens locally - no data sent to external APIs

---

## 🏗️ Architecture

Doc Whisperer uses a **RAG (Retrieval-Augmented Generation)** pipeline:

```
┌─────────────────┐
│  User uploads   │
│   PDF/DOCX      │
└────────┬────────┘
         │
         ▼
┌─────────────────────────────────────────┐
│  Apache Tika extracts text              │
└────────┬────────────────────────────────┘
         │
         ▼
┌─────────────────────────────────────────┐
│  Text split into chunks                 │
└────────┬────────────────────────────────┘
         │
         ▼
┌─────────────────────────────────────────┐
│  Embeddings generated (MiniLM-L6-v2)    │
│  384-dimensional vectors                │
└────────┬────────────────────────────────┘
         │
         ▼
┌─────────────────────────────────────────┐
│  Stored in PostgreSQL (pgvector)        │
└─────────────────────────────────────────┘

         [USER ASKS QUESTION]
                  │
                  ▼
┌─────────────────────────────────────────┐
│  Question → Embedding conversion        │
└────────┬────────────────────────────────┘
         │
         ▼
┌─────────────────────────────────────────┐
│  Vector similarity search (pgvector)    │
│  Find most relevant text chunks         │
└────────┬────────────────────────────────┘
         │
         ▼
┌─────────────────────────────────────────┐
│  Context + Question → Ollama (llama3)   │
└────────┬────────────────────────────────┘
         │
         ▼
┌─────────────────────────────────────────┐
│  AI generates context-aware answer      │
└─────────────────────────────────────────┘
```

---

## 🛠️ Technology Stack

### Backend
- **Framework**: Spring Boot 3.5.9
- **Language**: Java 21
- **AI/RAG**: LangChain4j (0.30.0)
- **LLM**: Ollama (llama3 model)
- **Embeddings**: all-MiniLM-L6-v2 (384 dimensions)
- **Database**: PostgreSQL 16 with pgvector extension
- **Document Parsing**: Apache Tika
- **API Documentation**: OpenAPI 3 / Swagger UI
- **Build Tool**: Maven

### Frontend
- **Framework**: React 18.3
- **Language**: TypeScript 5.8
- **Build Tool**: Vite 5.4
- **Styling**: Tailwind CSS
- **UI Components**: Shadcn/UI (Radix UI)
- **State Management**: TanStack Query (React Query)
- **Routing**: React Router DOM 6

### Infrastructure
- **Containerization**: Docker & Docker Compose
- **Database**: PostgreSQL with pgvector

---

## 📦 Prerequisites

Before running Doc Whisperer, ensure you have the following installed:

### Required
- **Java 21** or higher ([Download](https://adoptium.net/))
- **Maven 3.8+** ([Download](https://maven.apache.org/download.cgi))
- **Node.js 18+** and npm ([Download](https://nodejs.org/))
- **Docker & Docker Compose** ([Download](https://www.docker.com/products/docker-desktop))
- **Ollama** ([Download](https://ollama.ai/))

### Verify Installation
```bash
java -version    # Should show Java 21+
mvn -version     # Should show Maven 3.8+
node -version    # Should show Node 18+
docker --version # Should show Docker 20+
ollama --version # Should show Ollama installed
```

---

## 🚀 Installation & Setup

### 1. Clone the Repository
```bash
git clone https://github.com/farealahmed/doc-whisperer.git
cd doc-whisperer
```

### 2. Start PostgreSQL with pgvector
```bash
cd backend
docker-compose up -d
```

This will start PostgreSQL on port `5433` with the `pgvector` extension enabled.

Verify the database is running:
```bash
docker ps
# Should show doc-whisperer-db container running
```

### 3. Install and Configure Ollama

#### Install Ollama
Follow the instructions at [ollama.ai](https://ollama.ai/) for your operating system.

#### Pull the llama3 Model
```bash
ollama pull llama3
```

#### Start Ollama Server
```bash
ollama serve
```

The server will run on `http://localhost:11434` by default.

### 4. Backend Setup

#### Navigate to backend directory
```bash
cd backend
```

#### Install Maven Dependencies
```bash
mvn clean install
```

This will:
- Download all required dependencies
- Compile the Java code
- Run tests

### 5. Frontend Setup

#### Navigate to frontend directory
```bash
cd ../frontend
```

#### Install Node Dependencies
```bash
npm install
```

---

## ▶️ Running the Application

You'll need **three separate terminal windows**:

### Terminal 1: PostgreSQL (if not already running)
```bash
cd backend
docker-compose up
```

### Terminal 2: Backend (Spring Boot)
```bash
cd backend
mvn spring-boot:run
```

The backend will start on `http://localhost:8080`

**Expected output:**
```
Started BackendApplication in X seconds
```

### Terminal 3: Frontend (React)
```bash
cd frontend
npm run dev
```

The frontend will start on `http://localhost:5173`

**Expected output:**
```
VITE vX.X.X ready in XXX ms

➜  Local:   http://localhost:5173/
➜  Network: use --host to expose
```

### 6. Access the Application

Open your browser and navigate to:
```
http://localhost:5173
```

---

## 📖 How It Works

### Upload Flow
1. **User uploads** a PDF or DOCX file through the web interface
2. **Apache Tika** extracts raw text from the document
3. **Text chunking**: Content is split into semantic chunks (sentences/paragraphs)
4. **Embedding generation**: Each chunk is converted to a 384-dimensional vector using `all-MiniLM-L6-v2`
5. **Storage**: Vectors are stored in PostgreSQL with metadata using the `pgvector` extension

### Chat Flow
1. **User asks** a question in the chat interface
2. **Question embedding**: The question is converted to a vector
3. **Similarity search**: pgvector finds the most relevant document chunks using cosine similarity
4. **Context building**: Top matching chunks are retrieved
5. **LLM inference**: Question + context is sent to Ollama (llama3)
6. **Response generation**: The LLM generates a context-aware answer
7. **Display**: Answer is streamed back to the UI

---

## 🔌 API Documentation

### Interactive API Documentation (Swagger UI)

The application includes **interactive API documentation** powered by OpenAPI 3 and Swagger UI.

**Access Swagger UI:**
```
http://localhost:8080/swagger-ui.html
```

**OpenAPI Specification:**
```
http://localhost:8080/v3/api-docs
```

Swagger UI provides:
- 📝 Complete API documentation with request/response schemas
- 🧪 Interactive testing - Try out endpoints directly from the browser
- 📋 Auto-generated from your code - Always up-to-date
- 🔍 Detailed parameter descriptions and examples

### Base URL
```
http://localhost:8080/api
```

### Endpoints

#### 1. Upload Document
```http
POST /documents
Content-Type: multipart/form-data

Request:
- file: (binary) PDF or DOCX file

Response: 200 OK
{
  "id": "uuid",
  "name": "document.pdf",
  "type": "application/pdf",
  "size": 1234567,
  "uploadedAt": "2026-02-01T11:54:45Z",
  "pageCount": 10
}
```

#### 2. List All Documents
```http
GET /documents

Response: 200 OK
[
  {
    "id": "uuid",
    "name": "document.pdf",
    "type": "application/pdf",
    "size": 1234567,
    "uploadedAt": "2026-02-01T11:54:45Z",
    "pageCount": 10
  }
]
```

#### 3. Delete Document
```http
DELETE /documents/{id}

Response: 204 No Content
```

#### 4. Chat with AI
```http
POST /chat
Content-Type: application/json

Request:
{
  "question": "What is the main topic of the document?",
  "documentId": "uuid (optional)"
}

Response: 200 OK
{
  "answer": "The main topic of the document is..."
}
```

---

## 🧪 Testing

### Run Unit Tests

#### ChatService Unit Tests
```bash
cd backend
./mvnw test -Dtest=ChatServiceTest -q
```

#### Run All Tests
```bash
cd backend
./mvnw test
```

---

## 📁 Project Structure

```
doc-whisperer/
├── backend/                          # Spring Boot Backend
│   ├── src/main/java/com/docwhisperer/backend/
│   │   ├── BackendApplication.java   # Main application entry point
│   │   ├── config/
│   │   │   └── ChatConfiguration.java # LangChain4j AI config
│   │   ├── controllers/
│   │   │   ├── ChatController.java   # REST: /api/chat
│   │   │   └── DocumentController.java # REST: /api/documents
│   │   ├── services/
│   │   │   ├── ChatService.java      # RAG pipeline logic
│   │   │   └── DocumentService.java  # Document processing
│   │   ├── repositories/
│   │   │   └── DocumentRepository.java # Database access
│   │   └── documents/
│   │       └── Document.java         # Entity model
│   ├── src/main/resources/
│   │   ├── application.properties    # Configuration
│   │   └── sql/schema.sql            # Database schema
│   ├── pom.xml                       # Maven dependencies
│   └── docker-compose.yaml           # PostgreSQL setup
│
├── frontend/                         # React Frontend
│   ├── src/
│   │   ├── components/
│   │   │   ├── ChatInterface.tsx     # Main chat UI
│   │   │   ├── DocumentSidebar.tsx   # Document list
│   │   │   ├── UploadModal.tsx       # File upload UI
│   │   │   └── ui/                   # Shadcn components
│   │   ├── pages/
│   │   │   ├── Index.tsx             # Main app page
│   │   │   ├── Welcome.tsx           # Landing page
│   │   │   └── NotFound.tsx          # 404 page
│   │   ├── services/
│   │   │   └── api.ts                # Backend API client
│   │   ├── types/
│   │   │   └── document.ts           # TypeScript types
│   │   ├── App.tsx                   # Root component
│   │   └── main.tsx                  # React entry point
│   ├── package.json                  # npm dependencies
│   └── vite.config.ts                # Vite configuration
│
└── README.md                         # This file
```

---

## ⚙️ Configuration

### Backend Configuration
Edit `backend/src/main/resources/application.properties`:

```properties
# Database
spring.datasource.url=jdbc:postgresql://localhost:5433/docwhisperer
spring.datasource.username=postgres
spring.datasource.password=password

# File Upload Limits
spring.servlet.multipart.max-file-size=50MB
spring.servlet.multipart.max-request-size=50MB

# Ollama LLM
langchain4j.ollama.chat-model.base-url=http://localhost:11434
langchain4j.ollama.chat-model.model-name=llama3
langchain4j.ollama.chat-model.timeout=PT120S

# PgVector
langchain4j.pgvector.dimension=384
langchain4j.pgvector.table=embeddings
```

### Frontend Configuration
The frontend uses a proxy in development. API calls to `/api/*` are automatically forwarded to `http://localhost:8080`.

Edit `frontend/vite.config.ts` to change the backend URL if needed.

---

## 🐛 Troubleshooting

### Issue: Backend fails to start with "Connection refused" to PostgreSQL
**Solution:**
```bash
# Verify database is running
docker ps

# Restart the database
cd backend
docker-compose down
docker-compose up -d

# Check logs
docker logs doc-whisperer-db
```

### Issue: Ollama connection error
**Solution:**
```bash
# Ensure Ollama is running
ollama serve

# Verify llama3 model is installed
ollama list

# If not installed:
ollama pull llama3
```

### Issue: Frontend can't connect to backend
**Solution:**
- Verify backend is running on port 8080
- Check console for CORS errors
- Ensure no firewall is blocking port 8080

### Issue: Document upload fails
**Solution:**
- Check file size (max 50MB)
- Verify file format is PDF or DOCX
- Check backend logs for errors: `cd backend && mvn spring-boot:run`

### Issue: Slow AI responses
**Solution:**
- Ollama/llama3 requires significant RAM (8GB+)
- Consider using a smaller model: `ollama pull llama3.2`
- Update `application.properties` to use the smaller model

---

## 🎯 Usage Tips

1. **First upload**: Start with a small document (1-5 pages) to test the system
2. **Clear questions**: Ask specific questions rather than vague queries
3. **Document context**: Reference specific topics from your document
4. **Multiple documents**: You can upload multiple documents and ask questions across all of them

---

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

---

## 📄 License

This project is for educational and portfolio purposes.

---

## 👨‍💻 Author

**Fareal Ahmed**
- GitHub: [@farealahmed](https://github.com/farealahmed)
- Project: [doc-whisperer](https://github.com/farealahmed/doc-whisperer)

---

## 🙏 Acknowledgments

- [LangChain4j](https://github.com/langchain4j/langchain4j) - RAG framework for Java
- [Ollama](https://ollama.ai/) - Local LLM runtime
- [pgvector](https://github.com/pgvector/pgvector) - Vector similarity search for PostgreSQL
- [Apache Tika](https://tika.apache.org/) - Document parsing
- [Shadcn/UI](https://ui.shadcn.com/) - React component library

---

**Happy Document Querying! 🚀**
