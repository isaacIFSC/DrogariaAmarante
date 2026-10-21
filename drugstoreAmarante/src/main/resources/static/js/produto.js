const PLACEHOLDER_IMAGE = '/images/produto-placeholder.svg';

const moneyFormatter = new Intl.NumberFormat('pt-BR', {
  style: 'currency',
  currency: 'BRL',
});

let currentStock = 0;

function getProductId() {
  const datasetId = document.body?.dataset?.produtoId;
  if (datasetId && !Number.isNaN(Number.parseInt(datasetId, 10))) {
    return Number.parseInt(datasetId, 10);
  }

  // Fallback: extrair o ID numérico da URL atual (ex: /produto/1)
  const match = window.location.pathname.match(/\/produto(?:s)?\/(\d+)/);
  if (match && match[1]) {
    return Number.parseInt(match[1], 10);
  }

  return null;
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
  toast.innerHTML = `
    <span class="toast__icon">${type === 'success' ? '✓' : 'ℹ'}</span>
    <span class="toast__message">${message}</span>
  `;

  container.appendChild(toast);
  setTimeout(() => toast.classList.add('toast--visible'), 10);
  setTimeout(() => {
    toast.classList.remove('toast--visible');
    setTimeout(() => toast.remove(), 300);
  }, 3500);
}

async function updateHeaderCartCount() {
  try {
    const res = await fetch('/api/carrinho', { headers: { Accept: 'application/json' } });
    if (res.ok) {
      const data = await res.json();
      document.querySelectorAll('.badge, #header-cart-count').forEach((el) => {
        el.textContent = data.quantidadeTotalItens || 0;
      });
    }
  } catch (err) {
    console.error('Erro ao atualizar carrinho:', err);
  }
}

function escapeText(value) {
  return value == null ? '' : String(value);
}

function formatPrice(value) {
  const numeric = Number(value);
  return Number.isFinite(numeric) ? moneyFormatter.format(numeric) : escapeText(value);
}

function resolveImageUrl(url) {
  if (!url) return PLACEHOLDER_IMAGE;
  if (/^https?:\/\//i.test(url) || url.startsWith('/') || url.startsWith('data:')) return url;
  return `/${url.replace(/^\.?\//, '')}`;
}

function setStatus(message, type = 'info') {
  const statusEl = document.getElementById('produto-status');
  if (!statusEl) return;
  statusEl.textContent = message;
  statusEl.classList.toggle('is-error', type === 'error');
}

function updateContent(product) {
  currentStock = product.estoque || 0;
  const image = document.getElementById('produto-imagem');
  const breadcrumb = document.getElementById('produto-breadcrumb');
  const category = document.getElementById('produto-categoria');
  const name = document.getElementById('produto-nome');
  const brand = document.getElementById('produto-marca');
  const description = document.getElementById('produto-descricao');
  const price = document.getElementById('produto-preco');
  const stock = document.getElementById('produto-estoque');
  const available = document.getElementById('produto-disponivel');
  const addBtn = document.getElementById('btn-add-detalhes');

  if (image) {
    image.src = resolveImageUrl(product.imagemUrl);
    image.alt = product.nome ? `Imagem de ${product.nome}` : 'Imagem do produto';
  }
  if (breadcrumb) breadcrumb.textContent = product.nome || 'Produto';
  if (category) category.textContent = product.categoria || 'Geral';
  if (name) name.textContent = product.nome || 'Produto sem nome';
  if (brand) brand.textContent = product.marca ? `Marca: ${product.marca}` : 'Marca não informada';
  if (description) description.textContent = product.descricao || 'Sem descrição disponível.';
  if (price) price.textContent = formatPrice(product.preco);
  if (stock) stock.textContent = product.estoque == null ? '0' : String(product.estoque);
  if (available) available.textContent = product.disponivel ? 'Em estoque' : 'Indisponível';

  if (addBtn) {
    if (!product.disponivel) {
      addBtn.disabled = true;
      addBtn.textContent = 'Produto Indisponível';
    } else {
      addBtn.disabled = false;
      addBtn.textContent = '🛒 Adicionar ao Carrinho';
    }
  }
}

async function loadProduct() {
  const productId = getProductId();
  if (!productId) {
    setStatus('Não foi possível identificar o código do produto.', 'error');
    return;
  }

  setStatus('Carregando produto...');
  try {
    const response = await fetch(`/api/produtos/${productId}`, {
      headers: { Accept: 'application/json' },
    });
    if (!response.ok) {
      throw new Error(`Falha ao carregar os detalhes do produto (${response.status})`);
    }

    const product = await response.json();
    updateContent(product);
    setStatus('');
  } catch (error) {
    console.error(error);
    setStatus('Não foi possível carregar os detalhes do produto.', 'error');
  }
}

async function handleAddToCart(e) {
  if (e) e.preventDefault();

  const productId = getProductId();
  if (!productId) {
    showToast('Código do produto não identificado.', 'error');
    return;
  }

  const qtyInput = document.getElementById('produto-qtd-input');
  const quantidade = qtyInput ? Math.max(Number.parseInt(qtyInput.value, 10) || 1, 1) : 1;

  if (currentStock > 0 && quantidade > currentStock) {
    showToast(`Estoque disponível insuficiente (${currentStock} un).`, 'error');
    return;
  }

  const addBtn = document.getElementById('btn-add-detalhes');
  if (addBtn) addBtn.disabled = true;

  try {
    const res = await fetch('/api/carrinho/adicionar', {
      method: 'POST',
      headers: csrfHeaders({ 'Content-Type': 'application/json', Accept: 'application/json' }),
      body: JSON.stringify({ produtoId: productId, quantidade }),
    });

    const errorJson = !res.ok ? await res.json().catch(() => ({})) : null;

    if (!res.ok) {
      const errorMsg = errorJson?.message || errorJson?.detail || errorJson?.error || `Erro (${res.status})`;
      throw new Error(errorMsg);
    }

    await updateHeaderCartCount();
    showToast(`${quantidade} item(ns) adicionado(s) ao carrinho com sucesso!`, 'success');
  } catch (err) {
    console.error('Erro ao adicionar ao carrinho:', err);
    showToast(err.message || 'Erro ao adicionar item ao carrinho.', 'error');
  } finally {
    if (addBtn) addBtn.disabled = false;
  }
}

document.addEventListener('DOMContentLoaded', () => {
  loadProduct();
  updateHeaderCartCount();

  const addBtn = document.getElementById('btn-add-detalhes');
  if (addBtn) {
    addBtn.addEventListener('click', handleAddToCart);
  }

  const minusBtn = document.getElementById('btn-qtd-minus');
  const plusBtn = document.getElementById('btn-qtd-plus');
  const qtyInput = document.getElementById('produto-qtd-input');

  if (minusBtn && qtyInput) {
    minusBtn.addEventListener('click', () => {
      const val = Math.max((Number.parseInt(qtyInput.value, 10) || 1) - 1, 1);
      qtyInput.value = val;
    });
  }

  if (plusBtn && qtyInput) {
    plusBtn.addEventListener('click', () => {
      const current = Number.parseInt(qtyInput.value, 10) || 1;
      if (currentStock > 0 && current >= currentStock) {
        showToast(`Limite máximo do estoque atingido (${currentStock}).`, 'error');
        return;
      }
      qtyInput.value = current + 1;
    });
  }
});
