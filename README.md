# Fintrack API

REST API para controle financeiro. Permite gerenciar contas e transacoes financeiras com autenticacao JWT, paginacao, workers assincronos, documentacao interativa (Swagger) e atualizacao automatica de saldo a cada movimentacao registrada.

---

## Tecnologias

- **Java 17**
- **Spring Boot 3**
- **Spring Security + JWT (JJWT)**
- **PostgreSQL**
- **H2** (banco em memoria para testes)
- **SpringDoc OpenAPI** (Swagger UI)
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

**3. Configure as variaveis de ambiente**

Crie um arquivo `.env` na raiz do projeto com as credenciais do seu banco e uma chave para o JWT:

```env
DB_URL=jdbc:postgresql://localhost:5432/fintrack
DB_USERNAME=postgres
DB_PASSWORD=sua_senha_do_postgres
JWT_SECRET=bWluaGFDaGF2ZVNlY3JldGFTdXBlclNlZ3VyYTEyMzQ=
JWT_EXPIRATION=86400000
CORS_ORIGINS=http://localhost:5173
```

| Variavel | Descricao |
|----------|-----------|
| `DB_URL` | URL de conexao com o PostgreSQL |
| `DB_USERNAME` | Usuario do banco |
| `DB_PASSWORD` | Senha do banco |
| `JWT_SECRET` | Chave Base64 para assinar tokens JWT |
| `JWT_EXPIRATION` | Validade do token em milissegundos (86400000 = 24h) |
| `CORS_ORIGINS` | Origens permitidas para requisicoes do frontend (separar por virgula para multiplas) |

**4. Execute a aplicacao**

Pelo terminal:
```bash
export $(cat .env | xargs) && ./mvnw spring-boot:run
```

Pelo IntelliJ: va em **Run** > **Edit Configurations** > no campo **Environment variables**, adicione as mesmas variaveis do `.env`.

A API estara disponivel em `http://localhost:8080`.

> As tabelas sao criadas automaticamente pelo Hibernate na primeira execucao.

---

## Documentacao Interativa (Swagger)

Com a aplicacao rodando, acesse:

```
http://localhost:8080/swagger-ui.html
```

O Swagger UI permite visualizar todos os endpoints, seus parametros, exemplos de request/response e testar requisicoes direto pelo navegador.

**Para testar endpoints protegidos no Swagger:**

1. Execute `POST /auth/login` com suas credenciais
2. Copie o `token` da resposta
3. Clique no botao **Authorize** no topo da pagina
4. Cole o token (sem o prefixo "Bearer") e confirme
5. Agora todos os endpoints protegidos podem ser testados

A especificacao OpenAPI tambem esta disponivel em `http://localhost:8080/v3/api-docs`.

---

## Autenticacao

A API utiliza **Bearer Token (JWT)** para autenticacao stateless.

### Fluxo

1. Usuario faz registro via `POST /auth/register`
2. Usuario faz login via `POST /auth/login` e recebe um token JWT
3. Nas proximas requisicoes, o token deve ser enviado no header:
   ```
   Authorization: Bearer {token}
   ```
4. O token e valido por **24 horas**

### Regras de seguranca

- Rotas `/auth/**` sao **publicas** (registro e login)
- Rotas `/swagger-ui/**` e `/v3/api-docs/**` sao **publicas** (documentacao)
- Todas as demais rotas exigem **token valido**
- Requisicoes sem token ou com token invalido retornam `401 Unauthorized`
- Senhas sao armazenadas com **BCrypt**
- O campo `password` nunca e exposto nas respostas JSON

---

## CORS

A API possui CORS configurado para permitir requisicoes do frontend.

- **Padrao (desenvolvimento):** `http://localhost:5173`
- **Producao:** defina a variavel `CORS_ORIGINS` com o dominio do frontend
- **Multiplas origens:** separe por virgula (`https://app.exemplo.com,https://www.exemplo.com`)

Metodos permitidos: `GET`, `POST`, `PUT`, `PATCH`, `DELETE`, `OPTIONS`
Headers permitidos: `Authorization`, `Content-Type`

---

## Endpoints

### Autenticacao `/auth` (rotas publicas)
| Metodo | Rota | Descricao |
|--------|------|-----------|
| POST | `/auth/register` | Registra um novo usuario |
| POST | `/auth/login` | Autentica e retorna o token JWT |

**Registro - Body:**
```json
{
  "name": "Joao",
  "email": "joao@email.com",
  "password": "senha123"
}
```

**Registro - Resposta (201):**
```json
{
  "message": "Usuario registrado com sucesso."
}
```

**Login - Body:**
```json
{
  "email": "joao@email.com",
  "password": "senha123"
}
```

**Login - Resposta (200):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9..."
}
```

---

### Usuario `/users` (requer autenticacao)
| Metodo | Rota | Descricao |
|--------|------|-----------|
| GET | `/users/me` | Retorna dados do usuario autenticado |
| PUT | `/users/me` | Atualiza dados do usuario autenticado |
| PATCH | `/users/me` | Atualiza campos especificos (name, email, password) |
| DELETE | `/users/me` | Remove a conta do usuario autenticado |

> O usuario so tem acesso aos seus proprios dados. Nao e possivel visualizar ou modificar outros usuarios.

---

### Contas `/accounts` (requer autenticacao)
| Metodo | Rota | Descricao |
|--------|------|-----------|
| POST | `/accounts` | Cria uma conta vinculada ao usuario autenticado |
| GET | `/accounts` | Lista as contas do usuario (paginado) |
| GET | `/accounts/{id}` | Busca conta por ID (somente do proprio usuario) |
| PUT | `/accounts/{id}` | Atualiza uma conta |
| PATCH | `/accounts/{id}` | Atualiza campos especificos |
| DELETE | `/accounts/{id}` | Remove uma conta |

---

### Transacoes `/transactions` (requer autenticacao)
| Metodo | Rota | Descricao |
|--------|------|-----------|
| POST | `/transactions` | Registra uma transacao |
| GET | `/transactions` | Lista as transacoes do usuario (paginado) |
| GET | `/transactions/{id}` | Busca transacao por ID (somente do proprio usuario) |
| PUT | `/transactions/{id}` | Atualiza uma transacao |
| PATCH | `/transactions/{id}` | Atualiza campos especificos |
| DELETE | `/transactions/{id}` | Remove uma transacao |
| GET | `/transactions/report` | Relatorio mensal (query params: `mes`, `ano`) |

> O saldo da conta e atualizado automaticamente ao criar, atualizar ou remover uma transacao. Tipos de transacao: `ENTRADA` (soma ao saldo) e `DESPESA` (subtrai do saldo).

---

## Paginacao

Os endpoints de listagem (`GET /accounts` e `GET /transactions`) retornam dados paginados.

### Parametros de query

| Parametro | Padrao | Descricao |
|-----------|--------|-----------|
| `page` | `0` | Numero da pagina (comeca em 0) |
| `size` | `10` | Quantidade de itens por pagina |
| `sort` | `bankName` / `date` | Campo para ordenacao |
| `direction` | `asc` / `desc` | Direcao da ordenacao |

### Exemplo de requisicao

```
GET /transactions?page=0&size=20&sort=date&direction=desc
```

### Exemplo de resposta

```json
{
  "content": [
    {
      "id": 1,
      "description": "Supermercado",
      "amount": 150.00,
      "type": "DESPESA",
      "date": "2026-04-01",
      "category": "ALIMENTACAO",
      "accountId": 1,
      "accountBankName": "Nubank"
    }
  ],
  "totalElements": 50,
  "totalPages": 3,
  "size": 20,
  "number": 0,
  "first": true,
  "last": false,
  "numberOfElements": 20,
  "empty": false
}
```

| Campo | Descricao |
|-------|-----------|
| `content` | Lista de itens da pagina atual |
| `totalElements` | Total de registros no banco |
| `totalPages` | Total de paginas disponiveis |
| `size` | Itens por pagina |
| `number` | Pagina atual (comeca em 0) |
| `first` / `last` | Indica se e a primeira ou ultima pagina |
| `empty` | Indica se a pagina esta vazia |

---

## Formato de Erros

Todas as respostas de erro seguem o mesmo formato JSON:

```json
{
  "message": "Descricao do erro",
  "status": 400,
  "timestamp": "2026-04-03T15:30:45.123456"
}
```

| Status | Descricao |
|--------|-----------|
| `400` | Requisicao invalida (validacao, formato, parametros) |
| `401` | Nao autenticado (token ausente, invalido ou expirado) |
| `404` | Recurso nao encontrado |
| `405` | Metodo HTTP nao suportado |
| `409` | Conflito (e-mail duplicado, violacao de integridade) |
| `500` | Erro interno do servidor |

---

## Workers Assincronos

A aplicacao utiliza workers assincronos com thread pools dedicados para processar tarefas em segundo plano sem bloquear a requisicao principal.

| Worker | Pool | Descricao |
|--------|------|-----------|
| `AuditWorker` | `auditPool` | Registra logs de auditoria para operacoes de usuario (CREATE, UPDATE, PATCH, DELETE) |
| `TransactionCategorizationWorker` | `categorizationPool` | Categoriza transacoes automaticamente com base na descricao (ALIMENTACAO, TRANSPORTE, MORADIA, LAZER, OUTROS) |
| `LowBalanceAlertWorker` | `fintrackAsyncPool` | Emite alerta no log quando o saldo de uma conta fica abaixo de R$100 |

---

## Testes

A API possui testes de integracao automatizados cobrindo os cenarios de autenticacao.

**Executar os testes:**
```bash
./mvnw test
```

### Cenarios cobertos

| Teste | O que valida |
|-------|--------------|
| Registro com sucesso | Retorna 201 + JSON com message |
| Registro com e-mail duplicado | Retorna 409 |
| Registro sem campos obrigatorios | Retorna 400 |
| Login com sucesso | Retorna 200 + token JWT |
| Login com senha errada | Retorna 401 |
| Login com e-mail inexistente | Retorna 401 |
| Acesso a rota protegida sem token | Retorna 401 |
| Acesso a rota protegida com token invalido | Retorna 401 |
| Acesso a rota protegida com token valido | Retorna 200 |
| Senha oculta na resposta JSON | Campo password ausente |
| Alteracao de senha via PATCH | Nova senha funciona, antiga nao |

---

## Estrutura do Projeto

```
src/main/java/com/guimarobo/Fintrack/
├── config/          # Configuracoes (SecurityConfig, AsyncConfig, OpenApiConfig)
├── controller/      # Endpoints REST
├── dto/             # Objetos de request e response
├── exception/       # Tratamento global de erros
├── model/           # Entidades JPA
├── repository/      # Interfaces de acesso ao banco
├── security/        # JWT (JwtProvider, JwtAuthenticationFilter)
├── service/         # Regras de negocio (interface + implementacao)
└── worker/          # Workers assincronos
```
