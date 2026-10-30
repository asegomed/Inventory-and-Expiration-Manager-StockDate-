const API_URL = "/api/inventory";
const views = ["dashboard", "inventory", "register"];
let batches = [];

const elements = {
    notice: document.querySelector("#notice"),
    dashboardBatches: document.querySelector("#dashboard-batches"),
    inventoryRows: document.querySelector("#inventory-rows"),
    inventorySearch: document.querySelector("#inventory-search"),
    inventoryCount: document.querySelector("#inventory-count"),
    inventoryEmpty: document.querySelector("#inventory-empty"),
    inventoryEmptyTitle: document.querySelector("#inventory-empty-title"),
    inventoryEmptyCopy: document.querySelector("#inventory-empty-copy"),
    form: document.querySelector("#batch-form"),
    expirationDate: document.querySelector("#expiration-date"),
    totalCount: document.querySelector("#total-count"),
    redCount: document.querySelector("#red-count"),
    nextExpiration: document.querySelector("#next-expiration"),
    nextProduct: document.querySelector("#next-product")
};

const statusLabels = {
    EXPIRED: "Caducado",
    RED: "Caduca pronto",
    YELLOW: "Proxima caducidad",
    GREEN: "En buen estado"
};

function showNotice(message, isError = false) {
    elements.notice.textContent = message;
    elements.notice.classList.toggle("error", isError);
    elements.notice.hidden = false;
}

function hideNotice() {
    elements.notice.hidden = true;
    elements.notice.classList.remove("error");
}

function formatDate(value) {
    return new Intl.DateTimeFormat("es-ES", {
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

function expirationText(batch) {
    if (batch.daysUntilExpiration < 0) {
        return `Caducado hace ${Math.abs(batch.daysUntilExpiration)} dias`;
    }
    if (batch.daysUntilExpiration === 0) {
        return "Caduca hoy";
    }
    if (batch.daysUntilExpiration === 1) {
        return "Caduca manana";
    }
    return `En ${batch.daysUntilExpiration} dias`;
}

function renderDashboard() {
    elements.totalCount.textContent = batches.length;
    elements.redCount.textContent = batches.filter(batch => batch.status === "RED").length;

    const nextBatch = batches.find(batch => batch.daysUntilExpiration >= 0);
    elements.nextExpiration.textContent = nextBatch ? formatDate(nextBatch.expirationDate) : "—";
    elements.nextProduct.textContent = nextBatch ? nextBatch.productName : "Sin lotes registrados";

    const attentionBatches = batches.filter(batch => batch.status !== "GREEN").slice(0, 5);
    if (attentionBatches.length === 0) {
        elements.dashboardBatches.innerHTML = `
            <div class="empty-state">
                <span class="empty-icon" aria-hidden="true">✓</span>
                <h2>Todo en orden</h2>
                <p>${batches.length === 0
                    ? "Registra un lote para comenzar a controlar las caducidades."
                    : "No hay lotes proximos a caducar."}</p>
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
                <p class="batch-date">${batch.quantity} unidades</p>
                <p class="batch-date-label">${escapeHtml(batch.storageLocation)}</p>
            </div>
            <div>
                <p class="batch-date">${formatDate(batch.expirationDate)}</p>
                <p class="batch-date-label">${expirationText(batch)}</p>
            </div>
            ${statusMarkup(batch)}
        </article>`).join("");
}

function renderInventory() {
    const query = elements.inventorySearch.value.trim().toLocaleLowerCase("es");
    const filteredBatches = batches.filter(batch =>
        batch.productName.toLocaleLowerCase("es").includes(query)
        || batch.category.toLocaleLowerCase("es").includes(query)
        || batch.storageLocation.toLocaleLowerCase("es").includes(query)
    );

    elements.inventoryCount.textContent = `${filteredBatches.length} ${
        filteredBatches.length === 1 ? "lote" : "lotes"
    }`;
    elements.inventoryEmpty.hidden = filteredBatches.length > 0;
    elements.inventoryRows.closest(".table-wrap").hidden = filteredBatches.length === 0;

    if (batches.length === 0) {
        elements.inventoryEmptyTitle.textContent = "Aun no hay lotes";
        elements.inventoryEmptyCopy.textContent = "Registra el primer lote para verlo aqui.";
    } else {
        elements.inventoryEmptyTitle.textContent = "No se encontraron lotes";
        elements.inventoryEmptyCopy.textContent = "Prueba con otro nombre, categoria o ubicacion.";
    }

    elements.inventoryRows.innerHTML = filteredBatches.map(batch => `
        <tr>
            <td>${escapeHtml(batch.productName)}</td>
            <td>${escapeHtml(batch.category)}</td>
            <td>${batch.quantity}</td>
            <td>${escapeHtml(batch.storageLocation)}</td>
            <td>${formatDate(batch.expirationDate)}</td>
            <td>${statusMarkup(batch)}</td>
        </tr>`).join("");
}

function render() {
    renderDashboard();
    renderInventory();
}

function showView(viewName) {
    if (!views.includes(viewName)) {
        return;
    }

    views.forEach(view => {
        document.querySelector(`#${view}-view`).hidden = view !== viewName;
    });
    document.querySelectorAll("[data-view]").forEach(button => {
        button.classList.toggle("active", button.dataset.view === viewName);
    });
    hideNotice();

    if (viewName === "register") {
        const today = new Date();
        elements.expirationDate.min = [
            today.getFullYear(),
            String(today.getMonth() + 1).padStart(2, "0"),
            String(today.getDate()).padStart(2, "0")
        ].join("-");
        elements.form.elements.productName.focus();
    }
}

async function loadBatches() {
    try {
        const response = await fetch(API_URL);
        if (!response.ok) {
            throw new Error("No se pudo cargar el inventario.");
        }
        batches = await response.json();
        render();
    } catch (error) {
        showNotice(`${error.message} Comprueba que StockDate este en ejecucion e intenta recargar.`, true);
    }
}

document.querySelectorAll("[data-view]").forEach(button => {
    button.addEventListener("click", () => showView(button.dataset.view));
});

document.querySelectorAll("[data-open-register]").forEach(button => {
    button.addEventListener("click", () => showView("register"));
});

elements.inventorySearch.addEventListener("input", renderInventory);

elements.form.addEventListener("submit", async event => {
    event.preventDefault();
    hideNotice();

    if (!elements.form.reportValidity()) {
        return;
    }

    const formData = new FormData(elements.form);
    const request = {
        productName: formData.get("productName").trim(),
        category: formData.get("category").trim(),
        quantity: Number(formData.get("quantity")),
        storageLocation: formData.get("storageLocation").trim(),
        expirationDate: formData.get("expirationDate"),
        expirationDateType: formData.get("expirationDateType")
    };

    try {
        const response = await fetch(API_URL, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(request)
        });
        const result = await response.json();
        if (!response.ok) {
            throw new Error(result.message || "No se pudo guardar el lote.");
        }

        elements.form.reset();
        batches.push(result);
        batches.sort((first, second) =>
            first.expirationDate.localeCompare(second.expirationDate) || first.id - second.id
        );
        render();
        showView("inventory");
        showNotice("Lote registrado correctamente.");
    } catch (error) {
        showNotice(error.message || "No se pudo guardar el lote.", true);
    }
});

document.querySelector("#today-label").textContent = new Intl.DateTimeFormat("es-ES", {
    weekday: "long",
    day: "numeric",
    month: "long",
    year: "numeric"
}).format(new Date());

loadBatches();
