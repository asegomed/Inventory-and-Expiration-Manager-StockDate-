// Helpers shared by main.js (dashboard) and app.js (products page).

const statusLabels = {
    EXPIRED: "Expired",
    RED: "Expiring soon",
    YELLOW: "Upcoming",
    GREEN: "In date"
};

function showNotice(message, isError = false) {
    const notice = document.querySelector("#notice");
    notice.textContent = message;
    notice.classList.toggle("error", isError);
    notice.hidden = false;
}

function hideNotice() {
    const notice = document.querySelector("#notice");
    notice.hidden = true;
    notice.classList.remove("error");
}

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

function statusMarkup(batch) {
    const status = statusLabels[batch.status] ? batch.status : "GREEN";
    return `<span class="status-badge status-${status.toLowerCase()}">${statusLabels[status]}</span>`;
}
