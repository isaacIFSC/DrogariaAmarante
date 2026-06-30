const PRODUCT_ID = document.body?.dataset?.produtoId;
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

  if (/^https?:\/\//i.test(url) || url.startsWith('/') || url.startsWith('data:')) {
    return url;
  }

  return `/${url.replace(/^\.?\//, '')}`;
}

function setStatus(message, type = 'info') {
  const statusEl = document.getElementById('produto-status');
  if (!statusEl) {
    return;
  }

  statusEl.textContent = message;
  statusEl.classList.toggle('is-error', type === 'error');
}

function updateContent(product) {
  const image = document.getElementById('produto-imagem');
  const breadcrumb = document.getElementById('produto-breadcrumb');
  const category = document.getElementById('produto-categoria');
  const name = document.getElementById('produto-nome');
  const brand = document.getElementById('produto-marca');
  const description = document.getElementById('produto-descricao');
  const price = document.getElementById('produto-preco');
  const stock = document.getElementById('produto-estoque');
  const available = document.getElementById('produto-disponivel');

  if (image) {
    image.src = resolveImageUrl(product.imagemUrl);
    image.alt = product.nome ? `Imagem de ${product.nome}` : 'Imagem do produto';
  }

  if (breadcrumb) {
    breadcrumb.textContent = product.nome || 'Produto';
  }

  if (category) {
    category.textContent = product.categoria || 'Categoria não informada';
  }

  if (name) {
    name.textContent = product.nome || 'Produto sem nome';
  }

  if (brand) {
    brand.textContent = product.marca ? `Marca: ${product.marca}` : 'Marca não informada';
  }

  if (description) {
    description.textContent = product.descricao || 'Sem descrição disponível.';
  }

  if (price) {
    price.textContent = formatPrice(product.preco);
  }

  if (stock) {
    stock.textContent = product.estoque == null ? 'Indisponível' : String(product.estoque);
  }

  if (available) {
    available.textContent = product.disponivel ? 'Disponível' : 'Indisponível';
  }
}

async function loadProduct() {
  if (!PRODUCT_ID) {
    setStatus('Não foi possível identificar o produto.', 'error');
    return;
  }

  setStatus('Carregando produto...');

  try {
    const response = await fetch(`/api/produtos/${PRODUCT_ID}`, {
      headers: { Accept: 'application/json' },
    });

    if (!response.ok) {
      throw new Error(`Falha ao carregar o produto (${response.status})`);
    }

    const product = await response.json();
    updateContent(product);
    setStatus('Produto carregado com sucesso.');
  } catch (error) {
    console.error(error);
    setStatus('Nao foi possivel carregar os detalhes do produto.', 'error');
  }
}

document.addEventListener('DOMContentLoaded', loadProduct);
