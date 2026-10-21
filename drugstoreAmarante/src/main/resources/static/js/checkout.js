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

function escapeHtml(value) {
  return String(value ?? '').replace(/[&<>"']/g, (char) => ({
    '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;',
  })[char]);
}

async function loadCheckoutCartSummary() {
  try {
    const res = await fetch('/api/carrinho', { headers: { Accept: 'application/json' } });
    if (!res.ok) return;
    const data = await res.json();
    updateHeaderCount(data.quantidadeTotalItens);

    const subtotalEl = document.getElementById('checkout-subtotal');
    const freteEl = document.getElementById('checkout-frete');
    const descontoEl = document.getElementById('checkout-desconto');
    const totalEl = document.getElementById('checkout-total');

    if (subtotalEl) subtotalEl.textContent = data.subtotalFormatado;
    if (freteEl) freteEl.textContent = data.freteFormatado;
    if (descontoEl) descontoEl.textContent = data.descontoFormatado;
    if (totalEl) totalEl.textContent = data.totalFormatado;

    if (!data.itens || data.itens.length === 0) {
      showToast('Seu carrinho está vazio.', 'error');
      setTimeout(() => {
        window.location.href = '/carrinho';
      }, 1500);
    }
  } catch (err) {
    console.error(err);
  }
}

function setupPaymentMethods() {
  const methodRadios = document.querySelectorAll('input[name="paymentMethod"]');

  methodRadios.forEach((radio) => {
    radio.addEventListener('change', () => {
      document.querySelectorAll('.payment-method').forEach((m) => m.classList.remove('payment-method--selected'));
      radio.closest('.payment-method').classList.add('payment-method--selected');

    });
  });
}

async function handleConfirmPayment(e) {
  if (e) e.preventDefault();

  const nomeInput = document.getElementById('checkout-nome');
  const emailInput = document.getElementById('checkout-email');
  const cpfInput = document.getElementById('checkout-cpf');
  const telefoneInput = document.getElementById('checkout-telefone');
  const logradouroInput = document.getElementById('checkout-logradouro');
  const numeroInput = document.getElementById('checkout-numero');
  const bairroInput = document.getElementById('checkout-bairro');
  const cidadeInput = document.getElementById('checkout-cidade');
  const ufInput = document.getElementById('checkout-uf');
  const cepInput = document.getElementById('checkout-cep');

  const selectedMethod = document.querySelector('input[name="paymentMethod"]:checked');
  const formaPagamento = selectedMethod ? selectedMethod.value : 'CARTAO';

  const body = {
    nomeCliente: nomeInput ? nomeInput.value.trim() : '',
    emailCliente: emailInput ? emailInput.value.trim() : '',
    cpfCliente: cpfInput ? cpfInput.value.trim() : '',
    telefoneCliente: telefoneInput ? telefoneInput.value.trim() : '',
    enderecoLogradouro: logradouroInput ? logradouroInput.value.trim() : '',
    enderecoNumero: numeroInput ? numeroInput.value.trim() : '',
    enderecoBairro: bairroInput ? bairroInput.value.trim() : '',
    enderecoCidade: cidadeInput ? cidadeInput.value.trim() : '',
    enderecoUf: ufInput ? ufInput.value.trim() : 'SP',
    enderecoCep: cepInput ? cepInput.value.trim() : '',
    formaPagamento,
  };

  if (!body.nomeCliente || !body.emailCliente || !body.enderecoLogradouro || !body.enderecoNumero || !body.enderecoCep) {
    showToast('Por favor, preencha todos os campos obrigatórios do formulário.', 'error');
    return;
  }

  const btnConfirmar = document.getElementById('btn-confirmar-pagamento');
  if (btnConfirmar) {
    btnConfirmar.disabled = true;
    btnConfirmar.textContent = 'Registrando pedido...';
  }

  try {
    const res = await fetch('/api/checkout/finalizar', {
      method: 'POST',
      headers: csrfHeaders({ 'Content-Type': 'application/json', Accept: 'application/json' }),
      body: JSON.stringify(body),
    });

    if (!res.ok) {
      const err = await res.json().catch(() => ({}));
      throw new Error(err.message || 'Falha ao processar pagamento.');
    }

    const pedido = await res.json();
    renderOrderConfirmation(pedido);
  } catch (err) {
    showToast(err.message, 'error');
    if (btnConfirmar) {
      btnConfirmar.disabled = false;
      btnConfirmar.textContent = 'Registrar pedido pendente';
    }
  }
}

function renderOrderConfirmation(pedido) {
  const checkoutMain = document.getElementById('checkout-main-content');
  if (!checkoutMain) return;

  updateHeaderCount(0);

  checkoutMain.innerHTML = `
    <div class="order-success-card">
      <div class="order-success-icon">🎉</div>
      <h2>Pedido registrado</h2>
      <p class="order-success-subtitle">O pedido aguarda confirmação de pagamento. Nenhuma cobrança foi realizada.</p>

      <div class="order-details-box">
        <div class="order-detail-row">
          <span>Código de Rastreio:</span>
          <strong class="order-code">${escapeHtml(pedido.codigoRastreio)}</strong>
        </div>
        <div class="order-detail-row">
          <span>Status do Pedido:</span>
          <span class="status-badge">${escapeHtml(pedido.statusDescricao)}</span>
        </div>
        <div class="order-detail-row">
          <span>Forma de Pagamento:</span>
          <strong>${escapeHtml(pedido.formaPagamento)}</strong>
        </div>
        <div class="order-detail-row">
          <span>Valor Total:</span>
          <strong class="order-total-price">${pedido.totalFormatado}</strong>
        </div>
        <div class="order-detail-row">
          <span>Endereço de Entrega:</span>
          <span>${escapeHtml(pedido.enderecoCompleto)}</span>
        </div>
      </div>

      <div class="order-items-preview">
        <h3>Itens do Pedido (${pedido.itens ? pedido.itens.length : 0})</h3>
        <ul class="order-items-list">
          ${(pedido.itens || []).map((item) => `
            <li>
              <span>${escapeHtml(item.quantidade)}x ${escapeHtml(item.nomeProduto)}</span>
              <strong>${escapeHtml(item.subtotalFormatado)}</strong>
            </li>
          `).join('')}
        </ul>
      </div>

      <div class="order-success-actions">
        <a class="btn-primary" href="/">Voltar para a Loja</a>
        <a class="btn-secondary" href="/perfil">Ver meus pedidos</a>
      </div>
    </div>
  `;

  window.scrollTo({ top: 0, behavior: 'smooth' });
}

document.addEventListener('DOMContentLoaded', () => {
  loadCheckoutCartSummary();
  setupPaymentMethods();

  const btnConfirmar = document.getElementById('btn-confirmar-pagamento');
  if (btnConfirmar) {
    btnConfirmar.addEventListener('click', handleConfirmPayment);
  }

});
