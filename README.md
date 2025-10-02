# insurance-policy-request

## Steps for explain the creation

1. Create a new repository on GitHub.
2. Clone the repository to your local machine.
3. Create a springboot project using Spring Initializr (https://start.spring.io/) with the following dependencies:
   - Spring Web
   - Spring Data JPA
   - H2 Database
   - Lombok
4. Create Docker file
5. Implement a Spring application using DDD with hexagonal architecture
6. Define the domain model for insurance policy request
7. Create controllers, services, repositories, and entities


## Run with Docker

```bash
docker compose up
```

Alterou código Java → precisa rodar:

```bash
docker compose build app
docker compose up -d app
```

Executar a APP
```bash
docker compose up -d app
```

Consultar logs APP
```bash
docker compose logs -f app
```

Listar Containers
 ```bash
 docker ps
 ```

Parar Containers
 ```bash
 docker compose down
 ```