# HealthSys - Estrutura do Backend

Este backend reaproveita o principio estrutural do `TCC-back`: cada modulo interno segue arquitetura em camadas com `controller`, `service`, `repository`, `domain` e `dto`, alem de `config` e `shared` para codigo transversal.

## Monorepo de Microsservicos

```text
Comp-Dist-Backend/
|-- docs/
|-- infra/
|   `-- docker-compose.yml
|-- services/
|   |-- api-gateway/
|   |-- identity-service/
|   |-- patient-service/
|   `-- notification-service/
|-- pom.xml
|-- mvnw
`-- .mvn/
```

## Servicos

- `api-gateway`: centraliza rotas externas e expande a arquitetura distribuida desde a primeira entrega.
- `identity-service`: concentra autenticacao JWT e gestao de usuarios, coerente com o documento de requisitos que agrupa usuarios e autenticacao.
- `patient-service`: responde pelo cadastro e manutencao de pacientes.
- `notification-service`: valida a comunicacao assincrona obrigatoria via RabbitMQ consumindo eventos publicados pelos servicos.

## Padrao Interno dos Servicos de Negocio

```text
src/main/java/com/healthsys/{service}/
|-- config/
|-- shared/
|   `-- exception/
|-- auth/ or patient/ or user/
|   |-- controller/
|   |-- service/
|   |-- repository/
|   |-- domain/
|   `-- dto/
`-- {Service}Application.java
```

## Infraestrutura

- PostgreSQL: bancos separados por servico (`healthsys_identity` e `healthsys_patient`)
- RabbitMQ: exchange `healthsys.events` e fila `healthsys.notifications`
- Docker Compose: sobe banco, broker e servicos
- Spring Actuator: health check para todos os servicos
