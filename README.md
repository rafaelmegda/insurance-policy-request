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
- Atualiza o status da apólice de PENDING para APPROVED ou REJECTED após confirmação de pagamento e subscrição
  - Neste fluxo foi adotado o Aggregate Oriented Database para combinar múltiplas mensagens em uma única mensagem coerente.
  - [Mais informações no Artigo de Martin Fowler](https://martinfowler.com/bliki/AggregateOrientedDatabase.html)
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
      - Exchange: 
        - Order: insurance.order.ex
        - payment: insurance.payment.ex
        - subscription: insurance.subscription.ex
      - Queues:
        - Order: insurance.order.status.q
        - Payment: insurance.payment.q
        - Subscription: insurance.subscription.q
      - Routing keys: 
        - insurance.policy.received
        - insurance.policy.validated
        - insurance.policy.pending
        - insurance.policy.rejected
        - insurance.policy.approved
        - insurance.policy.canceled

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
# Como executar a aplicação com Docker Compose

**Observação:** Certifique-se de que o Docker e o Docker Compose estão instalados na sua máquina antes de executar os comandos acima.

1. Subir toda a infraestrutura (app, banco, mensageria, mocks)
```bash
docker compose up -d
```

2. Subir apenas a aplicação (após alterar código)
```bash
docker compose up -d app
```

3. Verificar status dos containers
 ```bash
 docker ps
 ```

4. Consultar os logs da Aplicação
```bash
docker compose logs -f app
```

5. Parar todos os containers
 ```bash
 docker compose down
 ```

6. Parar e remover containers órfãos
 ```bash
docker compose down --remove-orphans
 ```

7. Acessar o console do banco H2
    - URL: http://localhost:81
    - JDBC URL: jdbc:h2:tcp://h2:1521/~/insurance_db
    - Usuário: sa
    - Senha: (em branco)

8. Acessar o RabbitMQ Management UI
    - URL: http://localhost:15672
    - Usuário: guest
    - Senha: guest

9. Acessar o Wiremock Mock Fraud API
    - URL: http://localhost:8082/__admin/
    - Usuário: (não há autenticação)
    - Senha: (não há autenticação)


## Mensageria RabbitMQ para Subscrição e Pagamento
Para simular o fluxo completo, é necessário postar mensagens no RabbitMQ para os tópicos de Subscription e Payment.

Como fazer isso:
1. Acesse o RabbitMQ Management UI em http://localhost:15672 (usuário: guest, senha: guest).
2. Vá para a aba "Exchanges" e confirme se existe a exchange `insurance.policy.exchange`.
3. Na seção "Queues", verifique se as filas `insurance.subscription.q` e `insurance.payment.q` estão criadas
4. Selecione a "Queues" que deseja enviar a mensagem (insurance.subscription.q ou insurance.payment.q).
5. Na seção "Publish message", pegue os exemplos abaixo de APPROVED ou REJECTED.
4. No campo "Payload", insira o JSON da apólice que você deseja processar.
5. Clique em "Publish message" para enviar a mensagem.

Para visualizar as mensagens na fila:
1. Vá para a aba "Queues".
2. Clique na fila desejada (`insurance.subscription.q ou insurance.payment.q`).
3. Na seção "Get messages", defina o número de mensagens que deseja visualizar e clique em "Get Message(s)".

Para apagar as mensagens na fila:
1. Vá para a aba "Queues".
2. Clique na fila desejada (`insurance.subscription.q ou insurance.payment.q`).
3. Na seção "Purge", clique em "Purge" para remover todas as mensagens da fila.

Para visualizar as mensagens do tópico order (postado para os consumidores externos Payment e Subscription):
1. Vá para a aba "Queues".
2. Clique na fila `insurance.policy.status.q`.
3. Na seção "Get messages", defina o número de mensagens que deseja visualizar e clique em "Get Message(s)".

**Exemplo de Payload para Subscrição ou Pagamento como APPROVED:**

 ```json
{
   "policy_id": "PEGAR O VALOR DO BANCO PARA TESTES",
   "customer_id": "b6b62a04-a5fb-4409-ac52-208829c787f9",
   "product_id": 789,
   "category": "LIFE",
   "insured_amount": 150000.00,
   "status": "APPROVED",
   "timestamp": "2025-10-03T09:00:00"
}
```

**Exemplo de Payload para Subscrição ou Pagamento como REJECTED:**
 ```json
{
   "policy_id": "PEGAR O VALOR DO BANCO PARA TESTES",
   "customer_id": "b6b62a04-a5fb-4409-ac52-208829c787f9",
   "product_id": 789,
   "category": "LIFE",
   "insured_amount": 150000.00,
   "status": "REJECTED",
   "timestamp": "2025-10-03T09:00:00"
}
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










