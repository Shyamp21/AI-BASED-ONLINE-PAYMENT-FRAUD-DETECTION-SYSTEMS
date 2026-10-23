/**
 * Main Application Logic for FraudGuard AI Dashboard
 */

document.addEventListener("DOMContentLoaded", () => {
  initNavigation();
  initAuthUI();
  initSinglePredictForm();
  initBatchUpload();
  initHistoryFilters();
  initAlertsUI();
  initAdminUI();

  // Load initial view
  if (ApiClient.isAuthenticated()) {
    updateUserBadge();
    loadDashboardData();
  } else {
    // Show login modal or load default guest view
    openAuthModal("login");
  }

  window.addEventListener("auth:required", () => {
    showToast("Authentication required. Please sign in.", "warning");
    openAuthModal("login");
  });
});

/* =========================================================================
   1. NAVIGATION & TABS
   ========================================================================= */
function initNavigation() {
  const navLinks = document.querySelectorAll(".nav-link[data-tab]");
  navLinks.forEach(link => {
    link.addEventListener("click", (e) => {
      e.preventDefault();
      const tabId = link.getAttribute("data-tab");
      switchTab(tabId);
    });
  });
}

function switchTab(tabId) {
  document.querySelectorAll(".nav-link").forEach(l => l.classList.remove("active"));
  document.querySelectorAll(".tab-pane").forEach(p => p.classList.remove("active"));

  const targetLink = document.querySelector(`.nav-link[data-tab="${tabId}"]`);
  const targetPane = document.getElementById(tabId);

  if (targetLink) targetLink.classList.add("active");
  if (targetPane) targetPane.classList.add("active");

  // Load view-specific dynamic data
  if (tabId === "dashboard") loadDashboardData();
  if (tabId === "history") loadTransactionHistory();
  if (tabId === "alerts") loadFraudAlerts();
  if (tabId === "admin") loadAdminData();
}

/* =========================================================================
   2. AUTHENTICATION & PROFILE
   ========================================================================= */
function initAuthUI() {
  const loginForm = document.getElementById("loginForm");
  const registerForm = document.getElementById("registerForm");
  const logoutBtn = document.getElementById("logoutBtn");
  const authModal = document.getElementById("authModal");

  if (loginForm) {
    loginForm.addEventListener("submit", async (e) => {
      e.preventDefault();
      const u = document.getElementById("loginUsername").value.trim();
      const p = document.getElementById("loginPassword").value;
      try {
        await ApiClient.login(u, p);
        showToast("Logged in successfully!", "success");
        closeAuthModal();
        updateUserBadge();
        loadDashboardData();
      } catch (err) {
        showToast(err.message, "danger");
      }
    });
  }

  if (registerForm) {
    registerForm.addEventListener("submit", async (e) => {
      e.preventDefault();
      const u = document.getElementById("regUsername").value.trim();
      const em = document.getElementById("regEmail").value.trim();
      const p = document.getElementById("regPassword").value;
      const fn = document.getElementById("regFullName").value.trim();
      const r = document.getElementById("regRole").value;
      try {
        await ApiClient.register(u, em, p, fn, r);
        showToast("Account created successfully!", "success");
        closeAuthModal();
        updateUserBadge();
        loadDashboardData();
      } catch (err) {
        showToast(err.message, "danger");
      }
    });
  }

  if (logoutBtn) {
    logoutBtn.addEventListener("click", () => {
      ApiClient.clearAuth();
      showToast("You have been signed out.", "info");
      updateUserBadge();
      openAuthModal("login");
    });
  }

  // Pre-fill demo credentials shortcuts
  document.getElementById("btnDemoAdmin")?.addEventListener("click", () => {
    document.getElementById("loginUsername").value = "admin";
    document.getElementById("loginPassword").value = "Admin@123";
  });
  document.getElementById("btnDemoAnalyst")?.addEventListener("click", () => {
    document.getElementById("loginUsername").value = "analyst";
    document.getElementById("loginPassword").value = "Analyst@123";
  });
}

function openAuthModal(mode = "login") {
  const modal = document.getElementById("authModal");
  if (!modal) return;
  modal.style.display = "flex";
  toggleAuthTab(mode);
}

function closeAuthModal() {
  const modal = document.getElementById("authModal");
  if (modal) modal.style.display = "none";
}

function toggleAuthTab(mode) {
  const loginSection = document.getElementById("loginSection");
  const regSection = document.getElementById("registerSection");
  if (mode === "login") {
    loginSection.style.display = "block";
    regSection.style.display = "none";
  } else {
    loginSection.style.display = "none";
    regSection.style.display = "block";
  }
}

function updateUserBadge() {
  const user = ApiClient.getUser();
  const nameEl = document.getElementById("topUserName");
  const roleEl = document.getElementById("topUserRole");
  const avatarEl = document.getElementById("topUserAvatar");
  const adminNav = document.getElementById("navAdminItem");

  if (user) {
    if (nameEl) nameEl.textContent = user.fullName || user.username;
    if (roleEl) roleEl.textContent = user.role.replace("ROLE_", "");
    if (avatarEl) avatarEl.textContent = (user.username || "U")[0].toUpperCase();
    if (adminNav) adminNav.style.display = user.role === "ROLE_ADMIN" ? "block" : "none";
  } else {
    if (nameEl) nameEl.textContent = "Guest";
    if (roleEl) roleEl.textContent = "ANONYMOUS";
    if (avatarEl) avatarEl.textContent = "?";
    if (adminNav) adminNav.style.display = "none";
  }
}

/* =========================================================================
   3. DASHBOARD METRICS
   ========================================================================= */
async function loadDashboardData() {
  try {
    const summary = await ApiClient.getSummary();
    const stats = await ApiClient.getStatistics();

    // Update KPI counters
    document.getElementById("kpiTotalTxns").textContent = Number(summary.totalTransactions).toLocaleString();
    document.getElementById("kpiFraudTxns").textContent = Number(summary.totalFraudDetected).toLocaleString();
    document.getElementById("kpiLegitTxns").textContent = Number(summary.totalLegitimate).toLocaleString();
    document.getElementById("kpiFraudPct").textContent = `${summary.fraudPercentage}%`;
    document.getElementById("kpiPendingAlerts").textContent = summary.pendingAlerts;
    document.getElementById("navAlertsBadge").textContent = summary.pendingAlerts;

    // Initialize Chart.js
    DashboardCharts.init(stats);

    // Populate recent activity table
    renderRecentTable(stats.recentActivity || []);
  } catch (err) {
    console.error("Dashboard load failed:", err);
  }
}

function renderRecentTable(activities) {
  const tbody = document.getElementById("recentActivityBody");
  if (!tbody) return;
  tbody.innerHTML = "";

  if (activities.length === 0) {
    tbody.innerHTML = `<tr><td colspan="6" style="text-align:center; padding: 20px; color:#94a3b8;">No recent transactions recorded</td></tr>`;
    return;
  }

  activities.forEach(t => {
    const isFraud = t.predictedClass === "FRAUD";
    const badgeClass = isFraud ? "badge-fraud" : "badge-legit";
    const riskBadgeClass = `badge-${(t.riskLevel || "LOW").toLowerCase()}`;

    const tr = document.createElement("tr");
    tr.innerHTML = `
      <td><strong>${t.externalTransactionId}</strong></td>
      <td><span class="badge" style="background:#e2e8f0; color:#334155;">${t.type}</span></td>
      <td>$${Number(t.amount).toLocaleString(undefined, {minimumFractionDigits: 2})}</td>
      <td><span class="badge ${badgeClass}">${t.predictedClass || "ANALYZING"}</span></td>
      <td><span class="badge ${riskBadgeClass}">${t.riskLevel || "LOW"} (${((t.fraudProbability || 0) * 100).toFixed(1)}%)</span></td>
      <td>${new Date(t.timestamp).toLocaleTimeString([], {hour: '2-digit', minute:'2-digit'})}</td>
    `;
    tbody.appendChild(tr);
  });
}

/* =========================================================================
   4. SINGLE TRANSACTION FRAUD PREDICTION FORM
   ========================================================================= */
function initSinglePredictForm() {
  const form = document.getElementById("predictForm");
  const resultBox = document.getElementById("singlePredictionResult");

  // Sample data fill buttons
  document.getElementById("btnFillFraudSample")?.addEventListener("click", () => {
    document.getElementById("inpType").value = "TRANSFER";
    document.getElementById("inpAmount").value = "125000.00";
    document.getElementById("inpStep").value = "16";
    document.getElementById("inpSenderId").value = "C9872103456";
    document.getElementById("inpOldOrig").value = "125000.00";
    document.getElementById("inpNewOrig").value = "0.00";
    document.getElementById("inpReceiverId").value = "C5544332211";
    document.getElementById("inpOldDest").value = "0.00";
    document.getElementById("inpNewDest").value = "0.00";
  });

  document.getElementById("btnFillLegitSample")?.addEventListener("click", () => {
    document.getElementById("inpType").value = "PAYMENT";
    document.getElementById("inpAmount").value = "142.50";
    document.getElementById("inpStep").value = "22";
    document.getElementById("inpSenderId").value = "C1122334455";
    document.getElementById("inpOldOrig").value = "2500.00";
    document.getElementById("inpNewOrig").value = "2357.50";
    document.getElementById("inpReceiverId").value = "M9988776655";
    document.getElementById("inpOldDest").value = "0.00";
    document.getElementById("inpNewDest").value = "0.00";
  });

  if (form) {
    form.addEventListener("submit", async (e) => {
      e.preventDefault();
      const submitBtn = form.querySelector("button[type='submit']");
      submitBtn.disabled = true;
      submitBtn.textContent = "Analyzing Transaction...";

      const payload = {
        externalTransactionId: document.getElementById("inpTxnId").value.trim() || undefined,
        type: document.getElementById("inpType").value,
        amount: parseFloat(document.getElementById("inpAmount").value),
        step: parseInt(document.getElementById("inpStep").value) || 1,
        senderId: document.getElementById("inpSenderId").value.trim(),
        oldbalanceOrg: parseFloat(document.getElementById("inpOldOrig").value),
        newbalanceOrig: parseFloat(document.getElementById("inpNewOrig").value),
        receiverId: document.getElementById("inpReceiverId").value.trim(),
        oldbalanceDest: parseFloat(document.getElementById("inpOldDest").value),
        newbalanceDest: parseFloat(document.getElementById("inpNewDest").value)
      };

      try {
        const res = await ApiClient.predictTransaction(payload);
        renderPredictionResult(res);
        showToast("Analysis complete!", "success");
      } catch (err) {
        showToast(err.message, "danger");
      } finally {
        submitBtn.disabled = false;
        submitBtn.textContent = "Detect Fraud";
      }
    });
  }
}

function renderPredictionResult(res) {
  const box = document.getElementById("singlePredictionResult");
  if (!box) return;

  const isFraud = res.predictedClass === "FRAUD";
  box.className = `result-box ${isFraud ? "fraud" : "legitimate"}`;
  box.style.display = "block";

  document.getElementById("resTxnId").textContent = res.externalTransactionId;
  const classBadge = document.getElementById("resClassBadge");
  classBadge.className = `badge ${isFraud ? "badge-fraud" : "badge-legit"}`;
  classBadge.textContent = res.predictedClass;

  const probPct = ((res.fraudProbability || 0) * 100).toFixed(1);
  document.getElementById("resRiskScore").textContent = `${probPct}%`;

  const meterFill = document.getElementById("resMeterFill");
  meterFill.style.width = `${probPct}%`;
  meterFill.style.backgroundColor = isFraud ? "var(--danger-red)" : "var(--success-green)";

  const riskBadge = document.getElementById("resRiskLevel");
  riskBadge.className = `badge badge-${res.riskLevel.toLowerCase()}`;
  riskBadge.textContent = res.riskLevel;

  document.getElementById("resCbProb").textContent = ((res.catboostProbability || 0) * 100).toFixed(1) + "%";
  document.getElementById("resXgbProb").textContent = ((res.xgboostProbability || 0) * 100).toFixed(1) + "%";
  document.getElementById("resEnsembleScore").textContent = ((res.ensembleScore || 0) * 100).toFixed(1) + "%";
  document.getElementById("resExplanation").textContent = res.explanationSummary;
  document.getElementById("resDisclaimer").textContent = res.disclaimer;

  box.scrollIntoView({ behavior: "smooth" });
}

/* =========================================================================
   5. BATCH CSV UPLOAD
   ========================================================================= */
let batchResultsData = [];

function initBatchUpload() {
  const dropZone = document.getElementById("dropZone");
  const fileInput = document.getElementById("csvFileInput");
  const uploadBtn = document.getElementById("btnUploadBatch");
  const downloadBtn = document.getElementById("btnDownloadBatchCsv");

  if (dropZone && fileInput) {
    dropZone.addEventListener("click", () => fileInput.click());

    dropZone.addEventListener("dragover", (e) => {
      e.preventDefault();
      dropZone.style.borderColor = "var(--accent-blue)";
    });

    dropZone.addEventListener("dragleave", () => {
      dropZone.style.borderColor = "var(--border-color)";
    });

    dropZone.addEventListener("drop", (e) => {
      e.preventDefault();
      dropZone.style.borderColor = "var(--border-color)";
      if (e.dataTransfer.files.length) {
        fileInput.files = e.dataTransfer.files;
        document.getElementById("selectedFileName").textContent = e.dataTransfer.files[0].name;
      }
    });

    fileInput.addEventListener("change", () => {
      if (fileInput.files.length) {
        document.getElementById("selectedFileName").textContent = fileInput.files[0].name;
      }
    });
  }

  if (uploadBtn) {
    uploadBtn.addEventListener("click", async () => {
      if (!fileInput.files.length) {
        showToast("Please choose a CSV file first", "warning");
        return;
      }

      uploadBtn.disabled = true;
      uploadBtn.textContent = "Processing CSV Batches...";

      try {
        const res = await ApiClient.batchPredictCsv(fileInput.files[0]);
        batchResultsData = res.results || [];
        renderBatchSummary(res);
        showToast(`Processed ${res.totalProcessed} transactions!`, "success");
      } catch (err) {
        showToast(err.message, "danger");
      } finally {
        uploadBtn.disabled = false;
        uploadBtn.textContent = "Upload & Predict Batch";
      }
    });
  }

  if (downloadBtn) {
    downloadBtn.addEventListener("click", () => {
      if (!batchResultsData.length) {
        showToast("No batch results available to download", "warning");
        return;
      }
      exportBatchToCsv(batchResultsData);
    });
  }
}

function renderBatchSummary(res) {
  const container = document.getElementById("batchResultsContainer");
  if (!container) return;
  container.style.display = "block";

  document.getElementById("batchTotalCount").textContent = res.totalProcessed;
  document.getElementById("batchFraudCount").textContent = res.fraudDetected;
  document.getElementById("batchLegitCount").textContent = res.legitimateDetected;
  document.getElementById("batchFraudPct").textContent = `${res.fraudPercentage.toFixed(1)}%`;

  const tbody = document.getElementById("batchResultsBody");
  tbody.innerHTML = "";

  res.results.slice(0, 50).forEach(r => {
    const isFraud = r.predictedClass === "FRAUD";
    const tr = document.createElement("tr");
    tr.innerHTML = `
      <td><strong>${r.externalTransactionId}</strong></td>
      <td><span class="badge ${isFraud ? 'badge-fraud' : 'badge-legit'}">${r.predictedClass}</span></td>
      <td><span class="badge badge-${r.riskLevel.toLowerCase()}">${r.riskLevel}</span></td>
      <td>${((r.fraudProbability || 0) * 100).toFixed(1)}%</td>
      <td>${((r.ensembleScore || 0) * 100).toFixed(1)}%</td>
      <td style="font-size:0.78rem;">${r.explanationSummary}</td>
    `;
    tbody.appendChild(tr);
  });
}

function exportBatchToCsv(results) {
  const headers = ["Transaction ID", "Predicted Class", "Risk Level", "Fraud Probability", "Ensemble Score", "Explanation"];
  const rows = results.map(r => [
    r.externalTransactionId,
    r.predictedClass,
    r.riskLevel,
    r.fraudProbability,
    r.ensembleScore,
    `"${(r.explanationSummary || '').replace(/"/g, '""')}"`
  ]);

  const csvContent = [headers.join(","), ...rows.map(e => e.join(","))].join("\n");
  const blob = new Blob([csvContent], { type: "text/csv;charset=utf-8;" });
  const url = URL.createObjectURL(blob);
  const a = document.createElement("a");
  a.href = url;
  a.download = `fraud_prediction_results_${Date.now()}.csv`;
  a.click();
}

/* =========================================================================
   6. TRANSACTION HISTORY & SEARCH
   ========================================================================= */
let historyPage = 0;

function initHistoryFilters() {
  document.getElementById("btnSearchHistory")?.addEventListener("click", () => {
    historyPage = 0;
    loadTransactionHistory();
  });

  document.getElementById("historyFilterType")?.addEventListener("change", () => {
    historyPage = 0;
    loadTransactionHistory();
  });

  document.getElementById("btnPrevPage")?.addEventListener("click", () => {
    if (historyPage > 0) {
      historyPage--;
      loadTransactionHistory();
    }
  });

  document.getElementById("btnNextPage")?.addEventListener("click", () => {
    historyPage++;
    loadTransactionHistory();
  });
}

async function loadTransactionHistory() {
  const query = document.getElementById("historySearchInput")?.value.trim();
  const type = document.getElementById("historyFilterType")?.value;

  const params = {
    page: historyPage,
    size: 15
  };
  if (query) params.query = query;
  if (type) params.type = type;

  try {
    const data = await ApiClient.getTransactions(params);
    renderHistoryTable(data.content || []);
    document.getElementById("historyPageInfo").textContent = `Page ${data.number + 1} of ${data.totalPages || 1}`;
    document.getElementById("btnPrevPage").disabled = data.first;
    document.getElementById("btnNextPage").disabled = data.last;
  } catch (err) {
    console.error("History load failed:", err);
  }
}

function renderHistoryTable(transactions) {
  const tbody = document.getElementById("historyTableBody");
  if (!tbody) return;
  tbody.innerHTML = "";

  if (transactions.length === 0) {
    tbody.innerHTML = `<tr><td colspan="7" style="text-align:center; padding: 20px; color:#94a3b8;">No matching transactions found</td></tr>`;
    return;
  }

  transactions.forEach(t => {
    const isFraud = t.predictedClass === "FRAUD";
    const tr = document.createElement("tr");
    tr.innerHTML = `
      <td><strong>${t.externalTransactionId}</strong></td>
      <td><span class="badge" style="background:#e2e8f0; color:#334155;">${t.type}</span></td>
      <td>$${Number(t.amount).toLocaleString(undefined, {minimumFractionDigits: 2})}</td>
      <td>${t.senderId} &rarr; ${t.receiverId}</td>
      <td><span class="badge ${isFraud ? 'badge-fraud' : 'badge-legit'}">${t.predictedClass || "PENDING"}</span></td>
      <td><span class="badge badge-${(t.riskLevel || 'low').toLowerCase()}">${t.riskLevel || 'LOW'}</span></td>
      <td>
        <button class="btn btn-outline btn-sm" onclick="viewTransactionModal(${t.id})">Details</button>
      </td>
    `;
    tbody.appendChild(tr);
  });
}

window.viewTransactionModal = async function(id) {
  try {
    const t = await ApiClient.getTransactionById(id);
    const modal = document.getElementById("txnDetailModal");
    const body = document.getElementById("txnDetailBody");

    body.innerHTML = `
      <div style="display:grid; grid-template-columns: 1fr 1fr; gap:12px; font-size:0.9rem;">
        <div><strong>Transaction ID:</strong> ${t.externalTransactionId}</div>
        <div><strong>Timestamp:</strong> ${new Date(t.timestamp).toLocaleString()}</div>
        <div><strong>Type:</strong> ${t.type}</div>
        <div><strong>Amount:</strong> $${Number(t.amount).toLocaleString()}</div>
        <div><strong>Sender:</strong> ${t.senderId}</div>
        <div><strong>Receiver:</strong> ${t.receiverId}</div>
        <div><strong>Origin Bal (Pre):</strong> $${Number(t.oldBalanceOrig).toLocaleString()}</div>
        <div><strong>Origin Bal (Post):</strong> $${Number(t.newBalanceOrig).toLocaleString()}</div>
        <div><strong>Dest Bal (Pre):</strong> $${Number(t.oldBalanceDest).toLocaleString()}</div>
        <div><strong>Dest Bal (Post):</strong> $${Number(t.newBalanceDest).toLocaleString()}</div>
        <div><strong>Prediction:</strong> <span class="badge ${t.predictedClass === 'FRAUD' ? 'badge-fraud' : 'badge-legit'}">${t.predictedClass}</span></div>
        <div><strong>Fraud Risk:</strong> ${t.riskLevel} (${((t.fraudProbability || 0) * 100).toFixed(1)}%)</div>
      </div>
      <div style="margin-top:16px; padding:12px; background:#f8fafc; border-radius:6px; font-size:0.85rem;">
        <strong>Model Explainability Summary:</strong><br>
        ${t.explanationSummary || 'No specific anomalies identified.'}
      </div>
    `;

    modal.style.display = "flex";
  } catch (err) {
    showToast(err.message, "danger");
  }
};

/* =========================================================================
   7. FRAUD ALERTS PANEL
   ========================================================================= */
function initAlertsUI() {
  document.getElementById("alertFilterStatus")?.addEventListener("change", loadFraudAlerts);

  document.getElementById("alertReviewForm")?.addEventListener("submit", async (e) => {
    e.preventDefault();
    const id = document.getElementById("modalAlertId").value;
    const status = document.getElementById("modalAlertStatus").value;
    const remarks = document.getElementById("modalAlertRemarks").value.trim();

    try {
      await ApiClient.updateAlertStatus(id, status, remarks);
      showToast("Alert review status updated!", "success");
      document.getElementById("alertReviewModal").style.display = "none";
      loadFraudAlerts();
      loadDashboardData();
    } catch (err) {
      showToast(err.message, "danger");
    }
  });
}

async function loadFraudAlerts() {
  const status = document.getElementById("alertFilterStatus")?.value;
  const params = { page: 0, size: 25 };
  if (status) params.status = status;

  try {
    const data = await ApiClient.getAlerts(params);
    renderAlertsTable(data.content || []);
  } catch (err) {
    console.error("Alerts load failed:", err);
  }
}

function renderAlertsTable(alerts) {
  const tbody = document.getElementById("alertsTableBody");
  if (!tbody) return;
  tbody.innerHTML = "";

  if (alerts.length === 0) {
    tbody.innerHTML = `<tr><td colspan="7" style="text-align:center; padding: 20px; color:#94a3b8;">No fraud alerts matching filter</td></tr>`;
    return;
  }

  alerts.forEach(a => {
    const statusBg = a.reviewStatus === "PENDING" ? "var(--danger-bg)" : (a.reviewStatus === "REVIEWED" ? "var(--warning-bg)" : "var(--success-bg)");
    const statusColor = a.reviewStatus === "PENDING" ? "var(--danger-red)" : (a.reviewStatus === "REVIEWED" ? "var(--warning-amber)" : "var(--success-green)");

    const tr = document.createElement("tr");
    tr.innerHTML = `
      <td><strong>${a.externalTransactionId}</strong></td>
      <td>$${Number(a.amount).toLocaleString(undefined, {minimumFractionDigits: 2})}</td>
      <td><span class="badge badge-high">${((a.fraudProbability || 0) * 100).toFixed(1)}%</span></td>
      <td><span class="badge" style="background:${statusBg}; color:${statusColor}; font-weight:700;">${a.reviewStatus}</span></td>
      <td>${a.reviewedByUsername || "—"}</td>
      <td style="font-size:0.8rem; max-width:200px; white-space:nowrap; overflow:hidden; text-overflow:ellipsis;">${a.remarks || a.explanationSummary || "Awaiting review"}</td>
      <td>
        <button class="btn btn-outline btn-sm" onclick="openAlertModal(${a.id}, '${a.reviewStatus}', '${(a.remarks || '').replace(/'/g, "\\'")}')">Review</button>
      </td>
    `;
    tbody.appendChild(tr);
  });
}

window.openAlertModal = function(id, status, remarks) {
  const modal = document.getElementById("alertReviewModal");
  document.getElementById("modalAlertId").value = id;
  document.getElementById("modalAlertStatus").value = status;
  document.getElementById("modalAlertRemarks").value = remarks || "";
  modal.style.display = "flex";
};

/* =========================================================================
   8. ADMIN & MODEL METRICS
   ========================================================================= */
async function loadAdminData() {
  try {
    const users = await ApiClient.getAdminUsers();
    renderUsersTable(users);

    const metrics = await ApiClient.getModelMetrics();
    renderModelMetrics(metrics);

    const logs = await ApiClient.getAuditLogs({ page: 0, size: 15 });
    renderAuditLogsTable(logs.content || []);
  } catch (err) {
    console.error("Admin data load failed:", err);
  }
}

function renderUsersTable(users) {
  const tbody = document.getElementById("adminUsersTableBody");
  if (!tbody) return;
  tbody.innerHTML = "";

  users.forEach(u => {
    const tr = document.createElement("tr");
    tr.innerHTML = `
      <td>${u.id}</td>
      <td><strong>${u.username}</strong></td>
      <td>${u.email}</td>
      <td><span class="role-pill">${u.role}</span></td>
      <td>${u.fullName || "—"}</td>
      <td><span class="badge ${u.isActive ? 'badge-legit' : 'badge-low'}">${u.isActive ? 'ACTIVE' : 'DISABLED'}</span></td>
    `;
    tbody.appendChild(tr);
  });
}

function renderModelMetrics(m) {
  if (!m) return;
  document.getElementById("metricModelVersion").textContent = m.modelVersion || "v1.0-ensemble";

  const cb = m.catboost || {};
  const xb = m.xgboost || {};
  const ens = m.ensemble || {};

  document.getElementById("cbAcc").textContent = (cb.accuracy || 0).toFixed(4);
  document.getElementById("cbPrec").textContent = (cb.precision || 0).toFixed(4);
  document.getElementById("cbRec").textContent = (cb.recall || 0).toFixed(4);
  document.getElementById("cbF1").textContent = (cb.f1_score || 0).toFixed(4);
  document.getElementById("cbRoc").textContent = (cb.roc_auc || 0).toFixed(4);

  document.getElementById("xbAcc").textContent = (xb.accuracy || 0).toFixed(4);
  document.getElementById("xbPrec").textContent = (xb.precision || 0).toFixed(4);
  document.getElementById("xbRec").textContent = (xb.recall || 0).toFixed(4);
  document.getElementById("xbF1").textContent = (xb.f1_score || 0).toFixed(4);
  document.getElementById("xbRoc").textContent = (xb.roc_auc || 0).toFixed(4);

  document.getElementById("ensAcc").textContent = (ens.accuracy || 0).toFixed(4);
  document.getElementById("ensPrec").textContent = (ens.precision || 0).toFixed(4);
  document.getElementById("ensRec").textContent = (ens.recall || 0).toFixed(4);
  document.getElementById("ensF1").textContent = (ens.f1_score || 0).toFixed(4);
  document.getElementById("ensRoc").textContent = (ens.roc_auc || 0).toFixed(4);

  const cm = ens.confusion_matrix || {};
  document.getElementById("cmTP").textContent = cm.true_positive || 0;
  document.getElementById("cmFP").textContent = cm.false_positive || 0;
  document.getElementById("cmTN").textContent = cm.true_negative || 0;
  document.getElementById("cmFN").textContent = cm.false_negative || 0;
}

function renderAuditLogsTable(logs) {
  const tbody = document.getElementById("adminAuditBody");
  if (!tbody) return;
  tbody.innerHTML = "";

  logs.forEach(l => {
    const tr = document.createElement("tr");
    tr.innerHTML = `
      <td>${new Date(l.timestamp).toLocaleString()}</td>
      <td><strong>${l.username || "SYSTEM"}</strong></td>
      <td><span class="badge" style="background:#e2e8f0; color:#1e293b;">${l.action}</span></td>
      <td>${l.entityType} (${l.entityId || "—"})</td>
      <td style="font-size:0.8rem;">${l.details}</td>
      <td style="font-size:0.8rem; color:#64748b;">${l.ipAddress || "127.0.0.1"}</td>
    `;
    tbody.appendChild(tr);
  });
}

/* =========================================================================
   9. TOAST NOTIFICATIONS
   ========================================================================= */
function showToast(message, type = "info") {
  const container = document.getElementById("toastContainer");
  if (!container) return;

  const toast = document.createElement("div");
  toast.className = `toast`;
  if (type === "danger") toast.style.backgroundColor = "var(--danger-red)";
  if (type === "success") toast.style.backgroundColor = "var(--success-green)";
  if (type === "warning") toast.style.backgroundColor = "var(--warning-amber)";

  toast.innerHTML = `<span>${message}</span>`;
  container.appendChild(toast);

  setTimeout(() => {
    toast.remove();
  }, 4000);
}
