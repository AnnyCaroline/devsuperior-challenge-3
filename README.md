# CRUD de clientes

API REST do desafio de CRUD de clientes do curso Java Spring Professional. O projeto usa Java 21, Spring Boot, Spring Data JPA, Bean Validation e H2.

## Executar

Com JDK 21 e Maven instalados, rode os comandos na raiz do projeto.

```sh
mvn test
mvn spring-boot:run
```

A API fica em `http://localhost:8080`. O perfil `test` é o padrão e carrega dez clientes fictícios em um banco H2 em memória a cada inicialização. A aplicação também pode ser executada pela classe `ClientCrudApplication` em uma IDE.

## Rotas

| Método | Rota | Sucesso |
| --- | --- | --- |
| GET | `/clients?page=0&size=6&sort=name` | 200 |
| GET | `/clients/1` | 200 |
| POST | `/clients` | 201 e cabeçalho `Location` |
| PUT | `/clients/1` | 200 |
| DELETE | `/clients/1` | 204 |

A listagem usa páginas numeradas a partir de zero. O JSON traz a lista em `content` e os metadados (`number`, `size`, `totalElements` e `totalPages`) em `page`.

Use o corpo abaixo com `Content-Type: application/json` para POST e PUT.

```json
{
  "name": "Maria Silva",
  "cpf": "12345678901",
  "income": 6500.0,
  "birthDate": "1994-07-20",
  "children": 2
}
```

GET por ID, PUT e DELETE retornam 404 quando o cliente não existe. POST e PUT retornam 422 quando o nome está em branco ou a data de nascimento é futura. A resposta 422 lista cada campo inválido e sua mensagem em `errors`.

## Testes

`mvn test` roda dez testes de integração com MockMvc e H2. A suíte verifica a paginação e a ordenação, os códigos HTTP, as mensagens por campo e o efeito das operações de escrita no banco.
