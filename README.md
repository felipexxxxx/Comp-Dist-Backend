# HealthSys Distribuido Backend

Monorepo backend da entrega das semanas 1-2 e 3-4 do projeto HealthSys SaaS.

## Estrutura

- `services/api-gateway`: roteamento de entrada para os servicos internos
- `services/identity-service`: autenticacao JWT e gestao de usuarios
- `services/patient-service`: cadastro e atualizacao de pacientes
- `services/notification-service`: consumo basico de eventos RabbitMQ
- `infra/docker-compose.yml`: ambiente local com PostgreSQL, RabbitMQ e servicos
- `docs/`: documentacao tecnica e mapeamento das sprints

## Escopo Implementado

- Semana 1-2
  - estrutura do repositorio
  - modelagem inicial do dominio
  - ambiente Docker Compose
  - configuracao de PostgreSQL e RabbitMQ
- Semana 3-4
  - login com JWT
  - cadastro e listagem de usuarios
  - cadastro, consulta e atualizacao de pacientes
  - API Gateway com rotas centrais

## Fora de Escopo Nesta Entrega

- prontuario eletronico
- triagem medica
- analytics
- operacao offline
- QR code
- Terraform
- ELK Stack
- funcionalidades opcionais do documento

## Execucao

1. Suba a infraestrutura:

```powershell
docker compose -f .\infra\docker-compose.yml up --build
```

2. Credenciais padrao de desenvolvimento:

- admin: `admin@healthsys.local`
- senha: `Admin@123`

3. Endpoints principais via gateway:

- `POST /api/auth/login`
- `GET /api/auth/me`
- `POST /api/users`
- `GET /api/users`
- `POST /api/patients`
- `GET /api/patients`
- `PUT /api/patients/{id}`
