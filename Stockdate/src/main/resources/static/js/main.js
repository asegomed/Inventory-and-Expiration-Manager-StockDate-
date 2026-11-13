// Dashboard page (UC-05). The summary is calculated and cached by the server (GET /api/dashboard).
const DASHBOARD_URL = "/api/dashboard";
const elements = {
    dashboardBatches: document.querySelector("#dashboard-batches"),
    totalCount: document.querySelector("#total-count"),
    redCount: document.querySelector("#red-count"),
    expiredCount: document.querySelector("#expired-count"),
    nextExpiration: document.querySelector("#next-expiration"),
    nextProduct: document.querySelector("#next-product")
};

function expirationText(batch) {
    if (batch.daysUntilExpiration < 0) {
        return `Expired ${Math.abs(batch.daysUntilExpiration)} days ago`;
    }
    if (batch.daysUntilExpiration === 0) {
        return "Expires today";
    }
    if (batch.daysUntilExpiration === 1) {
        return "Expires tomorrow";
    }
    return `In ${batch.daysUntilExpiration} days`;
}

function renderDashboard(dashboard) {
    const total = dashboard.expiredCount + dashboard.redCount + dashboard.yellowCount + dashboard.greenCount;
    elements.totalCount.textContent = total;
    elements.redCount.textContent = dashboard.redCount;
    elements.expiredCount.textContent = dashboard.expiredCount;

    const next = dashboard.nextExpiring;
    elements.nextExpiration.textContent = next ? formatDate(next.expirationDate) : "—";
    elements.nextProduct.textContent = next ? next.productName : "No upcoming expirations";

    const attentionBatches = dashboard.pinnedBatches;
    if (attentionBatches.length === 0) {
        elements.dashboardBatches.innerHTML = `
            <div class="empty-state">
                <span class="empty-icon" aria-hidden="true">✓</span>
                <h2>All clear</h2>
                <p>${total === 0
                    ? "Add a batch to start tracking expiration dates."
                    : "No batches are expired or expiring soon."}</p>
            </div>`;
        return;
    }

    elements.dashboardBatches.innerHTML = attentionBatches.map(batch => `
            <article class="batch-row">
                <div>
                    <p class="batch-name">${escapeHtml(batch.productName)}</p>
                    <p class="batch-subtitle">${escapeHtml(batch.category)}</p>
                </div>
                <div class="batch-quantity">
                    <p class="batch-date">${batch.quantity} units</p>
                    <p class="batch-date-label">${escapeHtml(batch.storageLocation)}</p>
                </div>
                <div>
                    <p class="batch-date">${formatDate(batch.expirationDate)}</p>
                    <p class="batch-date-label">${expirationText(batch)}</p>
                </div>
                ${statusMarkup(batch)}
            </article>`).join("");
}

async function loadDashboard() {
    try {
        const response = await fetch(DASHBOARD_URL);
        if (!response.ok) {
            throw new Error("Could not load the dashboard.");
        }
        renderDashboard(await response.json());
    } catch (error) {
        showNotice(`${error.message} Make sure StockDate is running, then try refreshing.`, true);
    }
}

document.querySelector("#today-label").textContent = new Intl.DateTimeFormat("en-US", {
    weekday: "long",
    day: "numeric",
    month: "long",
    year: "numeric"
}).format(new Date());

loadDashboard();
