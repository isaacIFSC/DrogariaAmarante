const PLACEHOLDER_IMAGE = '/images/produto-placeholder.svg';

function resolveImageUrl(url) {
  if (!url) return PLACEHOLDER_IMAGE;
  if (/^https?:\/\//i.test(url) || url.startsWith('/') || url.startsWith('data:')) return url;
  return `/${url.replace(/^\.?\//, '')}`;
}

function showToast(message, type = 'success') {
  let container = document.getElementById('toast-container');
  if (!container) {
    container = document.createElement('div');
    container.id = 'toast-container';
    container.className = 'toast-container';
    document.body.appendChild(container);
  }

  const toast = document.createElement('div');
  toast.className = `toast toast--${type}`;
  const icon = document.createElement('span');
  icon.className = 'toast__icon';
  icon.textContent = type === 'success' ? '✓' : 'ℹ';
  const text = document.createElement('span');
  text.className = 'toast__message';
  text.textContent = message;
  toast.append(icon, text);

  container.appendChild(toast);
  setTimeout(() => toast.classList.add('toast--visible'), 10);
  setTimeout(() => {
    toast.classList.remove('toast--visible');
    setTimeout(() => toast.remove(), 300);
  }, 3500);
}

function updateHeaderCount(count) {
  document.querySelectorAll('.badge, #header-cart-count').forEach((el) => {
    el.textContent = count || 0;
  });
}

function renderResumo(data) {
  const subtotalEl = document.getElementById('resumo-subtotal');
  const freteEl = document.getElementById('resumo-frete');
  const descontoEl = document.getElementById('resumo-desconto');
  const totalEl = document.getElementById('resumo-total');
  const btnCheckout = document.getElementById('btn-prosseguir-checkout');

  if (subtotalEl) subtotalEl.textContent = data.subtotalFormatado;
  if (freteEl) freteEl.textContent = data.freteFormatado;
  if (descontoEl) descontoEl.textContent = data.descontoFormatado;
  if (totalEl) totalEl.textContent = data.totalFormatado;

  if (btnCheckout) {
    if (!data.itens || data.itens.length === 0) {
      btnCheckout.classList.add('is-disabled');
      btnCheckout.style.pointerEvents = 'none';
      btnCheckout.style.opacity = '0.5';
    } else {
      btnCheckout.classList.remove('is-disabled');
      btnCheckout.style.pointerEvents = 'auto';
      btnCheckout.style.opacity = '1';
    }
  }
}

function renderCartItems(data) {
  const cartList = document.getElementById('carrinho-itens-container');
  if (!cartList) return;

  updateHeaderCount(data.quantidadeTotalItens);
  renderResumo(data);

  if (!data.itens || data.itens.length === 0) {
    cartList.innerHTML = `
      <article class="cart-item cart-item--empty">
        <div class="cart-product">
          <div class="cart-thumb">🛒</div>
          <div>
            <h3>Seu carrinho está vazio</h3>
            <p class="cart-sub">Navegue pelas categorias e adicione produtos para comprar.</p>
          </div>
        </div>
        <div class="cart-price">-</div>
        <div class="cart-qty">-</div>
        <div class="cart-total">-</div>
      </article>
    `;
    return;
  }

  cartList.innerHTML = '';
  data.itens.forEach((item) => {
    const article = document.createElement('article');
    article.className = 'cart-item';
    article.innerHTML = `
      <div class="cart-product">
        <div class="cart-thumb">
          <img src="${resolveImageUrl(item.imagemUrl)}" alt="${item.nome}" />
        </div>
        <div>
          <h3><a href="/produto/${item.produtoId}">${item.nome}</a></h3>
          <p class="cart-sub">${item.descricao || 'Produto oficial Drogaria Amarante'}</p>
          <button type="button" class="cart-remove-btn" data-item-id="${item.id}">Remover</button>
        </div>
      </div>

      <div class="cart-price">${item.precoUnitarioFormatado}</div>

      <div class="cart-qty">
        <button type="button" class="qty-btn btn-minus" data-item-id="${item.id}" data-current="${item.quantidade}">-</button>
        <input type="number" min="1" max="${item.estoqueDisponivel}" value="${item.quantidade}" data-item-id="${item.id}" readonly />
        <button type="button" class="qty-btn btn-plus" data-item-id="${item.id}" data-current="${item.quantidade}" data-max="${item.estoqueDisponivel}">+</button>
      </div>

      <div class="cart-total">${item.totalFormatado}</div>
    `;

    cartList.appendChild(article);
  });

  // Event Listeners
  cartList.querySelectorAll('.btn-minus').forEach((btn) => {
    btn.addEventListener('click', () => {
      const id = btn.dataset.itemId;
      const current = Number.parseInt(btn.dataset.current, 10);
      alterarQuantidade(id, current - 1);
    });
  });

  cartList.querySelectorAll('.btn-plus').forEach((btn) => {
    btn.addEventListener('click', () => {
      const id = btn.dataset.itemId;
      const current = Number.parseInt(btn.dataset.current, 10);
      const max = Number.parseInt(btn.dataset.max, 10);
      if (max && current >= max) {
        showToast(`Limite máximo do estoque disponível (${max}).`, 'error');
        return;
      }
      alterarQuantidade(id, current + 1);
    });
  });

  cartList.querySelectorAll('.cart-remove-btn').forEach((btn) => {
    btn.addEventListener('click', () => {
      const id = btn.dataset.itemId;
      removerItem(id);
    });
  });
}

async function carregarCarrinho() {
  try {
    const res = await fetch('/api/carrinho', { headers: { Accept: 'application/json' } });
    if (!res.ok) throw new Error('Falha ao obter carrinho');
    const data = await res.json();
    renderCartItems(data);
  } catch (err) {
    console.error(err);
  }
}

async function alterarQuantidade(itemId, novaQuantidade) {
  try {
    const res = await fetch(`/api/carrinho/item/${itemId}`, {
      method: 'PUT',
      headers: csrfHeaders({ 'Content-Type': 'application/json', Accept: 'application/json' }),
      body: JSON.stringify({ quantidade: novaQuantidade }),
    });

    if (!res.ok) {
      const err = await res.json().catch(() => ({}));
      throw new Error(err.message || 'Falha ao atualizar quantidade');
    }

    const data = await res.json();
    renderCartItems(data);
  } catch (err) {
    showToast(err.message, 'error');
  }
}

async function removerItem(itemId) {
  try {
    const res = await fetch(`/api/carrinho/item/${itemId}`, {
      method: 'DELETE',
      headers: csrfHeaders({ Accept: 'application/json' }),
    });

    if (!res.ok) throw new Error('Falha ao remover item');
    const data = await res.json();
    renderCartItems(data);
    showToast('Item removido do carrinho.', 'info');
  } catch (err) {
    showToast(err.message, 'error');
  }
}

document.addEventListener('DOMContentLoaded', carregarCarrinho);
