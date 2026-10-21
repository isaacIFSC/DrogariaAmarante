# Mapa atual do projeto e lacunas do fluxo

Revisao estatica do codigo presente no repositorio em 01/10/2026. Este documento descreve o estado encontrado; nao significa que os fluxos foram executados em ambiente.

## Andamento da primeira etapa

Na primeira etapa do plano foram aplicadas estas correcoes: senha BCrypt para novas contas com migracao de senha legada no proximo login; senha removida do objeto guardado na sessao e rotacao do id de sessao ao autenticar; protecao CSRF ativa em formularios e chamadas de escrita; logout por POST; checkout restrito a cliente autenticado; alteracao/remocao de item limitada ao carrinho da sessao; consulta de pedido limitada ao dono ou administrador; codigo de pedido UUID; pedidos ficam `AGUARDANDO_PAGAMENTO` e nao baixam estoque sem pagamento confirmado; interpolacoes de dados do pedido no historico/confirmacao escapadas; removida a senha padrao do administrador da aplicacao. A credencial local do SQL Server foi mantida no Compose para nao interromper a conexao existente pelo Beekeeper.

Permanecem em aberto a integracao real de pagamento, a confirmacao segura por webhook, a reserva atomica de estoque/idempotencia e uma validacao completa dos campos do checkout. Por isso, ainda nao considerar apto para operacao comercial.

## Resumo

O repositorio ja tem uma loja web funcional em estrutura: catalogo, detalhes de produto, carrinho de visitante/cliente, cadastro e login por sessao, checkout que grava pedido, historico do cliente e uma area administrativa inicial. O checklist e a analise tecnica antigos descrevem uma versao anterior e devem ser lidos como historico, nao como retrato atual.

O fluxo ainda nao e comercialmente completo. A autenticacao manual complementa as regras permissivas do Spring Security; as operacoes de escrita agora usam CSRF, mas as rotas continuam dependentes de verificacoes proprias. Nao existe integracao de pagamento; pedidos ficam pendentes e o estoque nao e baixado ate que exista confirmacao real. O endpoint de rastreio e o historico exigem sessao de cliente dono do pedido ou administrador.

## Mapa da aplicacao

Tecnologia: Java 17, Spring Boot 4.0.3, Spring MVC, Thymeleaf, Spring Data JPA, Spring Security, H2 local e SQL Server via Docker. A interface e HTML/CSS/JavaScript sem framework de frontend.

| Area | Paginas e endpoints | Implementacao encontrada |
|---|---|---|
| Loja | `GET /`, `/api/catalogo/produtos`, `/api/catalogo/categorias`, `/api/catalogo/marcas` | Home com busca, filtros, ordenacao e paginacao; dados servidos pelo `ProdutoService`. |
| Produto | `GET /produto/{id}` ou `/produtos/{id}`, `GET /api/produtos/{id}` | Pagina de detalhe e API de produto. |
| Carrinho | `/carrinho`, `/api/carrinho` e endpoints adicionar/alterar/remover/limpar | Carrinho persistido por chave de sessao para visitante ou associado ao usuario autenticado; frete fixo de R$ 12,90 abaixo de R$ 150 e gratuito acima. |
| Conta | `/cadastro`, `/login`, `/logout`, `/perfil` | Cadastro de cliente, login por email ou CPF, sessao manual, fusao do carrinho visitante ao entrar e listagem de pedidos no perfil. Perfil e somente leitura. |
| Checkout/pedidos | `/pagamento`, `POST /api/checkout/finalizar`, `GET /api/checkout/meus-pedidos`, `GET /api/checkout/pedido/{codigo}` | Valida campos basicos e estoque, grava pedido e itens, baixa estoque e limpa o carrinho. Ha estados de pedido modelados, mas nao ha integracao de pagamento ou entrega. |
| Admin | `/admin/login`, `/admin/dashboard`, `/admin/produtos`, `/api/admin/produtos` | Login de admin pela mesma tabela de usuarios, dashboard com contagens e faturamento, cadastro de produto e importacao CSV. Nao ha edicao/exclusao de produto nem operacao de pedidos exposta na interface/API atual. |
| Dados e inicializacao | `domain`, `model`, `repository`, `config` | Entidades de produto, categoria, marca, usuario, carrinho, pedido e item; carga de catalogo de exemplo; inicializador de schema manual e Hibernate `ddl-auto=update`. |

### Rotas principais por papel

- Visitante: loja -> detalhe -> carrinho -> checkout -> pedido (hoje permitido sem conta).
- Cliente: cadastro/login -> carrinho associado -> checkout -> perfil/historico. O historico e consultado por endpoint que retorna lista vazia se nao houver sessao.
- Administrador: login -> dashboard -> cadastro/importacao de produtos. A protecao admin e feita por verificacoes manuais na sessao nos controllers, nao por regras centrais do Spring Security.

## Lacunas para um fluxo completo

### P0 — corrigir antes de expor na internet

- **Senha sem hash:** corrigido para novas contas; senhas legadas sao convertidas no login bem-sucedido. Ainda e necessario validar migracao de contas existentes.
- **Autorizacao insuficiente:** checkout, historico, rastreio e operacoes de carrinho agora fazem verificacoes de sessao/propriedade e CSRF esta ativo. `SecurityConfig` ainda permite as rotas no filtro central, logo a politica de acesso precisa ser consolidada e revisada.
- **Carrinho com acesso por ID:** corrigido; consulta de atualizacao/remocao inclui dono ou chave de sessao.
- **Pagamento simulado como confirmado:** corrigido para manter `AGUARDANDO_PAGAMENTO` e nao baixar estoque. A integracao e confirmacao via webhook continuam pendentes.
- **Exposicao de dados e XSS no cliente:** acesso ao pedido restringido e campos do historico/confirmacao escapados. Revisar restantes usos de `innerHTML` e dados de erro.
- **Pedido sem idempotencia e concorrencia de estoque:** codigo agora usa UUID, mas idempotencia e atualizacao/reserva atomica de estoque ainda faltam.

### P1 — fechar o fluxo operacional

- Permitir editar endereco/dados do cliente e salvar enderecos; hoje perfil apenas exibe cadastro e checkout pede endereco toda vez.
- Criar telas e endpoints administrativos para listar pedido, alterar status, cancelar e tratar reembolso/estorno de estoque. Existe metodo de servico de status, mas nao esta ligado a rota/admin.
- Completar CRUD de produtos e gestao de estoque; hoje ha criacao/importacao CSV, sem atualizar ou remover produto.
- Definir politica real de frete, desconto, CEP/area atendida e regras para produtos indisponiveis. Valores atuais sao fixos no carrinho.
- Validar dados no servidor (formato de email/CPF/CEP/UF, limites de quantidade/preco, campos obrigatorios) com DTOs e mensagens consistentes. Validacao de checkout atual e apenas parcial.
- Corrigir logout para POST com protecao CSRF; invalidar/recriar sessao no login e separar politicas de sessao cliente/admin.
- Substituir inicializacao ad hoc por migracoes versionadas; avaliar H2 em memoria como padrao contra SQL Server e mover a credencial local hoje definida no Compose para um gestor de segredos antes de publicar em producao.
- Revisar retorno dos dados: `ProdutoService` permite consultar produtos inativos no detalhe e catalogo sem filtro ativo por padrao; decidir regra de publicacao.

### P2 — completar escopo do produto

- Recuperacao de senha, verificacao de email, edicao de perfil e consentimento/retencao de dados LGPD.
- Integracao real de Pix/cartao, conciliacao de pagamento, notificacoes e entrega/rastreamento.
- Cupons, promocoes, fidelidade, avaliacoes, suporte e relatorios/exportacao.
- Observabilidade, auditoria de operacoes administrativas, backup/restauracao, CI/CD e requisitos de carga/disponibilidade.

## Sequencia recomendada

1. Corrigir autenticacao, autorizacao, CSRF, propriedade de carrinho e exposicao de dados.
2. Definir o contrato do pedido: visitante ou conta obrigatoria, enderecos, frete, estados e politica de cancelamento.
3. Manter pedido em `AGUARDANDO_PAGAMENTO`; implementar integracao de pagamento e transicoes confiaveis antes de marcar como pago/baixar estoque definitivamente.
4. Concluir operacao administrativa de pedidos, produtos e estoque.
5. Versionar schema e segredos; depois ampliar notificacoes, relatorios e requisitos de producao.

## Arquivos de referencia

- Controllers: `drugstoreAmarante/src/main/java/com/example/drugstoreAmarante/controller/`
- Regras de negocio: `drugstoreAmarante/src/main/java/com/example/drugstoreAmarante/service/`
- Entidades: `drugstoreAmarante/src/main/java/com/example/drugstoreAmarante/domain/` e `model/`
- Interface: `drugstoreAmarante/src/main/resources/templates/` e `static/`
- Configuracao e persistencia: `drugstoreAmarante/src/main/resources/application*.properties`, `config/` e `drugstoreAmarante/docker-compose.yml`
- Checklist e roadmap antigos: `docs/01-checklist-de-gaps.md` e `docs/02-roadmap-de-desenvolvimento.md`

## Limite desta revisao

Foi feita leitura estatica dos arquivos rastreados. Nao foram executados testes, build, aplicacao ou Docker; nao foram verificados fluxos visuais em navegador nem dados de ambiente real.
