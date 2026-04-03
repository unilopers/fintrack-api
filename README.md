# Fintrack API

REST API para controle financeiro. Permite gerenciar contas e transacoes financeiras com autenticacao JWT, workers assincronos e atualizacao automatica de saldo a cada movimentação registrada.

---

## Tecnologias

- **Java 17**
- **Spring Boot 3**
- **Spring Security + JWT (JJWT)**
- **PostgreSQL**
- **H2** (banco em memoria para testes)
- **JUnit 5 + MockMvc** (testes de integracao)

---

## Pre-requisitos

- Java 17+
- PostgreSQL rodando localmente na porta `5432`

---

## Como executar

**1. Clone o repositorio**
```bash
git clone https://github.com/unilopers/fintrack-api.git
cd fintrack-api
```

**2. Crie o banco de dados**
```sql
CREATE DATABASE fintrack;
```

**3. Configure as variáveis de ambiente**

Crie um arquivo `.env` na raiz do projeto com as credenciais do seu banco e uma chave para o JWT:

```env
DB_URL=jdbc:postgresql://localhost:5432/fintrack
DB_USERNAME=postgres
DB_PASSWORD=sua_senha_do_postgres
JWT_SECRET=bWluaGFDaGF2ZVNlY3JldGFTdXBlclNlZ3VyYTEyMzQ=
JWT_EXPIRATION=86400000
```

> **O que é JWT_SECRET?** É a chave usada para assinar os tokens de autenticação. Pode usar o valor de exemplo acima para desenvolvimento local. O `JWT_EXPIRATION` é o tempo de validade do token em milissegundos (86400000 = 24 horas).

> **Importante:** O arquivo `.env` já está no `.gitignore`, então suas credenciais não serão enviadas para o GitHub.

**4. Execute a aplicação**

Pelo terminal:
```bash
export $(cat .env | xargs) && ./mvnw spring-boot:run
```

Pelo IntelliJ: vá em **Run** > **Edit Configurations** > no campo **Environment variables**, adicione as mesmas variáveis do `.env`.

A API estara disponivel em `http://localhost:8080`.

> As tabelas sao criadas automaticamente pelo Hibernate na primeira execucao.

---

## Autenticação

A API utiliza **Bearer Token (JWT)** para autenticação stateless.

### Fluxo

1. Usuario faz registro via `POST /auth/register`
2. Usuario faz login via `POST /auth/login` e recebe um token JWT
3. Nas proximas requisições, o token deve ser enviado no header:
   ```
   Authorization: Bearer {token}
   ```
4. O token é válido por **24 horas**

### Regras de segurança

- Rotas `/auth/**` são **públicas** (registro e login)
- Todas as demais rotas exigem **token valido**
- Requisições sem token ou com token inválido retornam `401 Unauthorized`
- Senhas são armazenadas com **BCrypt**
- O campo `password` nunca é exposto nas respostas JSON

---

## Endpoints

### Autenticação `/auth` (rotas públicas)
| Método | Rota | Descrição                       |
|--------|------|---------------------------------|
| POST   | `/auth/register` | Registra um novo usuário        |
| POST   | `/auth/login` | Autentica e retorna o token JWT |

**Registro - Body:**
```json
{
  "name": "Joao",
  "email": "joao@email.com",
  "password": "senha123"
}
```

**Login - Body:**
```json
{
  "email": "joao@email.com",
  "password": "senha123"
}
```

**Login - Resposta:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9..."
}
```

---

### Usuario `/users` (requer autenticação)
| Método | Rota | Descrição                                           |
|--------|------|-----------------------------------------------------|
| GET    | `/users/me` | Retorna dados do usuário autenticado                |
| PUT    | `/users/me` | Atualiza dados do usuário autenticado               |
| PATCH  | `/users/me` | Atualiza campos específicos (name, email, password) |
| DELETE | `/users/me` | Remove a conta do usuário autenticado               |

> O usuário só tem acesso aos seus próprios dados. Não é possível visualizar ou modificar outros usuarios.

---

### Contas `/accounts` (requer autenticação)
| Método | Rota | Descrição                                       |
|--------|------|-------------------------------------------------|
| POST   | `/accounts` | Cria uma conta vinculada ao usuário autenticado |
| GET    | `/accounts` | Lista as contas do usuário autenticado          |
| GET    | `/accounts/{id}` | Busca conta por ID (somente do proprio usuário) |
| PUT    | `/accounts/{id}` | Atualiza uma conta                              |
| PATCH  | `/accounts/{id}` | Atualiza campos específicos                     |
| DELETE | `/accounts/{id}` | Remove uma conta                                |

---

### Transações `/transactions` (requer autenticação)
| Método | Rota | Descrição                                           |
|--------|------|-----------------------------------------------------|
| POST   | `/transactions` | Registra uma transação                              |
| GET    | `/transactions` | Lista as transações do usuário autenticado          |
| GET    | `/transactions/{id}` | Busca transação por ID (somente do proprio usuário) |
| PUT    | `/transactions/{id}` | Atualiza uma transação                              |
| PATCH  | `/transactions/{id}` | Atualiza campos específicos                         |
| DELETE | `/transactions/{id}` | Remove uma transação                                |
| GET    | `/transactions/report` | Relatório mensal (query params: `mes`, `ano`)       |

> O saldo da conta é atualizado automaticamente ao criar, atualizar ou remover uma transação. Tipos de transação: `ENTRADA` (soma ao saldo) e `DESPESA` (subtrai do saldo).

---

## Workers Assíncronos

A aplicação utiliza workers assincronos com thread pools dedicados para processar tarefas em segundo plano sem bloquear a requisição principal.

| Worker | Pool | Descrição                                                                                                     |
|--------|------|---------------------------------------------------------------------------------------------------------------|
| `AuditWorker` | `auditPool` | Registra logs de auditoria para operações de usuário (CREATE, UPDATE, PATCH, DELETE)                          |
| `TransactionCategorizationWorker` | `categorizationPool` | Categoriza transações automaticamente com base na descrição (ALIMENTACAO, TRANSPORTE, MORADIA, LAZER, OUTROS) |
| `LowBalanceAlertWorker` | `fintrackAsyncPool` | Emite alerta no log quando o saldo de uma conta fica abaixo de R$100                                          |

---

## Testes

A API possui testes de integração automatizados cobrindo os cenários de autenticação.

**Executar os testes:**
```bash
./mvnw test
```

### Cenarios cobertos

| Teste                                      | O que valida                    |
|--------------------------------------------|---------------------------------|
| Registro com sucesso                       | Retorna 201                     |
| Registro com e-mail duplicado              | Retorna 409                     |
| Registro sem campos obrigatórios           | Retorna 400                     |
| Login com sucesso                          | Retorna 200 + token JWT         |
| Login com senha errada                     | Retorna 401                     |
| Login com e-mail inexistente               | Retorna 401                     |
| Acesso a rota protegida sem token          | Retorna 401                     |
| Acesso a rota protegida com token inválido | Retorna 401                     |
| Acesso a rota protegida com token válido   | Retorna 200                     |
| Senha oculta na resposta JSON              | Campo password ausente          |
| Alteração de senha via PATCH               | Nova senha funciona, antiga não |

---

## Estrutura do Projeto

```
src/main/java/com/guimarobo/Fintrack/
├── config/          # Configurações (SecurityConfig, AsyncConfig)
├── controller/      # Endpoints REST
├── dto/             # Objetos de request e response
├── exception/       # Tratamento global de erros
├── model/           # Entidades JPA
├── repository/      # Interfaces de acesso ao banco
├── security/        # JWT (JwtProvider, JwtAuthenticationFilter)
├── service/         # Regras de negócio (interface + implementação)
└── worker/          # Workers assíncronos
```
