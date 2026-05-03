# Checklist de lacunas do projeto

Baseado no `DocumentoUnificado.docx` e no estado atual do repositório.

## 1. Catalogo e navegação
- [ ] Busca de produtos por nome, categoria e marca.
- [ ] Filtros por faixa de preco, disponibilidade e ordenacao.
- [ ] Pagina de detalhes do produto.
- [ ] Categorias organizadas no menu e na listagem.
- [ ] Imagens reais e nao apenas placeholders.

## 2. Carrinho e checkout
- [ ] Adicionar item ao carrinho.
- [ ] Remover item do carrinho.
- [ ] Alterar quantidade no carrinho.
- [ ] Limpar carrinho.
- [ ] Calcular subtotal, frete, desconto e total.
- [ ] Fluxo de finalizacao de compra.
- [ ] Validacao de estoque antes de concluir a compra.

## 3. Pagamentos
- [ ] Integracao com cartao de credito.
- [ ] Integracao com debito.
- [ ] Integracao com Pix.
- [ ] Suporte a boleto, se for mantido no escopo.
- [ ] Parcelamento.
- [ ] Confirmacao automatica de pagamento.

## 4. Usuarios e autenticacao
- [ ] Cadastro de cliente.
- [ ] Login e logout.
- [ ] Hash de senha com algoritmo seguro.
- [ ] Recuperacao de senha.
- [ ] Perfil e edicao de dados do cliente.
- [ ] Controle de acesso por perfil.

## 5. Pedidos e entrega
- [ ] Entidade de pedido.
- [ ] Itens do pedido.
- [ ] Status do pedido.
- [ ] Historico de pedidos do cliente.
- [ ] Codigo de rastreio.
- [ ] Atualizacao de status de entrega.

## 6. Promocoes e fidelidade
- [ ] Cupons de desconto.
- [ ] Regras promocionais.
- [ ] Cashback ou pontos.
- [ ] Produtos em destaque.
- [ ] Campanhas sazonais.

## 7. Painel administrativo
- [ ] CRUD de produtos.
- [ ] CRUD de categorias e marcas.
- [ ] CRUD de clientes, se necessario.
- [ ] Gestao de pedidos.
- [ ] Gestao de estoque.
- [ ] Gestao de promocoes e cupons.

## 8. Relatorios e analise
- [ ] Relatorio de vendas.
- [ ] Produtos mais vendidos.
- [ ] Estoque baixo.
- [ ] Clientes ativos.
- [ ] Utilizacao de cupons.
- [ ] Exportacao para PDF ou Excel.

## 9. Avaliacoes e suporte
- [ ] Avaliacao de produtos.
- [ ] Comentarios moderados.
- [ ] Canal de suporte.
- [ ] Notificacoes para pos-venda.

## 10. Arquitetura e qualidade
- [ ] Camada de servicos.
- [ ] DTOs para entrada e saida.
- [ ] Validacao de dados.
- [ ] Tratamento centralizado de erros.
- [ ] Testes de controller, service e repository.
- [ ] Pipeline de CI/CD.
- [ ] Documentacao tecnica atualizada.

## 11. Seguranca e conformidade
- [ ] Autenticacao real.
- [ ] Autorizacao por perfil.
- [ ] Protecao CSRF quando houver formularios.
- [ ] Registro de auditoria.
- [ ] Adequacao a LGPD.
- [ ] Backup e recuperacao.

## Prioridade sugerida
1. Catalogo completo.
2. Carrinho completo.
3. Cadastro e login.
4. Pedido e checkout.
5. Painel administrativo.
6. Relatorios, promocoes e fidelidade.
7. Qualidade, seguranca e testes.
