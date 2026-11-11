const API_URL = "/api/inventory";
const elements = {
    notice: document.querySelector("#notice"),
    dashboardBatches: document.querySelector("#dashboard-batches"),
    totalCount: document.querySelector("#total-count"),
    redCount: document.querySelector("#red-count"),
    nextExpiration: document.querySelector("#next-expiration"),
    nextProduct: document.querySelector("#next-product")
};
let batches = [];

const statusLabels = {
    EXPIRED: "Expired",
    RED: "Expiring soon",
    YELLOW: "Upcoming",
    GREEN: "In date"
};

function formatDate(value) {
    return new Intl.DateTimeFormat("en-US", {
        day: "numeric",
        month: "short",
        year: "numeric"
    }).format(new Date(`${value}T00:00:00`));
}

function escapeHtml(value) {
    return String(value).replace(/[&<>"']/g, character => ({
        "&": "&amp;",
        "<": "&lt;",
        ">": "&gt;",
        "\"": "&quot;",
        "'": "&#39;"
    })[character]);
}

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

function renderDashboard() {
    elements.totalCount.textContent = batches.length;
    elements.redCount.textContent = batches.filter(batch => batch.status === "RED").length;

    const nextBatch = batches.find(batch => batch.daysUntilExpiration >= 0);
    elements.nextExpiration.textContent = nextBatch ? formatDate(nextBatch.expirationDate) : "—";
    elements.nextProduct.textContent = nextBatch ? nextBatch.productName : "No batches registered";

    const attentionBatches = batches.filter(batch => batch.status !== "GREEN").slice(0, 5);
    if (attentionBatches.length === 0) {
        elements.dashboardBatches.innerHTML = `
            <div class="empty-state">
                <span class="empty-icon" aria-hidden="true">✓</span>
                <h2>All clear</h2>
                <p>${batches.length === 0
                    ? "Add a batch to start tracking expiration dates."
                    : "No batches are expiring soon."}</p>
            </div>`;
        return;
    }

    elements.dashboardBatches.innerHTML = attentionBatches.map(batch => {
        const status = statusLabels[batch.status] ? batch.status : "GREEN";
        return `
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
                <span class="status-badge status-${status.toLowerCase()}">${statusLabels[status]}</span>
            </article>`;
    }).join("");
}

async function loadBatches() {
    try {
        const response = await fetch(API_URL);
        if (!response.ok) {
            throw new Error("Could not load inventory.");
        }
        batches = await response.json();
        renderDashboard();
    } catch (error) {
        elements.notice.textContent = `${error.message} Make sure StockDate is running, then try refreshing.`;
        elements.notice.classList.add("error");
        elements.notice.hidden = false;
    }
}

document.querySelector("#today-label").textContent = new Intl.DateTimeFormat("en-US", {
    weekday: "long",
    day: "numeric",
    month: "long",
    year: "numeric"
}).format(new Date());

loadBatches();
