const baseUrl = "http://localhost:8080/api/accounts";

// ===== TOAST NOTIFICATION =====
function showToast(msg, type = 'info') {
    const toast = document.getElementById('toast');
    toast.textContent = msg;
    toast.className = `toast ${type} show`;
    setTimeout(() => { toast.classList.remove('show'); }, 3500);
}

// ===== LOGIN =====
function handleLogin() {
    const u = document.getElementById('loginUser').value.trim();
    const p = document.getElementById('loginPass').value;
    if (!u || !p) { showToast('Please enter credentials', 'error'); return; }
    if (u === "admin" && p === "admin123") {
        const authBox = document.getElementById('authBox');
        authBox.style.animation = 'fadeOut 0.4s ease forwards';
        setTimeout(() => {
            authBox.style.display = "none";
            document.getElementById('homePage').style.display = "flex";
            showToast('Welcome back, Admin! 👋', 'success');
        }, 380);
    } else {
        showToast('Invalid credentials. Try admin / admin123', 'error');
        document.getElementById('loginUser').style.borderColor = 'rgba(248,113,113,0.5)';
        document.getElementById('loginPass').style.borderColor = 'rgba(248,113,113,0.5)';
        setTimeout(() => {
            document.getElementById('loginUser').style.borderColor = '';
            document.getElementById('loginPass').style.borderColor = '';
        }, 2000);
    }
}

// Enter key login
document.addEventListener('DOMContentLoaded', () => {
    document.getElementById('loginPass').addEventListener('keydown', e => {
        if (e.key === 'Enter') handleLogin();
    });

    // Set greeting based on time
    const hour = new Date().getHours();
    const greetEl = document.getElementById('greeting');
    if (greetEl) {
        if (hour < 12) greetEl.textContent = 'Good Morning ☀️';
        else if (hour < 17) greetEl.textContent = 'Good Afternoon 🌤️';
        else greetEl.textContent = 'Good Evening 🌙';
    }

    // Live date display
    const dateBadge = document.getElementById('dateBadge');
    if (dateBadge) {
        const now = new Date();
        dateBadge.textContent = now.toLocaleDateString('en-IN', { weekday: 'short', day: 'numeric', month: 'short', year: 'numeric' });
    }
});

// ===== SECTION SWITCHING =====
function showSection(sectionId) {
    const sections = ['homeContent', 'depositSection', 'transferSection', 'viewBalanceSection', 'withdrawSection'];
    sections.forEach(s => {
        const el = document.getElementById(s);
        if (el) {
            el.style.display = 'none';
            el.classList.remove('active-page');
        }
    });
    const target = document.getElementById(sectionId);
    if (target) {
        target.style.display = 'block';
        target.classList.add('active-page');
        // Reset result box
        const res = document.getElementById('res');
        if (res) res.style.display = 'none';
    }
}

function setActive(el) {
    document.querySelectorAll('.nav-item').forEach(n => n.classList.remove('active'));
    el.classList.add('active');
}

function setActiveBySection(sectionId) {
    const map = {
        'homeContent': 0,
        'viewBalanceSection': 1,
        'depositSection': 2,
        'withdrawSection': 3,
        'transferSection': 4
    };
    const navItems = document.querySelectorAll('.nav-item');
    navItems.forEach(n => n.classList.remove('active'));
    if (map[sectionId] !== undefined && navItems[map[sectionId]]) {
        navItems[map[sectionId]].classList.add('active');
    }
}

function goBack() {
    showSection('homeContent');
    setActiveBySection('homeContent');
}

// ===== SIDEBAR TOGGLE =====
function toggleSidebar() {
    const sidebar = document.getElementById('sidebar');
    sidebar.classList.toggle('hidden');
    sidebar.classList.toggle('mobile-open');
}

// ===== LOGOUT =====
function doLogout() {
    showToast('Logging out... Goodbye! 👋', 'info');
    setTimeout(() => location.reload(), 1200);
}

// ===== BALANCE CHECK =====
async function getAccountDetails() {
    const id = document.getElementById('vId').value.trim();
    if (!id) { showToast('Please enter an Account ID', 'error'); return; }
    const btn = document.querySelector('#viewBalanceSection .btn-action');
    btn.textContent = 'Loading...';
    btn.disabled = true;
    try {
        const res = await fetch(`${baseUrl}/${id}`);
        if (res.ok) {
            const data = await res.json();
            const resBox = document.getElementById('res');
            resBox.style.display = 'block';
            resBox.innerHTML = `
                <div>👤 <strong>Name:</strong> ${data.accountHolderName}</div>
                <div>🆔 <strong>Account ID:</strong> ${data.id ?? id}</div>
                <div>💰 <strong>Balance:</strong> ₹${Number(data.balance).toLocaleString('en-IN')}</div>
            `;
            showToast('Account details loaded ✅', 'success');
        } else {
            showToast('Account not found. Check the ID.', 'error');
        }
    } catch (e) {
        showToast('Server unreachable. Is backend running?', 'error');
    } finally {
        btn.textContent = 'View Details';
        btn.disabled = false;
    }
}

// ===== DEPOSIT / WITHDRAW =====
async function apiCall(type, idField, amtField) {
    const id = document.getElementById(idField).value.trim();
    const amt = document.getElementById(amtField).value.trim();
    if (!id || !amt) { showToast('Fill in all fields', 'error'); return; }
    if (Number(amt) <= 0) { showToast('Amount must be greater than 0', 'error'); return; }
    const btns = document.querySelectorAll('.btn-action');
    btns.forEach(b => b.disabled = true);
    try {
        const r = await fetch(`${baseUrl}/${id}/${type}?amount=${amt}`, { method: 'PUT' });
        if (r.ok) {
            showToast(`${type.charAt(0).toUpperCase() + type.slice(1)} of ₹${Number(amt).toLocaleString('en-IN')} successful! 📧 Email sent.`, 'success');
            setTimeout(() => goBack(), 1500);
        } else {
            const errText = await r.text();
            showToast(errText || 'Transaction failed. Try again.', 'error');
        }
    } catch (e) {
        showToast('Server unreachable. Is backend running?', 'error');
    } finally {
        btns.forEach(b => b.disabled = false);
    }
}

// ===== TRANSFER =====
async function makeTransfer() {
    const fId = document.getElementById('fromId').value.trim();
    const tId = document.getElementById('toId').value.trim();
    const amt = document.getElementById('tAmt').value.trim();
    if (!fId || !tId || !amt) { showToast('Fill in all fields', 'error'); return; }
    if (fId === tId) { showToast('From and To accounts cannot be the same', 'error'); return; }
    if (Number(amt) <= 0) { showToast('Amount must be greater than 0', 'error'); return; }
    const btn = document.querySelector('#transferSection .btn-action');
    btn.textContent = 'Processing...'; btn.disabled = true;
    try {
        const r = await fetch(`${baseUrl}/transfer?fromId=${fId}&toId=${tId}&amount=${amt}`, { method: 'PUT' });
        if (r.ok) {
            showToast(`₹${Number(amt).toLocaleString('en-IN')} transferred successfully! 🎉`, 'success');
            setTimeout(() => goBack(), 1500);
        } else {
            const errText = await r.text();
            showToast(errText || 'Transfer failed. Check balance or IDs.', 'error');
        }
    } catch (e) {
        showToast('Server unreachable. Is backend running?', 'error');
    } finally {
        btn.textContent = 'Transfer Now'; btn.disabled = false;
    }
}

// CSS for fade out animation on logout
const style = document.createElement('style');
style.textContent = `@keyframes fadeOut { to { opacity: 0; transform: translateY(-20px) scale(0.95); } }`;
document.head.appendChild(style);
