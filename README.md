# insurance-request — README

## Visão geral rápida

Microserviço de processamento de solicitações de apólice de seguros
Arquitetura: Hexagonal (ports & adapters) + Mensageria (RabbitMQ, topic exchange) + Persistência (H2 para dev; Postgres recomendado em prod).
Objetivo: receber solicitações, validar via anti-fraude, coordenar pagamento e subscrição e publicar o status final.

1. Recebe `POST /v1/policies` → persiste como `RECEIVED`.
2. Consulta API de fraudes (sync/asynс) → aplica regras (REGULAR, ALTO_RISCO, PREFERENCIAL, SEM_INFO) → seta `VALIDATED` ou `REJECTED`.
3. Publica evento com status (routingKey = status) no Exchange.
4. Dois serviços externos (Payment e Subscription) publicam eventos `APPROVED` / `REJECTED`.
5. O **aggregator** (listener) aguarda os dois eventos; quando ambos são `APPROVED` → marca `APPROVED` na tabela `policies`, caso contrário `REJECTED`.

---
## Como rodar localmente (com Docker Compose)

Requisitos: 
- Docker >= 20.x, Docker Compose v2
- Java 17, Maven 3.8+ (para build local)
- Opcional: rabbitmqadmin (CLI) para publicar mensagens manualmente

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

10. Acessar a API da aplicação
    - URL: http://localhost:8081

## Endpoints disponíveis

**Criar uma nova apólice (POST)**
```json
curl --request POST \
--url http://localhost:8081/v1/policies \
--header 'Content-Type: application/json' \
--data '{
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
"sales_channel": "TELEFONE"
}'
```
**Consultar apólice por policy_id ou customer_id (GET)**
```json
curl --request GET \
  --url 'http://localhost:8081/v1/policies?customer_id=b6b62a04-a5fb-4409-ac52-208829c787f9&policy_id=75b17569-6b06-417e-a329-054b6389424e'
``` 

**Cancelar apólice (PATCH)**
```json
curl --request PATCH \
  --url 'http://localhost:8081/v1/policies/75b17569-6b06-417e-a329-054b6389424e?status=CANCELLED'
```

## Executando uma solicitação de apólice

Fluxo completo para solicitação de apólice:

1. Fazer um `POST` para `http://localhost:8081/v1/policies` com o payload de solicitação de apólice:

```json
curl --request POST \
--url http://localhost:8081/v1/policies \
--header 'Content-Type: application/json' \
--data '{
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
"sales_channel": "TELEFONE"
}'
```

2. O sistema processará a solicitação persistindo a apólice com status `RECEIVED` gerando um `policy_id` (UUID).
3. O sistema publicará um evento no RabbitMQ com o status `RECEIVED`.
4. O sistema consome a mensagem através de um Listener, consulta a API de fraude (Wiremock) e aplica as regras de negócio.
5. Dependendo da resposta da API de fraude, o sistema atualizará o status da apólice para `VALIDATED` ou `REJECTED` e publicará o evento correspondente no RabbitMQ.
6. Api responde com o status `201 Created` e o payload com os dados de response incluindo o policy_id    
7. Após este fluxo o sistema ficará aguardando os eventos de `APPROVED` ou `REJECTED` dos serviços externos (Payment e Subscription) para finalizar o status da apólice.
8. Para testar o fluxo completo, envie mensagens de `APPROVED` ou `REJECTED` para os tópicos de Subscription e Payment no RabbitMQ
9. Acesse o console do RabbitMQ para postar mensagens e visualizar as filas (detalhes do acesso na seção Mensageria RabbitMQ abaixo).
10. Publique mensagens de `APPROVED` ou `REJECTED` para os tópicos nas filas `insurance.subscription.q` e `insurance.payment.q` com o `policy_id` gerado no passo 2.
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
   "policy_id": "PEGAR O VALOR RETORNADO NA API DE POST",
   "customer_id": "b6b62a04-a5fb-4409-ac52-208829c787f9",
   "product_id": 789,
   "category": "LIFE",
   "insured_amount": 150000.00,
   "status": "REJECTED",
   "timestamp": "2025-10-03T09:00:00"
}
```
11. O sistema processará os eventos recebidos aplicando a lógica do agregador para determinar o status final da apólice.
12. O status final da apólice será atualizado para `APPROVED` se ambos os eventos forem `APPROVED`, ou `REJECTED` se qualquer um dos eventos for `REJECTED`.
13. O sistema publicará o evento final no RabbitMQ com o status atualizado.
14. A qualquer momento é possível consultar o status através do GET `http://localhost:8080/v1/policies` passando o `policy_id` ou `client_id` como query param.
```json
curl --request GET \
  --url 'http://localhost:8081/v1/policies?customer_id=b6b62a04-a5fb-4409-ac52-208829c787f9&policy_id=75b17569-6b06-417e-a329-054b6389424e'
``` 
15. Também é possível cancelar a apólice através do endpoint PATCH `http://localhost:8081/v1/policies/{policy_id}` que será aceito apenas se o status atual não for `APPROVED` ou `REJECTED`.
```json
curl --request PATCH \
  --url 'http://localhost:8081/v1/policies/75b17569-6b06-417e-a329-054b6389424e?status=CANCELLED'
```

---------------------------------------------

# Arquitetura

## Decisões principais e motivos

- **Hexagonal:** separar domínio e infra favorece testes e troca de infra (ex.: trocar Rabbit por Kafka) — redução de acoplamento.
- **Topic Exchange (RabbitMQ):** permite routing por status com flexibilidade (consumidores filtram por binding keys). Alternativa direct seria menos flexível.
- **Aggregator (stateful) com JdbcMessageStore:** precisamos agregar dois eventos independentes (payment+subscription). Usar aggregator com store persistente garante tolerância a restart — alternativa Saga/Temporal seria overkill aqui.
- **WireMock para fraud API:** facilita testes e automação de cenários.
- **H2 no dev:** zero-config para contribuições; Postgres em prod recomendado (não usar H2 em produção).

### Trade-offs
-- **Eventual consistency:** decisões dependem de eventos externos; há janela de tempo (pending). Benefício: desacoplamento e escalabilidade; custo: complexidade operacional (retries, reconciliation).
-- **Simplicidade vs robustez:** prefiri implementar aggregator simples em vez de orquestrador (menor complexidade).

### Hexagonal (Ports and Adapters)
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

**Dominios e Conceitos**
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

### Fluxo de Eventos Mensageria RabbitMQ

O motivo pela opção do RabbitMQ foi baseada na estrutura de Exchange e Bind. Projetei o sistema para que os consumidores possam filtrar as mensagens que desejam receber via binding key 
definido pelo status, ou seja, um consumidor pode optar por receber apenas eventos de apólices aprovadas ou rejeitadas, dependendo do seu interesse.

**Fluxo de Eventos:**

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

**Como consultar e publicar mensagens no RabbitMQ usando UI:**
1. Acesse o RabbitMQ Management UI em http://localhost:15672 (usuário: guest, senha: guest).
2. Vá para a aba "Exchanges" e confirme se existe a exchange `insurance.order.ex`.
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


## Agregador (Aggregator) — estratégia sem DSL

A escolha pelo Aggregator se deu pela necessidade de combinar eventos de dois serviços externos (Payment e Subscription) para determinar o status final da apólice. 

Dessa forma, o Aggregator atua como um ponto central que aguarda a chegada de ambos os eventos e aplica a lógica de negócio necessária para decidir se a apólice deve ser aprovada ou rejeitada.

Essa solução é eficaz para garantir que todas as condições necessárias sejam atendidas antes de tomar uma decisão final sobre o status da apólice, proporcionando uma abordagem robusta e confiável para o processamento de eventos assíncronos.

Esta configuração utiliza o Spring Integration para implementar o padrão Aggregator, que é responsável por combinar mensagens de diferentes fontes (neste caso, eventos de pagamento e subscrição) em uma única mensagem agregada.

O Aggregator utiliza `JdbcMessageStore` para persistência das mensagens, garantindo que o estado das mensagens seja mantido mesmo em caso de falhas ou reinícios da aplicação.

As tabelas podem ser consultadas diretamente no banco H2 para monitoramento e depuração.

**Fluxo do Aggregator:**
* Listeners `PaymentListener` e `SubscriptionListener` publicam em `aggregatorInput` (canal).
* Use `AggregatingMessageHandler` com `JdbcMessageStore` (persistente) e `MessageGroupProcessor` custom para montar `AggregatorDomain`.
* Regras de liberação: liberar quando tiver payment e subscription ou se qualquer um for `REJECTED`.
* Tempo de espera atual para que chegue o mesmo evento para a mesma policy_id é 15 minutos (configurável).


## Database H2

A aplicação usa H2 para persistência de modo relacional, com tabelas para `policies`, `coverages`, `assistances` e `historys`.
A opção por H2 foi feita para facilitar o setup e testes locais, eliminando a necessidade de configurar um banco de dados externo.
A persistência é gerenciada via Spring Data JPA, com entidades mapeadas para as tabelas correspondentes. Dessa forma, é possível realizar operações CRUD e consultas complexas de maneira eficiente.

**Consultas Uteis**
```SQL
SELECT * FROM ASSISTANCES;
SELECT * FROM COVERAGES;
SELECT * FROM HISTORYS;
SELECT * FROM POLICIES;
```

**Consulta para ver o estado do Aggregator**
```SQL
SELECT * FROM INT_CHANNEL_MESSAGE;
SELECT * FROM INT_GROUP_TO_MESSAGE; 
SELECT * FROM INT_LOCK; 
SELECT * FROM INT_MESSAGE; 
SELECT * FROM INT_MESSAGE_GROUP ;
SELECT * FROM INT_METADATA_STORE; 
```

## Mock Fraud API (Wiremock)

A aplicação inclui um serviço simulado de análise de fraude usando Wiremock, que responde com diferentes classificações de fraude com base no policy_id.

Objetivo do wiremock é simular respostas de uma API antifraude com diferentes classificações (ex.: regular, preferential, high-risk, no-information).

Pasta do Wiremock contém:
- __files: Contém os arquivos de resposta JSON para diferentes classificações de fraude.
- mappings: Contém os arquivos de mapeamento que definem como o Wiremock deve responder a diferentes solicitações.

### Cenários de testes Fraudes

#### HIGH_RISK (ALTO_RISCO) — Deve ser REJECTED

```json
curl --request POST \
--url http://localhost:8081/v1/policies \
--header 'Content-Type: application/json' \
--data '{
"customer_id": "8e1c0c77-b7c4-4c4a-8c2d-d1b3c0c9f222",
"product_id": 789,
"category": "AUTO",
"coverages": [
{
"roubo": 10000,
"perdaTotal": 10000,
"colisaoComTerceiros": 500
}
],
"assistances": [
"24H_TOWING",
"ROADSIDE_ASSISTANCE"
],
"total_monthly_premium_amount": 250.75,
"insured_amount": 300000,
"payment_method": "DEBIT_CARD",
"sales_channel": "TELEFONE",
"created_at": "2025-09-01T09:00:00",
"finished_at": "2026-09-01T09:00:00"
}'
```

### HIGH_RISK (VALIDADO)
```json
curl --request POST \
  --url http://localhost:8081/v1/policies \
  --header 'Content-Type: application/json' \
  --data '{
    "customer_id": "8e1c0c77-b7c4-4c4a-8c2d-d1b3c0c9f222",
    "product_id": 789,
    "category": "AUTO",
    "coverages": [
        {
            "roubo": 10000,
            "perdaTotal": 10000,
            "colisaoComTerceiros": 500
        }
    ],
    "assistances": [
        "24H_TOWING",
        "ROADSIDE_ASSISTANCE"
    ],
    "total_monthly_premium_amount": 250.75,
    "insured_amount": 200000,
    "payment_method": "DEBIT_CARD",
    "sales_channel": "TELEFONE",
    "created_at": "2025-09-01T09:00:00",
    "finished_at": "2026-09-01T09:00:00"
}'
```

## Testes e cobertura

**Rodar testes unitários**
```bash
mvn test
```

**Gerar relatório de cobertura**
Foi gerado um script PowerShell para facilitar a geração do relatório de cobertura Jacoco.
```bash
Set-ExecutionPolicy -ExecutionPolicy RemoteSigned -Scope Process
.\generate-jacoco.ps1
 ```

disponível em: `start target\site\jacoco\index.html`








