package com.devsuperior.clientcrud;

import java.time.LocalDate;

import com.devsuperior.clientcrud.entities.Client;
import com.devsuperior.clientcrud.repositories.ClientRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ClientCrudIntegrationTest {

    private static final long MISSING_ID = Long.MAX_VALUE;
    private static final String CPF = "12345678901";
    private static final LocalDate BIRTH_DATE = LocalDate.of(1994, 7, 20);
    private static final String VALID_BODY = """
            {
                "name": "Maria Silva",
                "cpf": "%s",
                "income": 6500.0,
                "birthDate": "%s",
                "children": 2
            }
            """.formatted(CPF, BIRTH_DATE);

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ClientRepository repository;

    @Autowired
    private JdbcTemplate jdbc;

    @AfterEach
    void cleanUp() {
        jdbc.update("DELETE FROM tb_client WHERE cpf = ?", CPF);
    }

    @Test
    void findsExistingClient() throws Exception {
        mvc.perform(get("/clients/{id}", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Ana Ribeiro"))
                .andExpect(jsonPath("$.cpf").value("07325468001"))
                .andExpect(jsonPath("$.income").value(4800.0))
                .andExpect(jsonPath("$.birthDate").value("1991-05-14"))
                .andExpect(jsonPath("$.children").value(1));
    }

    @Test
    void returns404WhenFindingMissingClient() throws Exception {
        expectNotFound(mvc.perform(get("/clients/{id}", MISSING_ID)));
    }

    @Test
    void listsClientsByPageInNameOrder() throws Exception {
        mvc.perform(get("/clients").param("page", "0").param("size", "6").param("sort", "name"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page.number").value(0))
                .andExpect(jsonPath("$.page.size").value(6))
                .andExpect(jsonPath("$.page.totalElements").value(10))
                .andExpect(jsonPath("$.page.totalPages").value(2))
                .andExpect(jsonPath("$.content[*].name", contains("Ana Ribeiro", "Bruno Almeida",
                        "Camila Ferreira", "Daniel Costa", "Elisa Martins", "Felipe Rocha")));

        mvc.perform(get("/clients").param("page", "1").param("size", "6").param("sort", "name"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page.number").value(1))
                .andExpect(jsonPath("$.content[*].name", contains("Gabriela Souza", "Henrique Dias",
                        "Isabela Lopes", "João Pereira")));
    }

    @Test
    void insertsValidClient() throws Exception {
        var response = mvc.perform(post("/clients").contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.name").value("Maria Silva"))
                .andReturn().getResponse();

        long id = com.jayway.jsonpath.JsonPath.parse(response.getContentAsString()).read("$.id", Long.class);
        assertThat(response.getHeader("Location")).isEqualTo("http://localhost/clients/" + id);
        assertSavedClient(id);
    }

    @Test
    void rejectsInvalidClientOnInsert() throws Exception {
        long count = repository.count();

        expectValidationError(mvc.perform(post("/clients").contentType(MediaType.APPLICATION_JSON)
                .content(invalidBody())), "/clients");

        assertThat(repository.count()).isEqualTo(count);
    }

    @Test
    void updatesExistingClient() throws Exception {
        long id = createClient();

        mvc.perform(put("/clients/{id}", id).contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value((int) id))
                .andExpect(jsonPath("$.name").value("Maria Silva"));

        assertSavedClient(id);
    }

    @Test
    void returns404WhenUpdatingMissingClient() throws Exception {
        expectNotFound(mvc.perform(put("/clients/{id}", MISSING_ID)
                .contentType(MediaType.APPLICATION_JSON).content(VALID_BODY)));
    }

    @Test
    void rejectsInvalidUpdateWithoutChangingClient() throws Exception {
        long id = createClient();

        expectValidationError(mvc.perform(put("/clients/{id}", id).contentType(MediaType.APPLICATION_JSON)
                .content(invalidBody())), "/clients/" + id);

        Client client = repository.findById(id).orElseThrow();
        assertThat(client.getName()).isEqualTo("Lúcia Santos");
        assertThat(client.getBirthDate()).isEqualTo(LocalDate.of(1986, 2, 8));
    }

    @Test
    void deletesExistingClient() throws Exception {
        long id = createClient();

        mvc.perform(delete("/clients/{id}", id))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""))
                .andExpect(header().doesNotExist("Location"));

        assertThat(repository.findById(id)).isEmpty();
    }

    @Test
    void returns404WhenDeletingMissingClient() throws Exception {
        expectNotFound(mvc.perform(delete("/clients/{id}", MISSING_ID)));
    }

    private long createClient() {
        return repository.save(new Client("Lúcia Santos", CPF, 3900.0, LocalDate.of(1986, 2, 8), 0)).getId();
    }

    private void assertSavedClient(long id) {
        Client client = repository.findById(id).orElseThrow();
        assertThat(client.getName()).isEqualTo("Maria Silva");
        assertThat(client.getCpf()).isEqualTo(CPF);
        assertThat(client.getIncome()).isEqualTo(6500.0);
        assertThat(client.getBirthDate()).isEqualTo(BIRTH_DATE);
        assertThat(client.getChildren()).isEqualTo(2);
    }

    private String invalidBody() {
        return """
                {"name": "   ", "birthDate": "%s"}
                """.formatted(LocalDate.now().plusYears(1));
    }

    private void expectNotFound(ResultActions result) throws Exception {
        result.andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Cliente não encontrado para o ID " + MISSING_ID + "."))
                .andExpect(jsonPath("$.path").value("/clients/" + MISSING_ID));
    }

    private void expectValidationError(ResultActions result, String path) throws Exception {
        result.andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.status").value(422))
                .andExpect(jsonPath("$.path").value(path))
                .andExpect(jsonPath("$.errors[*].fieldName", containsInAnyOrder("name", "birthDate")))
                .andExpect(jsonPath("$.errors[?(@.fieldName == 'name')].message",
                        contains("Informe o nome do cliente.")))
                .andExpect(jsonPath("$.errors[?(@.fieldName == 'birthDate')].message",
                        contains("A data de nascimento não pode estar no futuro.")));
    }
}
