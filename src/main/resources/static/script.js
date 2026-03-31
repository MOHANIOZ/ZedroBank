/* ===== CONFIG ===== */
const BASE = 'http://localhost:8080/api';

/* ===== STATE ===== */
let currentUser = null;
let currentAcct = null;
let txHistory = [];

/* ===== TOAST ===== */
function toast(msg, type='info') {
  const el = document.getElementById('toast');
  el.textContent = msg;
  el.className = `show ${type}`;
  clearTimeout(el._t);
  el._t = setTimeout(() => el.className = '', 3200);
}

/* ===== CLOCK ===== */
function updateClock() {
  const now = new Date();
  document.getElementById('clock').textContent =
    now.toLocaleTimeString('en-IN', {hour:'2-digit',minute:'2-digit',second:'2-digit'}) +
    '  ' + now.toLocaleDateString('en-IN',{day:'2-digit',month:'short',year:'numeric'});
}
setInterval(updateClock, 1000);
updateClock();

/* ===== AUTH TABS ===== */
function switchTab(tab) {
  const isLogin = tab === 'login';
  document.getElementById('tabLogin').classList.toggle('active', isLogin);
  document.getElementById('tabSignup').classList.toggle('active', !isLogin);
  document.getElementById('loginForm').style.display = isLogin ? 'block' : 'none';
  document.getElementById('signupForm').style.display = isLogin ? 'none' : 'block';
  document.querySelector('.auth-title').textContent = isLogin ? 'Welcome Back' : 'Create Account';
  document.getElementById('authSubText').innerHTML = isLogin
    ? `Don't have an account? <b onclick="switchTab('signup')">Create one</b>`
    : `Already registered? <b onclick="switchTab('login')">Sign in</b>`;
}

function togglePass(id, eye) {
  const inp = document.getElementById(id);
  inp.type = inp.type === 'password' ? 'text' : 'password';
  eye.textContent = inp.type === 'password' ? '👁' : '🙈';
}

/* ===== LOGIN ===== */
async function handleLogin() {
  const u = document.getElementById('lUser').value.trim();
  const p = document.getElementById('lPass').value.trim();
  if (!u) { showErr('lUserErr'); return; } hideErr('lUserErr');
  if (!p) { showErr('lPassErr'); return; } hideErr('lPassErr');

  const btn = document.getElementById('btnLogin');
  btn.innerHTML = '<span class="loader-ring"></span> Signing in…';
  btn.disabled = true;

  try {
    const res = await fetch(`${BASE}/auth/login`, {
      method: 'POST',
      headers: {'Content-Type':'application/json'},
      body: JSON.stringify({username: u, password: p})
    });
    if (res.ok) {
      const data = await res.json();
      loginSuccess(u, data);
    } else {
      toast('Invalid credentials. Please try again.', 'error');
    }
  } catch(e) {
    toast('Backend not reachable. Using demo mode.', 'warn');
    loginSuccess(u, {id:1, accountHolderName: u, balance: 50000});
  } finally {
    btn.innerHTML = 'Sign In →'; btn.disabled = false;
  }
}

/* ===== SIGNUP ===== */
async function handleSignup() {
  const name = document.getElementById('sName').value.trim();
  const u    = document.getElementById('sUser').value.trim();
  const p    = document.getElementById('sPass').value.trim();
  if (!name) { showErr('sNameErr'); return; } hideErr('sNameErr');
  if (!u)    { showErr('sUserErr'); return; } hideErr('sUserErr');
  if (p.length < 6) { showErr('sPassErr'); return; } hideErr('sPassErr');

  const btn = document.getElementById('btnSignup');
  btn.innerHTML = '<span class="loader-ring"></span> Creating account…';
  btn.disabled = true;

  try {
    const res = await fetch(`${BASE}/auth/register`, {
      method: 'POST',
      headers: {'Content-Type':'application/json'},
      body: JSON.stringify({username: u, password: p, accountHolderName: name})
    });
    if (res.ok) {
      toast('Account created! Please sign in.', 'success');
      switchTab('login');
    } else {
      const err = await res.text();
      toast(err || 'Registration failed.', 'error');
    }
  } catch(e) {
    toast('Backend not reachable. Please check your server.', 'error');
  } finally {
    btn.innerHTML = 'Create Account →'; btn.disabled = false;
  }
}

function loginSuccess(username, acctData) {
  currentUser = username;
  currentAcct = acctData;
  document.getElementById('sbUsername').textContent = username;
  document.getElementById('dcName').textContent = acctData.accountHolderName || username;
  updateDashboard();
  document.getElementById('authScreen').style.display = 'none';
  document.getElementById('appScreen').style.display  = 'block';
  toast(`Welcome back, ${username}! 👋`, 'success');
}

function handleLogout() {
  currentUser = null; currentAcct = null; txHistory = [];
  document.getElementById('authScreen').style.display = 'flex';
  document.getElementById('appScreen').style.display  = 'none';
  switchTab('login');
  document.getElementById('lUser').value = '';
  document.getElementById('lPass').value = '';
  toast('Signed out successfully.', 'info');
}

/* ===== DASHBOARD ===== */
function updateDashboard() {
  if (!currentAcct) return;
  document.getElementById('dashAcctId').textContent  = currentAcct.id || '—';
  document.getElementById('dashBalance').textContent = `₹ ${fmt(currentAcct.balance || 0)}`;
  document.getElementById('dashName').textContent    = currentAcct.accountHolderName || currentUser;
  renderDashTx();
}

function renderDashTx() {
  const list = document.getElementById('dashTxList');
  if (!txHistory.length) {
    list.innerHTML = `<div class="empty-state"><div class="es-icon">📭</div><p>No transactions yet.</p></div>`;
    return;
  }
  const recent = [...txHistory].reverse().slice(0, 5);
  list.innerHTML = recent.map(tx => {
    const icon = tx.type==='deposit' ? '💰' : tx.type==='withdraw' ? '💸' : '🔄';
    const cls  = tx.type==='deposit' ? 'dep' : tx.type==='withdraw' ? 'wdr' : 'trn';
    const amtCls = tx.type==='deposit' ? 'pos' : 'neg';
    const sign   = tx.type==='deposit' ? '+' : '-';
    return `<div class="tx-row">
      <div class="tx-dot ${cls}">${icon}</div>
      <div class="tx-info">
        <div class="tx-type">${capitalize(tx.type)}</div>
        <div class="tx-desc">${tx.desc}</div>
      </div>
      <div>
        <div class="tx-amt ${amtCls}">${sign}₹${fmt(tx.amount)}</div>
        <div class="tx-time">${tx.time}</div>
      </div>
    </div>`;
  }).join('');
}

/* ===== NAV ===== */
const pageTitles = {
  pgDash:'Dashboard', pgBalance:'Check Balance',
  pgDeposit:'Deposit', pgWithdraw:'Withdraw',
  pgTransfer:'Transfer', pgHistory:'Transaction History'
};

function goPage(pageId, navEl) {
  document.querySelectorAll('.page').forEach(p => p.classList.remove('active'));
  document.querySelectorAll('.nav-item').forEach(n => n.classList.remove('active'));
  document.getElementById(pageId).classList.add('active');
  if (navEl) navEl.classList.add('active');
  document.getElementById('pageTitle').textContent = pageTitles[pageId] || '';
  if (pageId === 'pgHistory') renderHistory();
  if (pageId === 'pgDash') updateDashboard();
}

/* ===== CHECK BALANCE ===== */
async function checkBalance() {
  const id = document.getElementById('balAcctId').value;
  if (!id) { toast('Please enter an account ID', 'error'); return; }

  const btn = document.getElementById('btnBalance');
  btn.innerHTML = '<span class="loader-ring"></span> Fetching…';
  btn.disabled = true;

  try {
    const res = await fetch(`${BASE}/accounts/${id}`);
    if (res.ok) {
      const data = await res.json();
      document.getElementById('balName').textContent = data.accountHolderName;
      document.getElementById('balId').textContent   = data.id;
      document.getElementById('balAmt').textContent  = `₹ ${fmt(data.balance)}`;
      document.getElementById('balResult').classList.add('show');
      if (currentAcct && data.id == currentAcct.id) {
        currentAcct.balance = data.balance;
        updateDashboard();
      }
    } else {
      toast('Account not found.', 'error');
    }
  } catch(e) {
    toast('Could not reach server.', 'error');
  } finally {
    btn.innerHTML = 'View Balance →'; btn.disabled = false;
  }
}

/* ===== DEPOSIT ===== */
async function doDeposit() {
  const id  = document.getElementById('depId').value;
  const amt = document.getElementById('depAmt').value;
  if (!id || !amt || amt <= 0) { toast('Please fill all fields correctly.', 'error'); return; }

  const btn = document.getElementById('btnDeposit');
  btn.innerHTML = '<span class="loader-ring"></span> Processing…'; btn.disabled = true;

  try {
    const res = await fetch(`${BASE}/accounts/${id}/deposit?amount=${amt}`, {method:'PUT'});
    if (res.ok) {
      const data = await res.json();
      logTx('deposit', parseFloat(amt), `To Account #${id}`);
      if (currentAcct && data.id == currentAcct.id) { currentAcct.balance = data.balance; }
      toast(`₹${fmt(amt)} deposited successfully! ✅`, 'success');
      document.getElementById('depId').value = '';
      document.getElementById('depAmt').value = '';
    } else {
      toast('Deposit failed. Please check the account ID.', 'error');
    }
  } catch(e) {
    toast('Could not reach server.', 'error');
  } finally {
    btn.innerHTML = 'Confirm Deposit →'; btn.disabled = false;
  }
}

/* ===== WITHDRAW ===== */
async function doWithdraw() {
  const id  = document.getElementById('wdrId').value;
  const amt = document.getElementById('wdrAmt').value;
  if (!id || !amt || amt <= 0) { toast('Please fill all fields correctly.', 'error'); return; }

  const btn = document.getElementById('btnWithdraw');
  btn.innerHTML = '<span class="loader-ring"></span> Processing…'; btn.disabled = true;

  try {
    const res = await fetch(`${BASE}/accounts/${id}/withdraw?amount=${amt}`, {method:'PUT'});
    if (res.ok) {
      const data = await res.json();
      logTx('withdraw', parseFloat(amt), `From Account #${id}`);
      if (currentAcct && data.id == currentAcct.id) { currentAcct.balance = data.balance; }
      toast(`₹${fmt(amt)} withdrawn successfully! ✅`, 'success');
      document.getElementById('wdrId').value = '';
      document.getElementById('wdrAmt').value = '';
    } else {
      const err = await res.text();
      toast(err || 'Withdrawal failed. Insufficient balance?', 'error');
    }
  } catch(e) {
    toast('Could not reach server.', 'error');
  } finally {
    btn.innerHTML = 'Confirm Withdrawal →'; btn.disabled = false;
  }
}

/* ===== TRANSFER ===== */
async function doTransfer() {
  const from = document.getElementById('trFrom').value;
  const to   = document.getElementById('trTo').value;
  const amt  = document.getElementById('trAmt').value;
  if (!from || !to || !amt || amt <= 0) { toast('Please fill all fields correctly.', 'error'); return; }
  if (from === to) { toast('From and To accounts must be different.', 'error'); return; }

  const btn = document.getElementById('btnTransfer');
  btn.innerHTML = '<span class="loader-ring"></span> Transferring…'; btn.disabled = true;

  try {
    const res = await fetch(`${BASE}/accounts/transfer?fromId=${from}&toId=${to}&amount=${amt}`, {method:'PUT'});
    if (res.ok) {
      logTx('transfer', parseFloat(amt), `Account #${from} → #${to}`);
      toast(`₹${fmt(amt)} transferred from #${from} to #${to}! ✅`, 'success');
      document.getElementById('trFrom').value = '';
      document.getElementById('trTo').value   = '';
      document.getElementById('trAmt').value  = '';
    } else {
      const err = await res.text();
      toast(err || 'Transfer failed. Check IDs and balance.', 'error');
    }
  } catch(e) {
    toast('Could not reach server.', 'error');
  } finally {
    btn.innerHTML = 'Transfer Now →'; btn.disabled = false;
  }
}

/* ===== HISTORY ===== */
function logTx(type, amount, desc) {
  txHistory.push({
    type, amount, desc,
    time: new Date().toLocaleTimeString('en-IN',{hour:'2-digit',minute:'2-digit'})
  });
}

function renderHistory() {
  const el = document.getElementById('historyContent');
  if (!txHistory.length) {
    el.innerHTML = `<div class="empty-state"><div class="es-icon">📋</div><p>No transaction history yet. Make a deposit to get started!</p></div>`;
    return;
  }
  const rows = [...txHistory].reverse().map((tx,i) => {
    const badge = `<span class="badge badge-${tx.type==='deposit'?'dep':tx.type==='withdraw'?'wdr':'trn'}">${tx.type}</span>`;
    const sign  = tx.type === 'deposit' ? '+' : '-';
    const amtCls= tx.type === 'deposit' ? 'style="color:var(--success)"' : 'style="color:var(--error)"';
    return `<tr>
      <td>${txHistory.length - i}</td>
      <td>${badge}</td>
      <td>${tx.desc}</td>
      <td ${amtCls}>${sign}₹${fmt(tx.amount)}</td>
      <td>${tx.time}</td>
    </tr>`;
  }).join('');
  el.innerHTML = `
    <table class="history-table">
      <thead>
        <tr>
          <th>#</th><th>Type</th><th>Description</th><th>Amount</th><th>Time</th>
        </tr>
      </thead>
      <tbody>${rows}</tbody>
    </table>`;
}

/* ===== HELPERS ===== */
function fmt(n) {
  return Number(n).toLocaleString('en-IN', {minimumFractionDigits:2, maximumFractionDigits:2});
}
function capitalize(s) { return s.charAt(0).toUpperCase() + s.slice(1); }
function showErr(id) { document.getElementById(id).classList.add('show'); }
function hideErr(id) { document.getElementById(id).classList.remove('show'); }

/* ===== ENTER KEY SUPPORT ===== */
document.addEventListener('keydown', e => {
  if (e.key === 'Enter') {
    if (document.getElementById('loginForm').style.display !== 'none') handleLogin();
    else if (document.getElementById('signupForm').style.display !== 'none') handleSignup();
  }
});