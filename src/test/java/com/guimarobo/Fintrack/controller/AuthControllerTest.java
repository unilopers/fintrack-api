package com.guimarobo.Fintrack.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.guimarobo.Fintrack.dto.LoginRequest;
import com.guimarobo.Fintrack.dto.RegisterRequest;
import com.guimarobo.Fintrack.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    // ==================== REGISTRO ====================

    @Test
    @DisplayName("Deve registrar usuário com sucesso e retornar 201")
    void deveRegistrarUsuarioComSucesso() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setName("João Teste");
        request.setEmail("joao@teste.com");
        request.setPassword("senha123");

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().string("Usuário registrado com sucesso."));
    }

    @Test
    @DisplayName("Deve retornar 409 ao registrar e-mail duplicado")
    void deveRetornar409_quandoEmailDuplicado() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setName("João Teste");
        request.setEmail("joao@teste.com");
        request.setPassword("senha123");

        // Primeiro registro
        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // Segundo registro com mesmo e-mail
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("Deve retornar 400 ao registrar sem campos obrigatórios")
    void deveRetornar400_quandoCamposObrigatoriosAusentes() throws Exception {
        RegisterRequest request = new RegisterRequest();
        // Todos os campos vazios

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    // ==================== LOGIN ====================

    @Test
    @DisplayName("Deve fazer login com sucesso e retornar token JWT")
    void deveLogarComSucesso() throws Exception {
        registrarUsuario("joao@teste.com", "senha123");

        LoginRequest login = new LoginRequest();
        login.setEmail("joao@teste.com");
        login.setPassword("senha123");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty());
    }

    @Test
    @DisplayName("Deve retornar 401 ao fazer login com senha errada")
    void deveRetornar401_quandoSenhaErrada() throws Exception {
        registrarUsuario("joao@teste.com", "senha123");

        LoginRequest login = new LoginRequest();
        login.setEmail("joao@teste.com");
        login.setPassword("senhaerrada");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Deve retornar 401 ao fazer login com e-mail inexistente")
    void deveRetornar401_quandoEmailInexistente() throws Exception {
        LoginRequest login = new LoginRequest();
        login.setEmail("naoexiste@teste.com");
        login.setPassword("senha123");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isUnauthorized());
    }

    // ==================== ROTAS PROTEGIDAS ====================

    @Test
    @DisplayName("Deve retornar 401 ao acessar rota protegida sem token")
    void deveRetornar401_quandoSemToken() throws Exception {
        mockMvc.perform(get("/users/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    @DisplayName("Deve retornar 401 ao acessar rota protegida com token inválido")
    void deveRetornar401_quandoTokenInvalido() throws Exception {
        mockMvc.perform(get("/users/me")
                        .header("Authorization", "Bearer token.invalido.aqui"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Deve acessar rota protegida com token válido e retornar 200")
    void deveRetornar200_quandoTokenValido() throws Exception {
        String token = registrarELogar("joao@teste.com", "senha123");

        mockMvc.perform(get("/users/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("joao@teste.com"))
                .andExpect(jsonPath("$.name").value("João Teste"));
    }

    @Test
    @DisplayName("Deve garantir que a senha NÃO aparece na resposta JSON")
    void deveOcultarSenhaNaResposta() throws Exception {
        String token = registrarELogar("joao@teste.com", "senha123");

        mockMvc.perform(get("/users/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    // ==================== ALTERAÇÃO DE SENHA ====================

    @Test
    @DisplayName("Deve alterar senha via PATCH e conseguir logar com a nova senha")
    void deveAlterarSenhaComSucesso() throws Exception {
        String token = registrarELogar("joao@teste.com", "senha123");

        // Altera a senha
        mockMvc.perform(patch("/users/me")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"password\": \"novaSenha456\"}"))
                .andExpect(status().isOk());

        // Login com nova senha
        LoginRequest loginNovo = new LoginRequest();
        loginNovo.setEmail("joao@teste.com");
        loginNovo.setPassword("novaSenha456");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginNovo)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty());

        // Login com senha antiga deve falhar
        LoginRequest loginAntigo = new LoginRequest();
        loginAntigo.setEmail("joao@teste.com");
        loginAntigo.setPassword("senha123");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginAntigo)))
                .andExpect(status().isUnauthorized());
    }

    // ==================== MÉTODOS AUXILIARES ====================

    private void registrarUsuario(String email, String senha) throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setName("João Teste");
        request.setEmail(email);
        request.setPassword(senha);

        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));
    }

    private String registrarELogar(String email, String senha) throws Exception {
        registrarUsuario(email, senha);

        LoginRequest login = new LoginRequest();
        login.setEmail(email);
        login.setPassword(senha);

        MvcResult result = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        return json.get("token").asText();
    }
}
