const baseUrl = "http://localhost:8080/api/accounts";

// Login Handle
function handleLogin() {
    const u = document.getElementById('loginUser').value;
    const p = document.getElementById('loginPass').value;
    if(u === "admin" && p === "admin123") {
        document.getElementById('authBox').style.display = "none";
        document.getElementById('homePage').style.display = "block";
    } else { alert("Wrong Credentials"); }
}

// Section Switching Logic (Indha code dhaan neenga keta "New Page" effect-ai tharum)
function showSection(sectionId) {
    const sections = ['homeContent', 'depositSection', 'transferSection', 'viewBalanceSection', 'withdrawSection'];
    sections.forEach(s => {
        const el = document.getElementById(s);
        if(el) el.style.display = "none";
    });
    document.getElementById(sectionId).style.display = "block";
}

// Balance Fetch
async function getAccountDetails() {
    const id = document.getElementById('vId').value;
    const res = await fetch(`${baseUrl}/${id}`);
    if(id && res.ok) {
        const data = await res.json();
        document.getElementById('res').innerText = `Name: ${data.accountHolderName} | Balance: ₹${data.balance}`;
    } else { alert("Error!"); }
}

// Transaction API
async function apiCall(type, idF, amtF) {
    const id = document.getElementById(idF).value;
    const amt = document.getElementById(amtF).value;
    const r = await fetch(`${baseUrl}/${id}/${type}?amount=${amt}`, { method: 'PUT' });
    if(r.ok) { alert(`${type.toUpperCase()} Success! Email Sent.`); showSection('homeContent'); }
    else { alert("Failed"); }
}

// Transfer API
async function makeTransfer() {
    const fId = document.getElementById('fromId').value;
    const tId = document.getElementById('toId').value;
    const amt = document.getElementById('tAmt').value;
    const r = await fetch(`${baseUrl}/transfer?fromId=${fId}&toId=${tId}&amount=${amt}`, { method: 'PUT' });
    if(r.ok) { alert("Transfer Done!"); showSection('homeContent'); }
    else { alert("Failed"); }
}