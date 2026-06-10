const API_BASE = '';
let token = localStorage.getItem('token');
let allAlerts = [];
let allLogs = [];

// DOM Elements
const loginPage = document.getElementById('login-page');
const registerPage = document.getElementById('register-page');
const dashboardPage = document.getElementById('dashboard-page');
const loginForm = document.getElementById('login-form');
const registerForm = document.getElementById('register-form');
const registerBtn = document.getElementById('register-btn');
const backToLoginBtn = document.getElementById('back-to-login');
const logoutBtn = document.getElementById('logout-btn');
const loginError = document.getElementById('login-error');
const registerError = document.getElementById('register-error');

// Navigation
registerBtn.addEventListener('click', () => {
    loginPage.classList.add('hidden');
    registerPage.classList.remove('hidden');
});

backToLoginBtn.addEventListener('click', () => {
    registerPage.classList.add('hidden');
    loginPage.classList.remove('hidden');
});

// Login
loginForm.addEventListener('submit', async (e) => {
    e.preventDefault();
    const username = document.getElementById('username').value;
    const password = document.getElementById('password').value;
    
    try {
        const response = await fetch(`${API_BASE}/api/auth/login`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ username, password })
        });
        
        const data = await response.json();
        
        if (response.ok) {
            token = data.token;
            localStorage.setItem('token', token);
            localStorage.setItem('userRole', data.role);
            showDashboard();
        } else {
            loginError.textContent = data;
            loginError.classList.remove('hidden');
        }
    } catch (error) {
        loginError.textContent = 'Connection error. Please try again.';
        loginError.classList.remove('hidden');
    }
});

// Register
registerForm.addEventListener('submit', async (e) => {
    e.preventDefault();
    const username = document.getElementById('reg-username').value;
    const password = document.getElementById('reg-password').value;
    const role = document.getElementById('reg-role').value;
    
    try {
        const response = await fetch(`${API_BASE}/api/auth/register`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ username, password, role })
        });
        
        const data = await response.json();
        
        if (response.ok) {
            token = data.token;
            localStorage.setItem('token', token);
            localStorage.setItem('userRole', data.role);
            showDashboard();
        } else {
            registerError.textContent = data;
            registerError.classList.remove('hidden');
        }
    } catch (error) {
        registerError.textContent = 'Connection error. Please try again.';
        registerError.classList.remove('hidden');
    }
});

// Logout
logoutBtn.addEventListener('click', () => {
    localStorage.removeItem('token');
    localStorage.removeItem('userRole');
    token = null;
    dashboardPage.classList.add('hidden');
    loginPage.classList.remove('hidden');
});

// Show Dashboard
function showDashboard() {
    loginPage.classList.add('hidden');
    registerPage.classList.add('hidden');
    dashboardPage.classList.remove('hidden');
    loadDashboardData();
    
    // Show admin tab if user is admin
    const userRole = localStorage.getItem('userRole');
    if (userRole === 'ROLE_ADMIN') {
        document.getElementById('admin-tab-btn').classList.remove('hidden');
    }
}

// Load Dashboard Data
async function loadDashboardData() {
    try {
        const [statsResponse, alertsResponse, logsResponse] = await Promise.all([
            fetch(`${API_BASE}/api/analyst/dashboard-stats`, {
                headers: { 'Authorization': `Bearer ${token}` }
            }),
            fetch(`${API_BASE}/api/analyst/alerts`, {
                headers: { 'Authorization': `Bearer ${token}` }
            }),
            fetch(`${API_BASE}/api/analyst/traffic-logs`, {
                headers: { 'Authorization': `Bearer ${token}` }
            })
        ]);
        
        if (statsResponse.ok) {
            const stats = await statsResponse.json();
            document.getElementById('total-threats').textContent = stats.totalAlerts;
            document.getElementById('recent-alerts').textContent = Math.min(stats.totalAlerts, 10);
            document.getElementById('suspicious-ips').textContent = new Set(stats.recentAlerts?.map(a => a.ipAddress)).size || 0;
            document.getElementById('attack-count').textContent = stats.totalAlerts;
        }
        
        if (alertsResponse.ok) {
            allAlerts = await alertsResponse.json();
            renderAlerts(allAlerts);
        }
        
        if (logsResponse.ok) {
            allLogs = await logsResponse.json();
            renderLogs(allLogs);
        }
    } catch (error) {
        console.error('Error loading dashboard data:', error);
    }
}

// Render Alerts
function renderAlerts(alerts) {
    const tbody = document.getElementById('alerts-table');
    tbody.innerHTML = alerts.map(alert => `
        <tr class="border-b border-green-900">
            <td class="px-4 py-3">${alert.ipAddress}</td>
            <td class="px-4 py-3">${alert.attackType}</td>
            <td class="px-4 py-3">
                <span class="px-2 py-1 rounded ${
                    alert.severity === 'HIGH' ? 'bg-red-900 text-red-300' :
                    alert.severity === 'MEDIUM' ? 'bg-yellow-900 text-yellow-300' :
                    'bg-green-900 text-green-300'
                }">${alert.severity}</span>
            </td>
            <td class="px-4 py-3">${alert.threatScore}</td>
            <td class="px-4 py-3">${new Date(alert.timestamp).toLocaleString()}</td>
        </tr>
    `).join('');
}

// Render Logs
function renderLogs(logs) {
    const tbody = document.getElementById('logs-table');
    tbody.innerHTML = logs.map(log => `
        <tr class="border-b border-green-900">
            <td class="px-4 py-3">${log.sourceIp}</td>
            <td class="px-4 py-3">${log.destinationIp}</td>
            <td class="px-4 py-3">${log.requestType}</td>
            <td class="px-4 py-3">${log.status}</td>
            <td class="px-4 py-3">${new Date(log.timestamp).toLocaleString()}</td>
        </tr>
    `).join('');
}

// Initialize Charts
let attackTrendsChart, topIpsChart, attackCategoriesChart, severityChart;

function initializeCharts(alerts) {
    // Attack Trends Chart
    const trendsCtx = document.getElementById('attackTrendsChart').getContext('2d');
    const attackData = groupAttacksByDate(alerts);
    
    if (attackTrendsChart) attackTrendsChart.destroy();
    attackTrendsChart = new Chart(trendsCtx, {
        type: 'line',
        data: {
            labels: Object.keys(attackData),
            datasets: [{
                label: 'Attacks',
                data: Object.values(attackData),
                borderColor: '#00ff88',
                backgroundColor: 'rgba(0, 255, 136, 0.1)',
                fill: true,
                tension: 0.4
            }]
        },
        options: {
            responsive: true,
            plugins: {
                legend: { labels: { color: '#00ff88' } }
            },
            scales: {
                x: { ticks: { color: '#00ff88' }, grid: { color: 'rgba(0, 255, 136, 0.1)' } },
                y: { ticks: { color: '#00ff88' }, grid: { color: 'rgba(0, 255, 136, 0.1)' } }
            }
        }
    });
    
    // Top IPs Chart
    const ipsCtx = document.getElementById('topIpsChart').getContext('2d');
    const ipData = getTopIPs(alerts, 5);
    
    if (topIpsChart) topIpsChart.destroy();
    topIpsChart = new Chart(ipsCtx, {
        type: 'bar',
        data: {
            labels: ipData.map(d => d.ip),
            datasets: [{
                label: 'Attack Count',
                data: ipData.map(d => d.count),
                backgroundColor: ['#00ff88', '#00cc6a', '#00994d', '#006633', '#003319']
            }]
        },
        options: {
            responsive: true,
            plugins: {
                legend: { labels: { color: '#00ff88' } }
            },
            scales: {
                x: { ticks: { color: '#00ff88' }, grid: { color: 'rgba(0, 255, 136, 0.1)' } },
                y: { ticks: { color: '#00ff88' }, grid: { color: 'rgba(0, 255, 136, 0.1)' } }
            }
        }
    });
    
    // Attack Categories Chart
    const categoriesCtx = document.getElementById('attackCategoriesChart').getContext('2d');
    const categoryData = groupByAttackType(alerts);
    
    if (attackCategoriesChart) attackCategoriesChart.destroy();
    attackCategoriesChart = new Chart(categoriesCtx, {
        type: 'doughnut',
        data: {
            labels: Object.keys(categoryData),
            datasets: [{
                data: Object.values(categoryData),
                backgroundColor: ['#00ff88', '#ff0044', '#00ccff', '#ffff00', '#ff00ff']
            }]
        },
        options: {
            responsive: true,
            plugins: {
                legend: { labels: { color: '#00ff88' } }
            }
        }
    });
    
    // Severity Distribution Chart
    const severityCtx = document.getElementById('severityChart').getContext('2d');
    const severityData = groupBySeverity(alerts);
    
    if (severityChart) severityChart.destroy();
    severityChart = new Chart(severityCtx, {
        type: 'pie',
        data: {
            labels: Object.keys(severityData),
            datasets: [{
                data: Object.values(severityData),
                backgroundColor: ['#ff0044', '#ffff00', '#00ff88']
            }]
        },
        options: {
            responsive: true,
            plugins: {
                legend: { labels: { color: '#00ff88' } }
            }
        }
    });
}

function groupAttacksByDate(alerts) {
    const grouped = {};
    alerts.forEach(alert => {
        const date = new Date(alert.timestamp).toLocaleDateString();
        grouped[date] = (grouped[date] || 0) + 1;
    });
    return grouped;
}

function getTopIPs(alerts, limit) {
    const ipCounts = {};
    alerts.forEach(alert => {
        ipCounts[alert.ipAddress] = (ipCounts[alert.ipAddress] || 0) + 1;
    });
    return Object.entries(ipCounts)
        .map(([ip, count]) => ({ ip, count }))
        .sort((a, b) => b.count - a.count)
        .slice(0, limit);
}

function groupByAttackType(alerts) {
    const grouped = {};
    alerts.forEach(alert => {
        grouped[alert.attackType] = (grouped[alert.attackType] || 0) + 1;
    });
    return grouped;
}

function groupBySeverity(alerts) {
    const grouped = {};
    alerts.forEach(alert => {
        grouped[alert.severity] = (grouped[alert.severity] || 0) + 1;
    });
    return grouped;
}

// Tab Navigation
document.querySelectorAll('.tab-btn').forEach(btn => {
    btn.addEventListener('click', async () => {
        document.querySelectorAll('.tab-btn').forEach(b => b.classList.remove('active'));
        btn.classList.add('active');
        
        document.querySelectorAll('.tab-content').forEach(content => {
            content.classList.add('hidden');
        });
        
        const tab = btn.dataset.tab;
        document.getElementById(`${tab}-tab`).classList.remove('hidden');
        
        // Load analytics data when analytics tab is opened
        if (tab === 'analytics') {
            const alertsResponse = await fetch(`${API_BASE}/api/analyst/alerts`, {
                headers: { 'Authorization': `Bearer ${token}` }
            });
            if (alertsResponse.ok) {
                const alerts = await alertsResponse.json();
                initializeCharts(alerts);
            }
        }
        
        // Load admin data when admin tab is opened
        if (tab === 'admin') {
            loadAdminData();
        }
    });
});

// Check if already logged in
if (token) {
    showDashboard();
}

// Search and Filter
document.getElementById('alert-search').addEventListener('input', filterAlerts);
document.getElementById('alert-severity-filter').addEventListener('change', filterAlerts);
document.getElementById('log-search').addEventListener('input', filterLogs);

function filterAlerts() {
    const searchTerm = document.getElementById('alert-search').value.toLowerCase();
    const severityFilter = document.getElementById('alert-severity-filter').value;
    
    const filtered = allAlerts.filter(alert => {
        const matchesSearch = alert.ipAddress.toLowerCase().includes(searchTerm) ||
                            alert.attackType.toLowerCase().includes(searchTerm);
        const matchesSeverity = !severityFilter || alert.severity === severityFilter;
        return matchesSearch && matchesSeverity;
    });
    
    renderAlerts(filtered);
}

function filterLogs() {
    const searchTerm = document.getElementById('log-search').value.toLowerCase();
    
    const filtered = allLogs.filter(log => {
        return log.sourceIp.toLowerCase().includes(searchTerm) ||
               log.destinationIp.toLowerCase().includes(searchTerm) ||
               log.requestType.toLowerCase().includes(searchTerm);
    });
    
    renderLogs(filtered);
}

// CSV Export
document.getElementById('export-alerts-csv').addEventListener('click', () => exportToCSV(allAlerts, 'alerts'));
document.getElementById('export-logs-csv').addEventListener('click', () => exportToCSV(allLogs, 'traffic-logs'));

function exportToCSV(data, filename) {
    if (!data || data.length === 0) {
        alert('No data to export');
        return;
    }
    
    const headers = Object.keys(data[0]);
    const csvContent = [
        headers.join(','),
        ...data.map(row => headers.map(header => {
            const value = row[header];
            return typeof value === 'string' ? `"${value}"` : value;
        }).join(','))
    ].join('\n');
    
    const blob = new Blob([csvContent], { type: 'text/csv' });
    const url = window.URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `${filename}-${new Date().toISOString().split('T')[0]}.csv`;
    a.click();
    window.URL.revokeObjectURL(url);
}

// Admin Functions
async function loadAdminData() {
    try {
        const usersResponse = await fetch(`${API_BASE}/api/admin/users`, {
            headers: { 'Authorization': `Bearer ${token}` }
        });
        if (usersResponse.ok) {
            const users = await usersResponse.json();
            renderUsersTable(users);
        }
        
        const configResponse = await fetch(`${API_BASE}/api/admin/config`, {
            headers: { 'Authorization': `Bearer ${token}` }
        });
        if (configResponse.ok) {
            const config = await configResponse.json();
            document.getElementById('brute-force-threshold').value = config.bruteForceThreshold;
            document.getElementById('dos-threshold').value = config.dosThreshold;
        }
        
        loadBlockedIps();
    } catch (error) {
        console.error('Error loading admin data:', error);
    }
}

function renderUsersTable(users) {
    const tbody = document.getElementById('users-table');
    tbody.innerHTML = users.map(user => `
        <tr class="border-b border-green-900">
            <td class="px-4 py-3">${user.id}</td>
            <td class="px-4 py-3">${user.username}</td>
            <td class="px-4 py-3">${user.role}</td>
            <td class="px-4 py-3">
                <button onclick="deleteUser(${user.id})" class="cyber-button px-3 py-1 rounded text-sm">Delete</button>
            </td>
        </tr>
    `).join('');
}

async function deleteUser(userId) {
    if (!confirm('Are you sure you want to delete this user?')) return;
    
    try {
        const response = await fetch(`${API_BASE}/api/admin/users/${userId}`, {
            method: 'DELETE',
            headers: { 'Authorization': `Bearer ${token}` }
        });
        if (response.ok) {
            loadAdminData();
        } else {
            alert('Failed to delete user');
        }
    } catch (error) {
        alert('Error deleting user');
    }
}

async function loadBlockedIps() {
    try {
        const response = await fetch(`${API_BASE}/api/admin/blocked-ips`, {
            headers: { 'Authorization': `Bearer ${token}` }
        });
        if (response.ok) {
            const blockedIps = await response.json();
            const listDiv = document.getElementById('blocked-ips-list');
            if (blockedIps.length === 0) {
                listDiv.innerHTML = '<p class="text-gray-400">No IPs blocked</p>';
            } else {
                listDiv.innerHTML = blockedIps.map(ip => `
                    <div class="flex justify-between items-center mb-2">
                        <span>${ip}</span>
                        <button onclick="unblockIp('${ip}')" class="cyber-button px-3 py-1 rounded text-sm">Unblock</button>
                    </div>
                `).join('');
            }
        }
    } catch (error) {
        console.error('Error loading blocked IPs:', error);
    }
}

async function unblockIp(ip) {
    try {
        const response = await fetch(`${API_BASE}/api/admin/unblock-ip`, {
            method: 'POST',
            headers: { 
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ ip })
        });
        if (response.ok) {
            alert('IP unblocked successfully');
            loadBlockedIps();
        } else {
            alert('Failed to unblock IP');
        }
    } catch (error) {
        alert('Error unblocking IP');
    }
}

// Save configuration
document.getElementById('save-config').addEventListener('click', async () => {
    const config = {
        bruteForceThreshold: parseInt(document.getElementById('brute-force-threshold').value),
        dosThreshold: parseInt(document.getElementById('dos-threshold').value)
    };
    
    try {
        const response = await fetch(`${API_BASE}/api/admin/config`, {
            method: 'POST',
            headers: { 
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(config)
        });
        if (response.ok) {
            alert('Configuration saved successfully');
        } else {
            alert('Failed to save configuration');
        }
    } catch (error) {
        alert('Error saving configuration');
    }
});

// Block IP
document.getElementById('block-ip-btn').addEventListener('click', async () => {
    const ip = document.getElementById('block-ip-input').value;
    if (!ip) {
        alert('Please enter an IP address');
        return;
    }
    
    try {
        const response = await fetch(`${API_BASE}/api/admin/block-ip`, {
            method: 'POST',
            headers: { 
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ ip })
        });
        if (response.ok) {
            alert('IP blocked successfully');
            document.getElementById('block-ip-input').value = '';
            loadBlockedIps();
        } else {
            alert('Failed to block IP');
        }
    } catch (error) {
        alert('Error blocking IP');
    }
});
