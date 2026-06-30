# Manual de acesso a plataforma Drogaria Amarante

## Registro de revisoes

Data | Responsavel | Descricao
--- | --- | ---
2026-06-28 | Codex | Versao inicial do manual baseada no modelo do professor.

Este manual foi elaborado para apresentar a plataforma Drogaria Amarante na perspectiva do usuario, com foco no acesso, cadastro, visualizacao de produtos e uso da area administrativa.

## Sumario

1. Introducao
2. Pagina inicial da plataforma
   2.1. Barra superior e menu
   2.2. Listagem de produtos
3. Primeiro acesso
   3.1. Criando conta
   3.2. Fazendo login
   3.3. Acessando o perfil
4. Area administrativa
   4.1. Cadastro manual de produtos
   4.2. Importacao de produtos por CSV
5. Pagina de produto
6. Carrinho de compras
7. Observacoes importantes

## 1. Introducao

Este manual foi preparado para orientar o usuario no uso da plataforma Drogaria Amarante.
Aqui voce encontra as instrucoes para criar conta, entrar no sistema, consultar produtos,
visualizar o perfil e acessar a area administrativa de cadastro de produtos.

## 2. Pagina inicial da plataforma

A pagina inicial da Drogaria Amarante pode ser acessada pelo endereco raiz da aplicacao, ou seja, pelo caminho `/`.

Nela voce encontra o banner promocional, o campo de busca, os atalhos de acesso e a lista de produtos carregada automaticamente pela API.

Figura 1: Pagina inicial da Drogaria Amarante

### 2.1. Barra superior e menu

Na parte superior da pagina ha uma faixa promocional e um menu com atalhos rapidos.

Os itens mais importantes sao:

- **Entrar**: leva para a tela de login quando o usuario ainda nao esta autenticado.
- **Meu perfil**: aparece quando o usuario esta logado.
- **Cadastrar produto**: atalho para a area administrativa.
- **Sair**: encerra a sessao do usuario.
- **Carrinho**: abre a pagina do carrinho.

Figura 2: Menu principal da plataforma

### 2.2. Listagem de produtos

Os produtos exibidos na pagina inicial sao carregados automaticamente pela API `/api/produtos`.

Cada card apresenta:

- imagem do produto
- categoria
- marca
- nome
- descricao
- preco

Ao clicar em um card, o usuario acessa a pagina de detalhes do produto.

Figura 3: Cards de produtos na pagina inicial

## 3. Primeiro acesso

Para utilizar a plataforma pela primeira vez, o usuario deve criar uma conta e depois fazer login.

### 3.1. Criando conta

Para criar uma conta, acesse o endereco `/cadastro`.

Preencha os seguintes campos:

- nome
- sobrenome
- email
- senha
- CPF
- telefone
- genero
- data de nascimento

Depois de preencher o formulario, clique no botao **Cadastrar**.
Ao final do processo, o sistema redireciona para a tela de login.

Figura 4: Tela de cadastro de usuario

### 3.2. Fazendo login

Para entrar na plataforma, acesse `/login`.

No formulario de login:

- informe **Email ou CPF**
- informe a **Senha**
- clique em **Entrar**

Se os dados estiverem corretos, o sistema abre a pagina inicial ou a pagina solicitada anteriormente.

Observacao:

- o sistema contem um usuario padrao criado automaticamente para uso administrativo
- email: `admin@drugstoreamarante.com`
- senha: `123456`

Figura 5: Tela de login

### 3.3. Acessando o perfil

Depois de autenticado, o usuario pode visualizar seus dados em `/perfil`.

Nessa pagina sao exibidos:

- nome
- sobrenome
- email
- CPF
- telefone
- data de nascimento

Tambem ha atalhos para voltar a pagina inicial, sair da conta e acessar a area administrativa.

Figura 6: Pagina de perfil do usuario

## 4. Area administrativa

A area administrativa fica disponivel no endereco `/admin/produtos`.

Para acessar essa area, o usuario precisa estar logado.
Caso nao esteja, o sistema envia para a tela de login com retorno automatico para a pagina administrativa.

### 4.1. Cadastro manual de produtos

O formulario de cadastro manual permite incluir um produto por vez.

Os campos obrigatorios sao:

- nome
- marca
- categoria
- preco
- estoque
- descricao

O campo de imagem e opcional. Se ele nao for preenchido, o sistema utiliza uma imagem padrao.

Depois de preencher o formulario, clique em **Salvar produto**.
O sistema grava o produto e atualiza a lista de itens recentes.

Figura 7: Area administrativa com formulario de cadastro

### 4.2. Importacao de produtos por CSV

A mesma area administrativa permite importar varios produtos por arquivo CSV.

O sistema aceita dois formatos:

1. Cadastro padrao, com colunas como:
   - nome
   - descricao
   - preco
   - estoque
   - categoria
   - marca

2. Relatorio de estoque, com colunas como:
   - codigo
   - descricao
   - ncm
   - icms
   - unidade
   - qtd
   - vlrUnit
   - vlrTotal

Para importar:

1. selecione o arquivo CSV
2. clique em **Importar arquivo**
3. aguarde a mensagem de retorno na tela

Figura 8: Importacao de produtos por CSV

## 5. Pagina de produto

A pagina de produto pode ser acessada pelo caminho `/produto/{id}`.

Ela mostra os detalhes completos do item selecionado:

- imagem
- categoria
- nome
- marca
- descricao
- preco
- estoque
- status de disponibilidade

Tambem existe o botao **Adicionar ao carrinho**, que faz parte da interface visual da aplicacao.

Figura 9: Pagina de detalhes do produto

## 6. Carrinho de compras

A pagina `/carrinho` apresenta o carrinho de compras e o resumo do pedido.

Nesta tela o usuario ve:

- itens adicionados
- preco unitario
- quantidade
- total por item
- subtotal
- descontos
- taxa de entrega
- total final

Figura 10: Pagina do carrinho de compras

Observacao importante:

- no estado atual do projeto, o carrinho funciona como uma tela demonstrativa
- o fluxo completo de adicionar, remover e finalizar compra ainda nao esta integrado

## 7. Observacoes importantes

- A plataforma foi desenhada para uso em ambiente web.
- A busca e a listagem de produtos dependem da API de produtos.
- A area administrativa exige sessao logada.
- Se ocorrer erro no login, o sistema retorna para a tela de entrada com aviso de acesso invalido.
- Se nao houver produtos cadastrados, a pagina inicial exibe uma mensagem indicando que nao ha itens para mostrar.

---

Figura 11: Estrutura geral da plataforma

Este manual pode ser ajustado futuramente para incluir capturas de tela reais da aplicacao, seguindo o mesmo padrao de um documento academico.
