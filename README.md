# Fintrack API

REST API para controle financeiro pessoal. Permite gerenciar contas e transações financeiras com autenticação JWT, associando todos os recursos ao usuário autenticado e com atualização automática de saldo a cada movimentação registrada.

---

## Tecnologias

- **Java 17**
- **Spring Boot 3**
- **Spring Security + JWT (JJWT)**
- **PostgreSQL**
- **H2** (banco em memória para testes)
- **JUnit 5 + MockMvc** (testes de integração)

---

## Pré-requisitos

- Java 17+
- PostgreSQL rodando localmente na porta `5432`

---

## Como executar

**1. Clone o repositório**
```bash
git clone https://github.com/guimarobo/fintrack-api.git
cd fintrack-api
```

**2. Crie o banco de dados**
```sql
CREATE DATABASE fintrack;
```

**3. Configure as credenciais**

Edite o arquivo `src/main/resources/application.properties`:
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/fintrack
spring.datasource.username=seu_usuario
spring.datasource.password=sua_senha
```

**4. Configure o JWT secret**

Por padrão, a aplicação já possui um secret para desenvolvimento. Para produção, defina a variável de ambiente:
```bash
export JWT_SECRET=suaChaveBase64ComPeloMenos32Bytes
```

**5. Execute a aplicação**
```bash
mvn spring-boot:run
```

A API estará disponível em `http://localhost:8080`.

> As tabelas são criadas automaticamente pelo Hibernate na primeira execução.

---

## Autenticação

A API utiliza **Bearer Token (JWT)** para autenticação stateless.

### Fluxo

1. Usuário faz registro via `POST /auth/register`
2. Usuário faz login via `POST /auth/login` e recebe um token JWT
3. Nas próximas requisições, o token deve ser enviado no header:
   ```
   Authorization: Bearer {token}
   ```
4. O token é válido por **24 horas**

### Regras de segurança

- Rotas `/auth/**` são **públicas** (registro e login)
- Todas as demais rotas exigem **token válido**
- Requisições sem token ou com token inválido retornam `401 Unauthorized`
- Senhas são armazenadas com **BCrypt**
- O campo `password` nunca é exposto nas respostas JSON

---

## Endpoints

### Autenticação `/auth` (rotas públicas)
| Método | Rota | Descrição |
|--------|------|-----------|
| POST | `/auth/register` | Registra um novo usuário |
| POST | `/auth/login` | Autentica e retorna o token JWT |

**Registro - Body:**
```json
{
  "name": "João",
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

### Usuário `/users` (requer autenticação)
| Método | Rota | Descrição |
|--------|------|-----------|
| GET | `/users/me` | Retorna dados do usuário autenticado |
| PUT | `/users/me` | Atualiza dados do usuário autenticado |
| PATCH | `/users/me` | Atualiza campos específicos (name, email, password) |
| DELETE | `/users/me` | Remove a conta do usuário autenticado |

> O usuário só tem acesso aos seus próprios dados. Não é possível visualizar ou modificar outros usuários.

---

### Contas `/accounts` (requer autenticação)
| Método | Rota | Descrição |
|--------|------|-----------|
| GET | `/accounts` | Lista as contas do usuário autenticado |
| GET | `/accounts/{id}` | Busca conta por ID (somente do próprio usuário) |
| POST | `/accounts` | Cria uma conta vinculada ao usuário autenticado |
| PUT | `/accounts/{id}` | Atualiza uma conta |
| PATCH | `/accounts/{id}` | Atualiza campos específicos |
| DELETE | `/accounts/{id}` | Remove uma conta |

---

### Transações `/transactions` (requer autenticação)
| Método | Rota | Descrição |
|--------|------|-----------|
| GET | `/transactions` | Lista as transações do usuário autenticado |
| GET | `/transactions/{id}` | Busca transação por ID (somente do próprio usuário) |
| POST | `/transactions` | Registra uma transação |
| PUT | `/transactions/{id}` | Atualiza uma transação |
| PATCH | `/transactions/{id}` | Atualiza campos específicos |
| DELETE | `/transactions/{id}` | Remove uma transação |

> O saldo da conta é atualizado automaticamente ao criar, atualizar ou remover uma transação. Tipos de transação: `ENTRADA` (soma ao saldo) e `DESPESA` (subtrai do saldo).

---

## Testes

A API possui testes de integração automatizados cobrindo os cenários de autenticação.

**Executar os testes:**
```bash
mvn test
```

### Cenários cobertos

| Teste | O que valida |
|-------|-------------|
| Registro com sucesso | Retorna 201 |
| Registro com e-mail duplicado | Retorna 409 |
| Registro sem campos obrigatórios | Retorna 400 |
| Login com sucesso | Retorna 200 + token JWT |
| Login com senha errada | Retorna 401 |
| Login com e-mail inexistente | Retorna 401 |
| Acesso a rota protegida sem token | Retorna 401 |
| Acesso a rota protegida com token inválido | Retorna 401 |
| Acesso a rota protegida com token válido | Retorna 200 |
| Senha oculta na resposta JSON | Campo password ausente |
| Alteração de senha via PATCH | Nova senha funciona, antiga não |

---
