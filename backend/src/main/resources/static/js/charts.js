/**
 * Chart.js Visualizations for Fraud Detection Dashboard
 */

class DashboardCharts {
  constructor() {
    this.fraudRatioChart = null;
    this.riskLevelChart = null;
    this.trendLineChart = null;
    this.typeDistributionChart = null;
  }

  init(stats) {
    this.renderFraudRatio(stats.predictedClassDistribution || {});
    this.renderRiskDistribution(stats.riskLevelDistribution || {});
    this.renderDailyTrends(stats.dailyTrends || []);
    this.renderTypeDistribution(stats.transactionTypeDistribution || {});
  }

  renderFraudRatio(classDist) {
    const ctx = document.getElementById("fraudRatioChart");
    if (!ctx) return;

    const fraud = classDist["FRAUD"] || 0;
    const legit = classDist["LEGITIMATE"] || 0;

    if (this.fraudRatioChart) {
      this.fraudRatioChart.destroy();
    }

    this.fraudRatioChart = new Chart(ctx, {
      type: "doughnut",
      data: {
        labels: ["Legitimate", "Potential Fraud"],
        datasets: [{
          data: [legit, fraud],
          backgroundColor: ["#10b981", "#ef4444"],
          borderWidth: 2,
          borderColor: "#ffffff"
        }]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
          legend: { position: "bottom" },
          tooltip: {
            callbacks: {
              label: (ctx) => ` ${ctx.label}: ${ctx.raw} transactions`
            }
          }
        },
        cutout: "70%"
      }
    });
  }

  renderRiskDistribution(riskDist) {
    const ctx = document.getElementById("riskDistributionChart");
    if (!ctx) return;

    const low = riskDist["LOW"] || 0;
    const med = riskDist["MEDIUM"] || 0;
    const high = riskDist["HIGH"] || 0;

    if (this.riskLevelChart) {
      this.riskLevelChart.destroy();
    }

    this.riskLevelChart = new Chart(ctx, {
      type: "bar",
      data: {
        labels: ["Low Risk", "Medium Risk", "High Risk"],
        datasets: [{
          label: "Transactions",
          data: [low, med, high],
          backgroundColor: ["#64748b", "#f59e0b", "#ef4444"],
          borderRadius: 6
        }]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
          legend: { display: false }
        },
        scales: {
          y: { beginAtZero: true, grid: { color: "#f1f5f9" } },
          x: { grid: { display: false } }
        }
      }
    });
  }

  renderDailyTrends(trends) {
    const ctx = document.getElementById("dailyTrendChart");
    if (!ctx) return;

    const labels = trends.map(t => t.date);
    const legitData = trends.map(t => t.legitimate);
    const fraudData = trends.map(t => t.fraud);

    if (this.trendLineChart) {
      this.trendLineChart.destroy();
    }

    this.trendLineChart = new Chart(ctx, {
      type: "line",
      data: {
        labels: labels.length ? labels : ["Day 1", "Day 2", "Day 3", "Day 4", "Day 5", "Day 6", "Day 7"],
        datasets: [
          {
            label: "Legitimate",
            data: legitData.length ? legitData : [12, 19, 15, 25, 22, 30, 28],
            borderColor: "#10b981",
            backgroundColor: "rgba(16, 185, 129, 0.1)",
            tension: 0.35,
            fill: true
          },
          {
            label: "Suspected Fraud",
            data: fraudData.length ? fraudData : [1, 2, 0, 3, 2, 1, 4],
            borderColor: "#ef4444",
            backgroundColor: "rgba(239, 68, 68, 0.1)",
            tension: 0.35,
            fill: true
          }
        ]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
          legend: { position: "top" }
        },
        scales: {
          y: { beginAtZero: true, grid: { color: "#f1f5f9" } },
          x: { grid: { color: "#f1f5f9" } }
        }
      }
    });
  }

  renderTypeDistribution(typeDist) {
    const ctx = document.getElementById("typeDistributionChart");
    if (!ctx) return;

    const labels = Object.keys(typeDist);
    const data = Object.values(typeDist);

    if (this.typeDistributionChart) {
      this.typeDistributionChart.destroy();
    }

    this.typeDistributionChart = new Chart(ctx, {
      type: "bar",
      data: {
        labels: labels.length ? labels : ["PAYMENT", "TRANSFER", "CASH_OUT", "CASH_IN", "DEBIT"],
        datasets: [{
          label: "Volume",
          data: data.length ? data : [150, 45, 60, 80, 20],
          backgroundColor: "#3b82f6",
          borderRadius: 6
        }]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
          legend: { display: false }
        },
        scales: {
          y: { beginAtZero: true },
          x: { grid: { display: false } }
        }
      }
    });
  }
}

window.DashboardCharts = new DashboardCharts();
