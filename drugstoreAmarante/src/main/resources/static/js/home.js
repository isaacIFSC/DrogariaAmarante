const PRODUCTS_GRID_ID = 'products-grid';
const PRODUCTS_STATUS_ID = 'products-status';
const PRODUCT_TEMPLATE_ID = 'product-card-template';
const PLACEHOLDER_IMAGE = '/images/produto-placeholder.svg';

const moneyFormatter = new Intl.NumberFormat('pt-BR', {
  style: 'currency',
  currency: 'BRL',
});

const filterElements = {
  search: document.getElementById('campo-busca'),
  category: document.getElementById('filter-category'),
  brand: document.getElementById('filter-brand'),
  minPrice: document.getElementById('filter-min-price'),
  maxPrice: document.getElementById('filter-max-price'),
  availability: document.getElementById('filter-availability'),
  sort: document.getElementById('catalog-sort'),
};

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

async function updateHeaderCartCount() {
  try {
    const res = await fetch('/api/carrinho', { headers: { Accept: 'application/json' } });
    if (res.ok) {
      const data = await res.json();
      const countEls = document.querySelectorAll('.badge, #header-cart-count');
      countEls.forEach((el) => {
        el.textContent = data.quantidadeTotalItens || 0;
        el.setAttribute('aria-label', `${data.quantidadeTotalItens || 0} itens`);
      });
    }
  } catch (err) {
    console.error('Erro ao atualizar contador do carrinho:', err);
  }
}

async function addToCart(produtoId, event) {
  if (event) {
    event.preventDefault();
    event.stopPropagation();
  }

  const pId = Number.parseInt(produtoId, 10);
  if (!pId || Number.isNaN(pId)) {
    showToast('Código do produto inválido.', 'error');
    return;
  }

  try {
    const res = await fetch('/api/carrinho/adicionar', {
      method: 'POST',
      headers: csrfHeaders({ 'Content-Type': 'application/json', Accept: 'application/json' }),
      body: JSON.stringify({ produtoId: pId, quantidade: 1 }),
    });

    const errorJson = !res.ok ? await res.json().catch(() => ({})) : null;

    if (!res.ok) {
      const errorMsg = errorJson?.message || errorJson?.detail || errorJson?.error || `Erro (${res.status})`;
      throw new Error(errorMsg);
    }

    const data = await res.json();
    const countEls = document.querySelectorAll('.badge, #header-cart-count');
    countEls.forEach((el) => {
      el.textContent = data.quantidadeTotalItens || 0;
    });

    showToast('Produto adicionado ao carrinho com sucesso!', 'success');
  } catch (err) {
    console.error('Erro ao adicionar ao carrinho:', err);
    showToast(err.message || 'Não foi possível adicionar o produto.', 'error');
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
  const statusEl = document.getElementById(PRODUCTS_STATUS_ID);
  if (!statusEl) return;
  statusEl.textContent = message;
  statusEl.classList.toggle('is-error', type === 'error');
}

function createLoadingCards(grid) {
  grid.innerHTML = '';
  for (let index = 0; index < 4; index += 1) {
    const card = document.createElement('article');
    card.className = 'product-card is-loading';
    card.setAttribute('aria-hidden', 'true');
    card.innerHTML = '<div class="product-image"></div><div class="product-info"></div>';
    grid.appendChild(card);
  }
}

function currentFilters() {
  const params = new URLSearchParams(window.location.search);
  return {
    q: params.get('q') || '',
    categoria: params.get('categoria') || '',
    marca: params.get('marca') || '',
    minPreco: params.get('minPreco') || '',
    maxPreco: params.get('maxPreco') || '',
    disponivel: params.get('disponivel') || '',
    ordenacao: params.get('ordenacao') || '',
    pagina: Math.max(Number.parseInt(params.get('pagina'), 10) || 0, 0),
  };
}

function writeFilters(filters, replace = false) {
  const params = new URLSearchParams();
  Object.entries(filters).forEach(([key, value]) => {
    if (value !== '' && value != null && !(key === 'pagina' && value === 0)) {
      params.set(key, String(value));
    }
  });

  const query = params.toString();
  const url = query ? `/?${query}` : '/';
  window.history[replace ? 'replaceState' : 'pushState']({}, '', url);
}

function syncFilterInputs(filters) {
  if (filterElements.search) filterElements.search.value = filters.q;
  if (filterElements.category) filterElements.category.value = filters.categoria;
  if (filterElements.brand) filterElements.brand.value = filters.marca;
  if (filterElements.minPrice) filterElements.minPrice.value = filters.minPreco;
  if (filterElements.maxPrice) filterElements.maxPrice.value = filters.maxPreco;
  if (filterElements.availability) filterElements.availability.value = filters.disponivel;
  if (filterElements.sort) filterElements.sort.value = filters.ordenacao;
}

function createProductCard(product, template) {
  const node = template.content.firstElementChild.cloneNode(true);
  const detailPath = product?.id != null ? `/produto/${product.id}` : '/';
  const link = node.querySelector('.product-card__link');
  const action = node.querySelector('.product-card__action');
  const image = node.querySelector('.product-image-el');

  if (link) {
    link.href = detailPath;
    link.setAttribute('aria-label', product.nome ? `Abrir ${product.nome}` : 'Abrir detalhes do produto');
  }
  if (action) {
    action.href = detailPath;
  }
  if (image) {
    image.src = resolveImageUrl(product.imagemUrl);
    image.alt = product.nome ? `Imagem de ${product.nome}` : 'Imagem do produto';
  }

  const categoryEl = node.querySelector('.category');
  if (categoryEl) categoryEl.textContent = escapeText(product.categoria || 'Produto');

  const brandEl = node.querySelector('.brand');
  if (brandEl) brandEl.textContent = escapeText(product.marca || 'Marca');

  const nameEl = node.querySelector('.product-name');
  if (nameEl) nameEl.textContent = escapeText(product.nome || 'Produto sem nome');

  const descEl = node.querySelector('.product-description');
  if (descEl) descEl.textContent = escapeText(product.descricao || 'Sem descrição disponível.');

  const priceEl = node.querySelector('.product-price');
  if (priceEl) priceEl.textContent = formatPrice(product.preco);

  const addBtn = node.querySelector('.btn-add-cart');
  if (addBtn) {
    if (product.disponivel === false) {
      addBtn.disabled = true;
      addBtn.textContent = 'Indisponível';
    } else {
      addBtn.addEventListener('click', (e) => addToCart(product.id, e));
    }
  }

  if (product.disponivel === false) {
    const badge = document.createElement('span');
    badge.className = 'product-badge product-badge--unavailable';
    badge.textContent = 'Indisponível';
    const meta = node.querySelector('.product-meta');
    if (meta) meta.appendChild(badge);
    node.classList.add('product-card--unavailable');
  }

  return node;
}

function renderPagination(result) {
  const pagination = document.getElementById('catalog-pagination');
  if (!pagination) return;
  pagination.innerHTML = '';
  if (result.totalPaginas <= 1) return;

  const createButton = (label, page, disabled, current = false, modifier = '') => {
    const button = document.createElement('button');
    button.type = 'button';
    button.textContent = label;
    button.disabled = disabled;
    button.className = modifier;
    if (current) button.classList.add('is-current');
    button.setAttribute('aria-label', modifier === 'catalog-pagination__previous'
      ? 'Página anterior'
      : modifier === 'catalog-pagination__next' ? 'Próxima página' : `Ir para página ${page + 1}`);
    if (current) button.setAttribute('aria-current', 'page');
    button.addEventListener('click', () => navigateToPage(page));
    pagination.appendChild(button);
  };

  createButton('Anterior', result.pagina - 1, !result.temPaginaAnterior, false, 'catalog-pagination__previous');

  const visiblePages = new Set([0, result.totalPaginas - 1]);
  for (let page = Math.max(0, result.pagina - 1); page <= Math.min(result.totalPaginas - 1, result.pagina + 1); page += 1) {
    visiblePages.add(page);
  }

  let previousPage = -1;
  [...visiblePages].sort((a, b) => a - b).forEach((page) => {
    if (page - previousPage > 1) {
      const ellipsis = document.createElement('span');
      ellipsis.className = 'catalog-pagination__ellipsis';
      ellipsis.textContent = '…';
      ellipsis.setAttribute('aria-hidden', 'true');
      pagination.appendChild(ellipsis);
    }
    createButton(String(page + 1), page, false, page === result.pagina, 'catalog-pagination__page');
    previousPage = page;
  });

  const summary = document.createElement('span');
  summary.className = 'catalog-pagination__summary';
  summary.textContent = `Página ${result.pagina + 1} de ${result.totalPaginas}`;
  summary.setAttribute('aria-live', 'polite');
  pagination.appendChild(summary);

  createButton('Próxima', result.pagina + 1, !result.temProximaPagina, false, 'catalog-pagination__next');
}

function renderProducts(result) {
  const grid = document.getElementById(PRODUCTS_GRID_ID);
  const template = document.getElementById(PRODUCT_TEMPLATE_ID);
  if (!grid) return;
  grid.innerHTML = '';
  grid.setAttribute('aria-busy', 'false');

  if (!Array.isArray(result.produtos) || result.produtos.length === 0) {
    grid.innerHTML = '<div class="products-empty">Nenhum produto foi encontrado com esses filtros.</div>';
    setStatus('Nenhum produto encontrado. Ajuste os filtros e tente novamente.');
    renderPagination(result);
    return;
  }

  const fragment = document.createDocumentFragment();
  result.produtos.forEach((product) => fragment.appendChild(createProductCard(product, template)));
  grid.appendChild(fragment);
  setStatus(`Exibindo ${result.produtos.length} de ${result.totalProdutos} produto(s).`);
  renderPagination(result);
}

async function loadProducts() {
  const grid = document.getElementById(PRODUCTS_GRID_ID);
  if (!grid) return;

  const filters = currentFilters();
  syncFilterInputs(filters);
  createLoadingCards(grid);
  setStatus('Carregando produtos...');
  const params = new URLSearchParams({ tamanho: '12', pagina: String(filters.pagina) });
  ['q', 'categoria', 'marca', 'minPreco', 'maxPreco', 'disponivel', 'ordenacao'].forEach((key) => {
    if (filters[key]) params.set(key, filters[key]);
  });

  try {
    const response = await fetch(`/api/catalogo/produtos?${params}`, { headers: { Accept: 'application/json' } });
    if (!response.ok) {
      const error = await response.json().catch(() => ({}));
      throw new Error(error.message || `Falha ao carregar produtos (${response.status})`);
    }
    renderProducts(await response.json());
  } catch (error) {
    grid.innerHTML = '<div class="products-empty">Não foi possível carregar os produtos. Tente novamente em instantes.</div>';
    grid.setAttribute('aria-busy', 'false');
    const pagination = document.getElementById('catalog-pagination');
    if (pagination) pagination.innerHTML = '';
    setStatus(error.message, 'error');
    console.error(error);
  }
}

function navigateToPage(page) {
  const filters = currentFilters();
  writeFilters({ ...filters, pagina: page });
  loadProducts();
  const catalogoEl = document.getElementById('catalogo');
  if (catalogoEl) catalogoEl.scrollIntoView({ behavior: 'smooth', block: 'start' });
}

function populateSelect(select, items, selectedValue) {
  if (!select) return;
  items.forEach((item) => {
    const option = document.createElement('option');
    option.value = item.nome;
    option.textContent = item.nome;
    select.appendChild(option);
  });
  select.value = selectedValue;
}

function renderCategoryChips(categories) {
  const chips = document.getElementById('category-chips');
  if (!chips) return;
  const filters = currentFilters();
  chips.innerHTML = '';

  const createChip = (label, category) => {
    const button = document.createElement('button');
    button.type = 'button';
    button.textContent = label;
    button.className = filters.categoria === category ? 'is-active' : '';
    button.addEventListener('click', () => {
      writeFilters({ ...currentFilters(), categoria: category, pagina: 0 });
      loadProducts();
    });
    chips.appendChild(button);
  };

  createChip('Todas', '');
  categories.forEach((category) => createChip(category.nome, category.nome));
}

async function loadCatalogMetadata() {
  try {
    const [categoriesResponse, brandsResponse] = await Promise.all([
      fetch('/api/catalogo/categorias', { headers: { Accept: 'application/json' } }),
      fetch('/api/catalogo/marcas', { headers: { Accept: 'application/json' } }),
    ]);
    if (!categoriesResponse.ok || !brandsResponse.ok) {
      throw new Error('Não foi possível carregar os filtros do catálogo.');
    }

    const [categories, brands] = await Promise.all([categoriesResponse.json(), brandsResponse.json()]);
    const filters = currentFilters();
    populateSelect(filterElements.category, categories, filters.categoria);
    populateSelect(filterElements.brand, brands, filters.marca);
    renderCategoryChips(categories);
  } catch (error) {
    setStatus(error.message, 'error');
    console.error(error);
  }
}

function formFilters() {
  return {
    q: filterElements.search ? filterElements.search.value.trim() : '',
    categoria: filterElements.category ? filterElements.category.value : '',
    marca: filterElements.brand ? filterElements.brand.value : '',
    minPreco: filterElements.minPrice ? filterElements.minPrice.value : '',
    maxPreco: filterElements.maxPrice ? filterElements.maxPrice.value : '',
    disponivel: filterElements.availability ? filterElements.availability.value : '',
    ordenacao: filterElements.sort ? filterElements.sort.value : '',
    pagina: 0,
  };
}

function submitFilters(event) {
  event.preventDefault();
  writeFilters(formFilters());
  loadProducts();
}

document.addEventListener('DOMContentLoaded', () => {
  const searchForm = document.getElementById('catalog-search-form');
  if (searchForm) searchForm.addEventListener('submit', submitFilters);

  const filtersForm = document.getElementById('catalog-filters-form');
  if (filtersForm) filtersForm.addEventListener('submit', submitFilters);

  if (filterElements.sort) {
    filterElements.sort.addEventListener('change', () => {
      writeFilters({ ...currentFilters(), ordenacao: filterElements.sort.value, pagina: 0 });
      loadProducts();
    });
  }

  const clearBtn = document.getElementById('clear-filters');
  if (clearBtn) {
    clearBtn.addEventListener('click', () => {
      writeFilters({}, true);
      loadProducts();
    });
  }

  window.addEventListener('popstate', loadProducts);
  loadCatalogMetadata();
  loadProducts();
  updateHeaderCartCount();
});
