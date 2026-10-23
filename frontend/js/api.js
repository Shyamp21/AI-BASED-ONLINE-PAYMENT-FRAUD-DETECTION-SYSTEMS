/**
 * API Client for AI Online Payment Fraud Detection System
 * Interacts with Java Spring Boot backend (port 8080)
 */

const API_BASE_URL = window.location.origin.includes(":8080")
  ? `${window.location.origin}/api`
  : "http://localhost:8080/api";

const TOKEN_KEY = "fraudguard_jwt_token";
const USER_KEY = "fraudguard_user_data";

class ApiClient {
  static getToken() {
    return localStorage.getItem(TOKEN_KEY);
  }

  static setAuth(token, user) {
    localStorage.setItem(TOKEN_KEY, token);
    localStorage.setItem(USER_KEY, JSON.stringify(user));
  }

  static clearAuth() {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(USER_KEY);
  }

  static getUser() {
    const raw = localStorage.getItem(USER_KEY);
    try {
      return raw ? JSON.parse(raw) : null;
    } catch {
      return null;
    }
  }

  static isAuthenticated() {
    return !!this.getToken();
  }

  static async request(endpoint, options = {}) {
    const headers = options.headers || {};
    const token = this.getToken();

    if (token) {
      headers["Authorization"] = `Bearer ${token}`;
    }

    if (!(options.body instanceof FormData)) {
      headers["Content-Type"] = "application/json";
    }

    const config = {
      ...options,
      headers
    };

    try {
      const response = await fetch(`${API_BASE_URL}${endpoint}`, config);

      if (response.status === 401) {
        this.clearAuth();
        window.dispatchEvent(new CustomEvent("auth:required"));
        throw new Error("Session expired. Please log in again.");
      }

      const contentType = response.headers.get("content-type");
      let data = null;
      if (contentType && contentType.includes("application/json")) {
        data = await response.json();
      } else {
        data = await response.text();
      }

      if (!response.ok) {
        const errorMsg = data && data.message ? data.message : `HTTP ${response.status}: Request failed`;
        throw new Error(errorMsg);
      }

      return data;
    } catch (err) {
      console.error(`API Error [${endpoint}]:`, err);
      throw err;
    }
  }

  // Auth Endpoints
  static async login(username, password) {
    const res = await this.request("/auth/login", {
      method: "POST",
      body: JSON.stringify({ username, password })
    });
    this.setAuth(res.token, {
      id: res.id,
      username: res.username,
      email: res.email,
      role: res.role,
      fullName: res.fullName
    });
    return res;
  }

  static async register(username, email, password, fullName, role = "ROLE_USER") {
    const res = await this.request("/auth/register", {
      method: "POST",
      body: JSON.stringify({ username, email, password, fullName, role })
    });
    this.setAuth(res.token, {
      id: res.id,
      username: res.username,
      email: res.email,
      role: res.role,
      fullName: res.fullName
    });
    return res;
  }

  // Dashboard Endpoints
  static async getSummary() {
    return this.request("/dashboard/summary");
  }

  static async getStatistics() {
    return this.request("/dashboard/statistics");
  }

  // Transaction & Prediction Endpoints
  static async predictTransaction(data) {
    return this.request("/transactions/predict", {
      method: "POST",
      body: JSON.stringify(data)
    });
  }

  static async batchPredictCsv(file) {
    const formData = new FormData();
    formData.append("file", file);
    return this.request("/transactions/batch-predict", {
      method: "POST",
      body: formData
    });
  }

  static async getTransactions(params = {}) {
    const query = new URLSearchParams(params).toString();
    return this.request(`/transactions?${query}`);
  }

  static async getTransactionById(id) {
    return this.request(`/transactions/${id}`);
  }

  // Fraud Alert Endpoints
  static async getAlerts(params = {}) {
    const query = new URLSearchParams(params).toString();
    return this.request(`/alerts?${query}`);
  }

  static async updateAlertStatus(id, status, remarks) {
    return this.request(`/alerts/${id}/status`, {
      method: "PATCH",
      body: JSON.stringify({ status, remarks })
    });
  }

  // Admin Endpoints
  static async getAdminUsers() {
    return this.request("/admin/users");
  }

  static async getModelMetrics() {
    return this.request("/admin/model-metrics");
  }

  static async getAuditLogs(params = {}) {
    const query = new URLSearchParams(params).toString();
    return this.request(`/admin/audit-logs?${query}`);
  }
}

window.ApiClient = ApiClient;
