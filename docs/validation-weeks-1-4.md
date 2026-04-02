# Validacao - Semanas 1 a 4

## Escopo Entregue

### Semana 1-2

- repositorio backend estruturado em monorepo de microsservicos
- modelagem inicial de usuarios e pacientes
- PostgreSQL e RabbitMQ definidos no `docker-compose`
- documentacao de arquitetura e execucao local

### Semana 3-4

- autenticacao JWT
- cadastro e listagem de usuarios
- cadastro, consulta e atualizacao de pacientes
- API Gateway
- frontend web em React + TypeScript + Tailwind CSS

## Requisitos Atendidos Nesta Entrega

- `RF01`: cadastro de pacientes
- `RF02`: consulta de pacientes cadastrados
- `RF03`: atualizacao de dados do paciente
- `RF17`: login de usuarios cadastrados
- `RF18`: logout stateless via descarte do token no cliente
- `RF20`: cadastro de usuarios com perfis de acesso

## Requisitos Ainda Fora do Escopo das Semanas 1-4

- `RF04` a `RF16`: prontuario e triagem
- `RF19`: notificacoes de triagem/prontuario completas

Observacao: a infraestrutura de notificacao assincrona via RabbitMQ ja foi preparada e validada com um `notification-service`, mas o caso completo de negocio depende dos servicos de prontuario e triagem das proximas entregas.

## Requisitos Nao Funcionais Cobertos

- `RNF01`: arquitetura distribuida baseada em microsservicos
- `RNF02`: backend em Spring Boot
- `RNF03`: frontend em React + TypeScript + Tailwind CSS
- `RNF04`: persistencia principal em PostgreSQL
- `RNF05`: containerizacao com Docker
- `RNF06`: autenticacao com JWT
- `RNF07`: comunicacao assincrona com RabbitMQ
- `RNF08`: interface web responsiva
- `RNF09`: restricao de acesso por perfil
- `RNF10`: codigos HTTP coerentes e tratamento padronizado de erro
- `RNF12`: execucao local preparada para Docker Compose
- `RNF13`: senhas armazenadas com BCrypt
- `RNF14`: organizacao modular do codigo

## Evidencias de Validacao

- backend: `mvn test -q` executado com sucesso
- frontend: `npm.cmd run typecheck` executado com sucesso
- frontend: `npm.cmd run build` executado com sucesso
- infraestrutura: `docker compose -f .\infra\docker-compose.yml config` executado com sucesso

## Limitacao de Ambiente

O smoke test completo com `docker compose up -d --build` nao foi concluido porque o Docker Engine da maquina nao estava ativo no momento da validacao (`dockerDesktopLinuxEngine` indisponivel).
