const PLACEHOLDER_IMAGE = '/images/produto-placeholder.svg';

const moneyFormatter = new Intl.NumberFormat('pt-BR', {
  style: 'currency',
  currency: 'BRL',
});

function escapeText(value) {
  return value == null ? '' : String(value);
}

function escapeHtml(value) {
  return escapeText(value)
    .replaceAll('&', '&amp;')
    .replaceAll('<', '&lt;')
    .replaceAll('>', '&gt;')
    .replaceAll('"', '&quot;')
    .replaceAll("'", '&#39;');
}

function formatPrice(value) {
  const numeric = Number(value);
  return Number.isFinite(numeric) ? moneyFormatter.format(numeric) : escapeText(value);
}

function setFeedback(id, message, type = 'info') {
  const el = document.getElementById(id);
  if (!el) {
    return;
  }

  el.textContent = message;
  el.classList.toggle('is-error', type === 'error');
  el.classList.toggle('is-success', type === 'success');
}

async function fetchJson(url, options = {}) {
  const response = await fetch(url, {
    headers: { Accept: 'application/json', ...(options.headers || {}) },
    ...options,
  });

  const text = await response.text();
  const data = text ? JSON.parse(text) : null;

  if (!response.ok) {
    const message = data?.message || data?.error || `Falha na requisicao (${response.status})`;
    throw new Error(message);
  }

  return data;
}

async function loadOptions() {
  const [categorias, marcas] = await Promise.all([
    fetchJson('/api/catalogo/categorias'),
    fetchJson('/api/catalogo/marcas'),
  ]);

  const categoriaSelect = document.getElementById('categoria-select');
  const marcaSelect = document.getElementById('marca-select');

  if (categoriaSelect) {
    categoriaSelect.innerHTML = categorias
      .map((item) => `<option value="${escapeHtml(item.nome)}">${escapeHtml(item.nome)}</option>`)
      .join('');
  }

  if (marcaSelect) {
    marcaSelect.innerHTML = marcas
      .map((item) => `<option value="${escapeHtml(item.nome)}">${escapeHtml(item.nome)}</option>`)
      .join('');
  }
}

function renderRecentProducts(products) {
  const grid = document.getElementById('recent-products');
  if (!grid) {
    return;
  }

  grid.setAttribute('aria-busy', 'false');

  if (!Array.isArray(products) || products.length === 0) {
    grid.innerHTML = '<div class="empty-state">Nenhum produto cadastrado ainda.</div>';
    return;
  }

  grid.innerHTML = products.map((product) => `
    <article class="admin-product-card">
      <div class="admin-product-card__image">
        <img src="${escapeHtml(product.imagemUrl || PLACEHOLDER_IMAGE)}" alt="Imagem de ${escapeHtml(product.nome)}" />
      </div>
      <div class="admin-product-card__body">
        <div class="admin-product-card__meta">
          <span>${escapeHtml(product.categoria)}</span>
          <span>${escapeHtml(product.marca)}</span>
        </div>
        <h3>${escapeHtml(product.nome)}</h3>
        <p>${escapeHtml(product.descricao)}</p>
        <strong>${formatPrice(product.preco)}</strong>
        <small>Estoque: ${escapeHtml(product.estoque)} | ${product.disponivel ? 'Disponivel' : 'Indisponivel'}</small>
      </div>
    </article>
  `).join('');
}

async function loadRecentProducts() {
  const grid = document.getElementById('recent-products');
  if (!grid) {
    return;
  }

  grid.setAttribute('aria-busy', 'true');
  setFeedback('recent-status', 'Carregando produtos recentes...');

  try {
    const products = await fetchJson('/api/admin/produtos/recentes');
    renderRecentProducts(products);
    setFeedback('recent-status', `${products.length} produto(s) recentes exibidos.`, 'success');
  } catch (error) {
    grid.setAttribute('aria-busy', 'false');
    grid.innerHTML = '<div class="empty-state">Nao foi possivel carregar os produtos.</div>';
    setFeedback('recent-status', error.message, 'error');
  }
}

async function handleProductSubmit(event) {
  event.preventDefault();
  const form = event.currentTarget;
  const formData = new FormData(form);

  const payload = {
    nome: formData.get('nome'),
    descricao: formData.get('descricao'),
    preco: Number(formData.get('preco')),
    imagemUrl: formData.get('imagemUrl'),
    estoque: Number(formData.get('estoque')),
    categoria: formData.get('categoria'),
    marca: formData.get('marca'),
  };

  setFeedback('form-status', 'Salvando produto...');

  try {
    await fetchJson('/api/admin/produtos', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload),
    });

    form.reset();
    await loadOptions();
    await loadRecentProducts();
    setFeedback('form-status', 'Produto cadastrado com sucesso.', 'success');
  } catch (error) {
    setFeedback('form-status', error.message, 'error');
  }
}

async function handleCsvSubmit(event) {
  event.preventDefault();
  const form = event.currentTarget;
  const input = document.getElementById('csv-file');
  const arquivo = input?.files?.[0];

  if (!arquivo) {
    setFeedback('csv-status', 'Escolha um arquivo CSV de relatorio antes de importar.', 'error');
    return;
  }

  const formData = new FormData();
  formData.append('arquivo', arquivo);
  setFeedback('csv-status', 'Importando relatorio CSV...');

  try {
    const resultado = await fetchJson('/api/admin/produtos/importar-csv', {
      method: 'POST',
      body: formData,
    });

    form.reset();
    await loadRecentProducts();
    const erros = Array.isArray(resultado.erros) && resultado.erros.length > 0
      ? ` ${resultado.erros.length} linha(s) com erro.`
      : '';
    setFeedback(
      'csv-status',
      `${resultado.produtosCriados} produto(s) importado(s) em ${resultado.linhasLidas} linha(s).${erros}`,
      resultado.erros && resultado.erros.length > 0 ? 'error' : 'success'
    );
  } catch (error) {
    setFeedback('csv-status', error.message, 'error');
  }
}

document.addEventListener('DOMContentLoaded', async () => {
  const productForm = document.getElementById('produto-form');
  const csvForm = document.getElementById('csv-form');
  const refreshButton = document.getElementById('refresh-products');

  try {
    await loadOptions();
    await loadRecentProducts();
  } catch (error) {
    setFeedback('recent-status', error.message, 'error');
  }

  productForm?.addEventListener('submit', handleProductSubmit);
  csvForm?.addEventListener('submit', handleCsvSubmit);
  refreshButton?.addEventListener('click', loadRecentProducts);
});
