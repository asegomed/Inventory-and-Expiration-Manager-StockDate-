const API_URL = "/api/inventory";
let batches = [];
let editingBatchId = null;

const elements = {
    notice: document.querySelector("#notice"),
    inventoryRows: document.querySelector("#inventory-rows"),
    inventorySearch: document.querySelector("#inventory-search"),
    inventoryCount: document.querySelector("#inventory-count"),
    inventoryEmpty: document.querySelector("#inventory-empty"),
    inventoryEmptyTitle: document.querySelector("#inventory-empty-title"),
    inventoryEmptyCopy: document.querySelector("#inventory-empty-copy"),
    form: document.querySelector("#batch-form"),
    expirationDate: document.querySelector("#expiration-date"),
    expirationDateType: document.querySelector('[name="expirationDateType"]'),
    formTitle: document.querySelector("#batch-form-title"),
    formSubmit: document.querySelector("#batch-form-submit")
};

const statusLabels = {
    EXPIRED: "Expired",
    RED: "Expiring soon",
    YELLOW: "Upcoming",
    GREEN: "In date"
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

function renderInventory() {
    const query = elements.inventorySearch.value.trim().toLocaleLowerCase("en-US");
    const filteredBatches = batches.filter(batch =>
        batch.productName.toLocaleLowerCase("en-US").includes(query)
        || batch.category.toLocaleLowerCase("en-US").includes(query)
        || batch.storageLocation.toLocaleLowerCase("en-US").includes(query)
    );

    elements.inventoryCount.textContent = `${filteredBatches.length} ${
        filteredBatches.length === 1 ? "batch" : "batches"
    }`;
    elements.inventoryEmpty.hidden = filteredBatches.length > 0;
    elements.inventoryRows.closest(".table-wrap").hidden = filteredBatches.length === 0;

    if (batches.length === 0) {
        elements.inventoryEmptyTitle.textContent = "No batches yet";
        elements.inventoryEmptyCopy.textContent = "Add your first batch to see it here.";
    } else {
        elements.inventoryEmptyTitle.textContent = "No batches found";
        elements.inventoryEmptyCopy.textContent = "Try another product, category, or storage location.";
    }

    elements.inventoryRows.innerHTML = filteredBatches.map(batch => `
        <tr>
            <td>${escapeHtml(batch.productName)}</td>
            <td>${escapeHtml(batch.category)}</td>
            <td>${batch.quantity}</td>
            <td>${escapeHtml(batch.storageLocation)}</td>
            <td>${formatDate(batch.expirationDate)}</td>
            <td>${statusMarkup(batch)}</td>
            <td><button class="button button-secondary edit-batch" type="button" data-edit-id="${batch.id}">Edit</button></td>
        </tr>`).join("");
}

function showView(viewName) {
    const showForm = viewName === "register";
    document.querySelector("#inventory-view").hidden = showForm;
    document.querySelector("#register-view").hidden = !showForm;
    hideNotice();

    if (showForm) {
        if (editingBatchId === null) {
            elements.formTitle.textContent = "Add a batch";
            elements.formSubmit.textContent = "Save batch";
        }
        updateExpirationDateMinimum();
        elements.form.elements.productName.focus();
    }
}

function updateExpirationDateMinimum() {
    if (elements.expirationDateType.value === "USE_BY") {
        const today = new Date();
        elements.expirationDate.min = [
            today.getFullYear(),
            String(today.getMonth() + 1).padStart(2, "0"),
            String(today.getDate()).padStart(2, "0")
        ].join("-");
    } else {
        elements.expirationDate.removeAttribute("min");
    }
}

function editBatch(batchId) {
    const batch = batches.find(item => item.id === batchId);
    if (!batch) {
        showNotice("Could not find that batch. Refresh the products and try again.", true);
        return;
    }

    editingBatchId = batch.id;
    elements.form.elements.productName.value = batch.productName;
    elements.form.elements.category.value = batch.category;
    elements.form.elements.quantity.value = batch.quantity;
    elements.form.elements.storageLocation.value = batch.storageLocation;
    elements.form.elements.expirationDate.value = batch.expirationDate;
    elements.form.elements.expirationDateType.value = batch.expirationDateType;
    elements.formTitle.textContent = "Edit batch";
    elements.formSubmit.textContent = "Save changes";
    showView("register");
}

async function loadBatches() {
    try {
        const response = await fetch(API_URL);
        if (!response.ok) {
            throw new Error("Could not load inventory.");
        }
        batches = await response.json();
        renderInventory();
    } catch (error) {
        showNotice(`${error.message} Make sure StockDate is running, then try refreshing.`, true);
    }
}

document.querySelectorAll("[data-open-register]").forEach(button => {
    button.addEventListener("click", () => {
        editingBatchId = null;
        elements.form.reset();
        showView("register");
    });
});

elements.inventorySearch.addEventListener("input", renderInventory);
elements.expirationDateType.addEventListener("change", updateExpirationDateMinimum);
elements.inventoryRows.addEventListener("click", event => {
    const editButton = event.target.closest("[data-edit-id]");
    if (editButton) {
        editBatch(Number(editButton.dataset.editId));
    }
});
elements.form.addEventListener("reset", () => {
    editingBatchId = null;
    elements.formTitle.textContent = "Add a batch";
    elements.formSubmit.textContent = "Save batch";
    updateExpirationDateMinimum();
});
document.querySelector("[data-cancel-form]").addEventListener("click", () => {
    elements.form.reset();
    showView("inventory");
});

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
        const isEditing = editingBatchId !== null;
        const response = await fetch(isEditing ? `${API_URL}/${editingBatchId}` : API_URL, {
            method: isEditing ? "PUT" : "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(request)
        });
        const result = await response.json();
        if (!response.ok) {
            throw new Error(result.message || (isEditing ? "Could not update the batch." : "Could not save the batch."));
        }

        editingBatchId = null;
        elements.form.reset();
        if (isEditing) {
            batches = batches.map(batch => batch.id === result.id ? result : batch);
        } else {
            batches.push(result);
        }
        batches.sort((first, second) =>
            first.expirationDate.localeCompare(second.expirationDate) || first.id - second.id
        );
        renderInventory();
        showView("inventory");
        showNotice(isEditing ? "Batch updated successfully." : "Batch added successfully.");
    } catch (error) {
        showNotice(error.message || "Could not save the batch.", true);
    }
});

loadBatches();
