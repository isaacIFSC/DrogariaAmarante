const PRODUCTS_GRID_ID = 'products-grid';
const PRODUCTS_STATUS_ID = 'products-status';
const PRODUCT_TEMPLATE_ID = 'product-card-template';
const PLACEHOLDER_IMAGE = '/images/produto-placeholder.svg';

const moneyFormatter = new Intl.NumberFormat('pt-BR', {
  style: 'currency',
  currency: 'BRL',
});

function escapeText(value) {
  return value == null ? '' : String(value);
}

function formatPrice(value) {
  const numeric = Number(value);
  if (Number.isFinite(numeric)) {
    return moneyFormatter.format(numeric);
  }

  return escapeText(value);
}

function resolveImageUrl(url) {
  if (!url) {
    return PLACEHOLDER_IMAGE;
  }

  if (/^https?:\/\//i.test(url) || url.startsWith('/')) {
    return url;
  }

  return `/${url.replace(/^\.?\//, '')}`;
}

function setStatus(message, type = 'info') {
  const statusEl = document.getElementById(PRODUCTS_STATUS_ID);
  if (!statusEl) {
    return;
  }

  statusEl.textContent = message;
  statusEl.classList.toggle('is-error', type === 'error');
}

function createLoadingCards(grid) {
  grid.innerHTML = '';

  for (let i = 0; i < 4; i += 1) {
    const card = document.createElement('article');
    card.className = 'product-card is-loading';
    card.setAttribute('aria-hidden', 'true');
    card.innerHTML = `
      <div class="product-image"></div>
      <div class="product-info"></div>
      <button class="add-btn" type="button" tabindex="-1" aria-hidden="true">+</button>
    `;
    grid.appendChild(card);
  }
}

function createProductCard(product, template) {
  const node = template.content.firstElementChild.cloneNode(true);
  const image = node.querySelector('.product-image-el');
  const category = node.querySelector('.category');
  const brand = node.querySelector('.brand');
  const name = node.querySelector('.product-name');
  const description = node.querySelector('.product-description');
  const price = node.querySelector('.product-price');

  image.src = resolveImageUrl(product.imagemUrl);
  image.alt = product.nome ? `Imagem de ${product.nome}` : 'Imagem do produto';
  category.textContent = escapeText(product.categoria || 'Produto');
  brand.textContent = escapeText(product.marca || 'Marca');
  name.textContent = escapeText(product.nome || 'Produto sem nome');
  description.textContent = escapeText(product.descricao || 'Sem descricao disponivel.');
  price.textContent = formatPrice(product.preco);

  if (product.disponivel === false) {
    const badge = document.createElement('span');
    badge.className = 'product-badge';
    badge.textContent = 'Indisponivel';
    node.querySelector('.product-meta').appendChild(badge);
    node.classList.add('product-card--unavailable');
  }

  return node;
}

async function loadProducts() {
  const grid = document.getElementById(PRODUCTS_GRID_ID);
  const template = document.getElementById(PRODUCT_TEMPLATE_ID);

  if (!grid || !template) {
    return;
  }

  createLoadingCards(grid);
  setStatus('Carregando produtos...');

  try {
    const response = await fetch('/api/produtos', {
      headers: { Accept: 'application/json' },
    });

    if (!response.ok) {
      throw new Error(`Falha ao carregar produtos (${response.status})`);
    }

    const products = await response.json();
    grid.innerHTML = '';
    grid.setAttribute('aria-busy', 'false');

    if (!Array.isArray(products) || products.length === 0) {
      grid.innerHTML = '<div class="products-empty">Nenhum produto encontrado no momento.</div>';
      setStatus('Sem produtos para exibir agora.');
      return;
    }

    const fragment = document.createDocumentFragment();
    products.forEach((product) => {
      fragment.appendChild(createProductCard(product, template));
    });

    grid.appendChild(fragment);
    setStatus(`${products.length} produto(s) carregado(s).`);
  } catch (error) {
    grid.innerHTML = '<div class="products-empty">Nao foi possivel carregar os produtos. Tente novamente em instantes.</div>';
    grid.setAttribute('aria-busy', 'false');
    setStatus('Falha ao carregar os produtos.', 'error');
    console.error(error);
  }
}

document.addEventListener('DOMContentLoaded', loadProducts);
