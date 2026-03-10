const baseUrl = "https://zedrobank.onrender.com/api/v1/accounts";

function toggleAuth(isSignup) {
    document.getElementById('loginForm').style.display = isSignup ? 'none' : 'block';
    document.getElementById('signupForm').style.display = isSignup ? 'block' : 'none';
}

function showSection(id) {
    document.querySelectorAll('.page').forEach(p => p.style.display = 'none');
    document.getElementById(id).style.display = 'block';
}

function showToast(msg) {
    const t = document.getElementById('toast');
    t.textContent = msg; t.classList.add('show');
    setTimeout(() => t.classList.remove('show'), 3000);
}

// LOGIN Logic
async function handleLogin() {
    const u = document.getElementById('loginUser').value;
    const p = document.getElementById('loginPass').value;
    
    // Inga backend auth connect pannanum. Ippodhikku Admin demo:
    if(u === "admin" && p === "admin123") {
        document.getElementById('authBox').style.display = 'none';
        document.getElementById('homePage').style.display = 'flex';
        showToast("Login Success!");
    } else {
        showToast("Invalid Credentials");
    }
}

// BANKING Logic
async function getAccountDetails() {
    const id = document.getElementById('vId').value;
    const res = await fetch(`${baseUrl}/${id}`);
    if(res.ok) {
        const data = await res.json();
        document.getElementById('res').style.display = 'block';
        document.getElementById('res').innerHTML = `Name: ${data.accountHolderName} <br> Balance: ₹${data.balance}`;
    }
}

async function apiCall(type, idField, amtField) {
    const id = document.getElementById(idField).value;
    const amt = document.getElementById(amtField).value;
    const res = await fetch(`${baseUrl}/${id}/${type}`, {
        method: 'PUT',
        headers: {'Content-Type': 'application/json'},
        body: JSON.stringify({ amount: parseFloat(amt) })
    });
    if(res.ok) showToast(`${type} Success!`);
}

async function makeTransfer() {
    const fId = document.getElementById('fromId').value;
    const tId = document.getElementById('toId').value;
    const amt = document.getElementById('tAmt').value;
    const res = await fetch(`${baseUrl}/transfer`, {
        method: 'POST',
        headers: {'Content-Type': 'application/json'},
        body: JSON.stringify({ from_account_id: fId, to_account_id: tId, amount: amt })
    });
    if(res.ok) showToast("Transfer Success!");
}