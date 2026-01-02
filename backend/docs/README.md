# Backend Development & Troubleshooting Log

This document details my journey in setting up, configuring, and troubleshooting the Spring Boot backend for the Doc Whisperer RAG application. It covers the architectural decisions I made, the specific errors I encountered, and the step-by-step solutions I implemented to achieve a stable running system.

## 1. Project Initialization & Configuration

My primary goal was to build a robust backend capable of ingesting PDF/DOCX documents, vectorizing them, and serving a chat interface using a local LLM (Ollama).

### Key Components Configured:
*   **Spring Boot**: Core framework.
*   **LangChain4j**: For orchestrating the AI components.
*   **PgVector (PostgreSQL)**: For storing vector embeddings.
*   **Apache Tika**: For document parsing.
*   **Ollama**: For the local Chat Language Model (Llama3).

## 2. Resolving Bean Conflicts

**The Issue:**
Initially, I encountered conflicts with the `EmbeddingModel` beans. LangChain4j attempts to auto-configure these, but I needed specific control over the model instantiation for my `ChatAgent`.

**My Solution:**
1.  I modified `ChatConfiguration.java` to explicitly define the `EmbeddingModel` bean using `AllMiniLmL6V2EmbeddingModel`.
2.  I updated `application.properties` to disable the default auto-configuration to prevent duplicate bean errors:
    ```properties
    langchain4j.embedding-model.all-minilm-l6-v2.enabled=false
    ```
3.  I ensured `DocumentService` received these dependencies via constructor injection, ensuring a clean wiring of components.

## 3. The PgVector Configuration Challenge

**The Issue:**
I attempted to manually define the `PgVectorEmbeddingStore` bean in `ChatConfiguration.java`. However, I ran into compilation errors because the builder method signatures didn't match the library version I was using (specifically around `datasource` configuration).

**My Solution:**
I decided to leverage the robustness of LangChain4j's Spring Boot starter auto-configuration instead of fighting the manual bean definition.
1.  I removed the manual `EmbeddingStore` bean from `ChatConfiguration.java`.
2.  I configured the store entirely through `application.properties`:
    ```properties
    langchain4j.pgvector.dimension=384
    langchain4j.pgvector.table=embeddings
    langchain4j.pgvector.create-table-if-missing=true
    ```
This simplified the code and let the library handle the connection pooling and initialization logic.

## 4. Database Connectivity & Environment Conflicts

**The Issue:**
This was the most critical hurdle. When running the application, I faced two distinct but related errors:
1.  `org.postgresql.util.PSQLException: FATAL: password authentication failed for user "postgres"`
2.  `org.postgresql.util.PSQLException: ERROR: could not open extension control file ... vector.control`

**My Diagnosis:**
I realized that my machine had a **local installation of PostgreSQL** running on port `5432` via Homebrew, which did *not* have the `pgvector` extension installed.
Even though I had a Docker container running, the application was connecting to the local Postgres instance because `localhost:5432` defaulted to the host service. This explained why the `vector` extension was missing and why authentication was failing (mismatched credentials between Docker config and local config).

**My Solution:**
I completely isolated the database environment to ensure the application used the Docker container with the correct extensions.

1.  **Port Remapping**: I modified `docker-compose.yaml` to expose the container on port **5433** instead of the default 5432.
    ```yaml
    ports:
      - "5433:5432"
    ```
2.  **Application Config Update**: I pointed the backend to this specific port in `application.properties`:
    ```properties
    spring.datasource.url=jdbc:postgresql://localhost:5433/docwhisperer
    ```
3.  **Volume Reset**: To ensure no stale credentials or data persisted from previous failed attempts, I performed a clean wipe of the Docker volume:
    ```bash
    docker compose down
    docker volume rm backend_postgres_data
    docker compose up -d
    ```

## 5. Final Status

After these interventions, I successfully started the backend. The logs confirmed:
*   Connection to Dockerized Postgres on port 5433.
*   Successful initialization of the `vector` extension.
*   Application started in ~6 seconds.

The backend is now ready to ingest documents and serve chat requests.

---

## Reference: Commands Used

Here is a quick reference of the commands I used throughout this process.

### Docker Management
Start the database container (detached mode):
```bash
docker compose up -d
```

Stop containers and remove network:
```bash
docker compose down
```

Check running containers:
```bash
docker ps
```

List Docker volumes:
```bash
docker volume ls
```

**Critical Fix Command (Reset Database):**
Wipes the existing volume to force re-initialization with correct credentials.
```bash
docker compose down && docker volume rm backend_postgres_data && docker compose up -d
```

### Application Execution
Run the Spring Boot application using the Maven wrapper:
```bash
./mvnw spring-boot:run
```

### Ollama (Local LLM)
Ensure these are running in a separate terminal for the chat functionality:

Start the Ollama server:
```bash
ollama serve
```

Pull the required model (if not already present):
```bash
ollama pull llama3
```
