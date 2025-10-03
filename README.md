# Insurance Policy Request

Plataforma de microsserviços para gerenciamento de apólices de seguro, desde a recpção, processamento e publicação de eventos relacionados a solicitações de 
apólices de seguros seguindo arquitetura de microsserviços com Spring Boot, Spring Data JPA, RabbitMQ e Docker.

# Arquitetura

## Visão Geral

A aplicação é composta por vários microsserviços que se comunicam entre si através de um broker de mensagens RabbitMQ. 

O fluxo contempla:
- Recepção de solicitações de apólices de seguro via API REST.
- Processamento das solicitações, incluindo validação e verificação de fraude
- Armazenamento dos dados em um banco de dados PostgreSQL com status da apólice em PENDING
- Notificação dos clientes no Broker Order Tópic sobre o status de suas solicitações como RECEIVED
- Recebe do Broker Order Tópic o status da apólice RECEIVED para análise de fraude (tonar a aplicação escálavel e independente)
- Faz a solicitação de análise de fraude para um serviço externo (mock fraud-api)
- Atualização do status da apólice para APPROVED ou REJECTED com base na análise de fraude
- Notificação dos clientes no Broker Order Tópic sobre o status final de análise de fraude (APPROVED ou REJECTED)
- Recebe do Broker Subscription Tópic o status da apólice APPROVED ou REJECTED
- Recebe do Broker Payment Tópic o status da apólice APPROVED ou REJECTED
- Atualiza o status da apólice de PENDING para APPROVED após confirmação de pagamento e subscrição
- Notificação dos clientes no Broker Order Tópic sobre o status final da apólice como APPROVED ou REJECTED
- Recepção de requisições de consulta de solicitação de apólices via API REST pelo id solicitação ou customerId
- Recepção de requisições de cancelamento das solicitações de apólices via API REST (permitida apenas para apólices que não estiverem no status APPROVED ou REJECTED)


Abaixo está a visão geral da arquitetura:

```plaintext
+-------------------+        +-------------------+        +-------------------+
|                   |        |                   |        |                   |
|  Insurance API    |        |  Fraud Service    |        |  Notification     |
|  (Spring Boot)    |        |  (Mock Service)   |        |  Service          |
|                   |        |                   |        |  (Spring Boot)    |
+-------------------+        +-------------------+        +-------------------+
        |                           |                           |
        |                           |                           |
        |                           |                           |
        v                           v                           v
+---------------------------------------------------------------+
|                                                               |
|                  RabbitMQ Message Broker                      |
|                                                               |
+---------------------------------------------------------------+
        |                           |                           |
        |                           |                           |                           
        v                           v                           v
+-------------------+        +-------------------+        +-------------------+
|                   |        |                   |        |                   |
|  Policy Service   |        |  Payment Service  |        |  Customer Service |
|  (Spring Boot)    |        |  (Spring Boot)    |        |  (Spring Boot)    |
|                   |        |                   |        |                   |
+-------------------+        +-------------------+        +-------------------+  
        |                           |                           |
        |                           |                           |
        v                           v                           v 
+---------------------------------------------------------------+
|                                                               |
|                     PostgreSQL Database                       |
|                                                               |
+---------------------------------------------------------------+
```
## Camadas
Camadas principais:
- Domain: Entidades e Regras de Negócio
- Application: Orquestração de casos de usos, coordenação entre ports e services
- Infrastructure: Implementações técnicas, persistência, comunicação externa

Vantagens:
- Separação clara de responsabilidades
- Facilita manutenção e evolução
- Independência de frameworks
- Facilidade de troca de tecnologias
- Testabilidade

## Dominios e Conceitos
- Policy: Representa a apólice de seguro, incluindo detalhes como tipo de seguro,
- Coverage: Representa as coberturas oferecidas pela apólice de seguro.
- Assistance: Representa os serviços de assistência associados à apólice de seguro.
- Status: Enumeração que define os possíveis status de uma apólice (PENDING, APPROVED, REJECTED, RECEIVED, CANCELLED).
- Event: Representa eventos do sistema, como mudanças de status de apólices.
- Repository: Interface para operações de persistência
- Service: Implementações de lógica de negócio
- UseCase: Casos de uso específicos da aplicação
- Controller: Exposição de APIs REST
- Messaging: Comunicação assíncrona via RabbitMQ

-----------------------

## Fluxo de Eventos Mensageria RabbitMQ

Acessar o RabbitMQ Management UI
- url: http://localhost:15672
- User: guest
- Password: guest

1. Um evento (subscription ou payment) é publicado no RabbitMQ com um routing key baseado no status da apólice (insurance.policy.requested, insurance.policy.approved, insurance.policy.rejected).
2. Um Listner escuta esses eventos e processa conforme o status.
   - Order Topic Listner escuta status RECEIVED
   - Subscription Topic Listner escuta status APPROVED ou REJECTED
   - Payment Topic Listner escuta status APPROVED ou REJECTED
3. O Service do tópico orquestra:
    - Para RECEIVED: Solicita análise de fraude e atualiza status RECEIVED no Order Topic e persiste na base o status PENDING
    - Para APPROVED: Verifica se já foi confirmado pagamento e subscrição, atualiza status para APPROVED
    - Para REJECTED: Atualiza status para REJECTED
4. Consumidores podem filtrar via binding key (insurance.policy.*). 
    Exchange (exemplo conceitual):
      - Exchange: insurance.policy.exchange
      - Routing keys: insurance.policy.requested, insurance.policy.approved, etc.

Postando Menssagem no Broker RabbitMQ

Exemplo de Payload para Subscription e payment

 ```json
{
   "policie_id": 1,
   "customer_id": "b6b62a04-a5fb-4409-ac52-208829c787f9",
   "product_id": 789,
   "category": "LIFE",
   "coverages": [
      {
         "roubo": 10000,
         "perda_total": 10000,
         "colisao_com_terceiros": 500
      }
   ],
   "assistances": [
      "24H_TOWING",
      "ROADSIDE_ASSISTANCE"
   ],
   "total_monthly_premium_amount": 250.75,
   "insured_amount": 150000,
   "payment_method": "DEBIT_CARD",
   "sales_channel": "TELEFONE",
   "statis": "APPROVED",
   "created_at": "2025-09-01T09:00:00",
   "finished_at": "2026-09-01T09:00:00"
}
```
-----------------------

## Banco de Dados
A aplicação usa H2 em memória para desenvolvimento.

Acesso ao console H2:
- Suba a aplicação e acesse o console do H2 em: http://localhost:8081/h2-console
- JDC URL: jdbc:h2:mem:insurance_db
- User: sa
- Password: (vazio)

### Consultas Uteis
```SQL
SELECT * FROM ASSISTANCES;
SELECT * FROM COVERAGES;
SELECT * FROM HISTORYS;
SELECT * FROM POLICIES;
```
A persistência é gerenciada via Spring Data JPA, facilitando operações CRUD e consultas personalizadas e é resetada a cada restart (Memória)

-----------------------
## Mock Fraud API (Wiremock)
A aplicação inclui um serviço simulado de análise de fraude usando Wiremock, que responde com diferentes classificações de fraude com base no UUID do cliente.

**Objetivo:** Simular respostas de uma API antifraude com diferentes classificações (ex.: regular, preferential, high-risk, no-information).

A collection abaixo tem todos os cenários para teste simulando os diferentes tipos de classificação de fraude.

Pasta do Wiremock contém:
- __files: Contém os arquivos de resposta JSON para diferentes classificações de fraude.
- mappings: Contém os arquivos de mapeamento que definem como o Wiremock deve responder a diferentes solicitações.

-----------------------

## Execução com Docker Compose

**Arquivo:** docker-compose.yml

Executar local sem o docker (Sem o Mock Fraud API)
```yaml
spring.docker.compose.enabled=false
```

Rebuid somente app após alterar código fonte:
```bash
docker compose build app
docker compose up -d app
```

Consultar os logs da Aplicação
```bash
docker compose logs -f app
```

Parar docker compose
 ```bash
 docker compose down
 ```

Parar forçado removendo Containers órfãos
 ```bash
docker compose down --remove-orphans
 ```

Listar Containers
 ```bash
 docker ps
 ```

--------------------------------------
# Testes

## [POST] Solicitação de Apólice

Exemplo de payload
```json
{
   "customer_id": "b6b62a04-a5fb-4409-ac52-208829c787f9",
   "product_id": 789,
   "category": "LIFE",
   "coverages": [
      {
         "roubo": 10000,
         "perda_total": 10000,
         "colisao_com_terceiros": 500
      }
   ],
   "assistances": [
      "24H_TOWING",
      "ROADSIDE_ASSISTANCE"
   ],
   "total_monthly_premium_amount": 250.75,
   "insured_amount": 150000,
   "payment_method": "DEBIT_CARD",
   "sales_channel": "TELEFONE",
   "created_at": "2025-09-01T09:00:00",
   "finished_at": "2026-09-01T09:00:00"
}
```










