(function (global, $) {
  /** CONSTANTS **/
  const HEADERS = [
    "ID",
    "Start",
    "End",
    "Status",
    "Error Message",
    "Request: Query Params",
    "Request: Body",
    "Response: Code",
    "Response: Headers",
    "Response: Body",
    "Response: Body Size",
  ];

  // Columns that should show "View Details" button instead of full content
  const MODAL_COLUMNS = [5, 6, 8, 9];

  /** Globals **/
  let dt = null;
  let rawData = []; // Store original data for modal display

  function truncateContent(content, maxLength = 100) {
    if (!content || content.length <= maxLength) return content;
    return content.substring(0, maxLength) + "...";
  }

  function truncateId(id) {
    if (!id || typeof id !== "string") return id;
    return id.length > 7 ? id.slice(-7) : id;
  }

  function createViewButton(rowIndex, columnIndex) {
    return `<button class="btn btn-sm btn-outline-primary view-details-btn" 
              data-row="${rowIndex}" 
              data-column="${columnIndex}"
              title="Click to view full content">
              <i class="fas fa-eye"></i> View Details
            </button>`;
  }

  function showModal(rowIndex, columnIndex) {
    const rowData = rawData[rowIndex];
    if (!rowData) return;

    // Set modal title with row ID
    const rowId = rowData[0] || `Row ${rowIndex + 1}`;
    $("#detailModalLabel").text(`Details for ${rowId}`);

    // Populate modal content
    $("#modalErrorMessage").html(
      rowData[5] || '<em class="text-muted">No error message</em>',
    );
    $("#modalQueryParams").html(
      rowData[6] || '<em class="text-muted">No query parameters</em>',
    );
    $("#modalRequestBody").html(
      rowData[8] || '<em class="text-muted">No request body</em>',
    );
    $("#modalResponseHeaders").html(
      rowData[9] || '<em class="text-muted">No response headers</em>',
    );

    // Show the modal
    $("#detailModal").modal("show");
  }

  function renderColumnWithModal(rowIndex, columnIndex, cell) {
    // For modal columns, show truncated content + button if content exists
    if (cell && cell.trim()) {
      if (cell.trim() === "null") {
        return '<em class="text-muted">No data</em>';
      }
      const truncated = truncateContent(cell.replace(/<[^>]*>/g, ""), 50); // Remove HTML tags for truncation
      return `
              <div class="modal-cell-content">
                <div class="truncated-text">${truncated}</div>
                ${createViewButton(rowIndex, columnIndex)}
              </div>
            `;
    } else {
      return '<em class="text-muted">No data</em>';
    }
  }

  function rebuildTable(rows) {
    dt.destroy();
    $("#logTable").empty();

    // Rebuild table structure
    const newThead = $("<thead></thead>");
    newThead.append(
      "<tr>" + HEADERS.map((h) => `<th>${h}</th>`).join("") + "</tr>",
    );

    const newTbody = $("<tbody></tbody>");
    rows.forEach((row, rowIndex) => {
      const processedRow = row.map((cell, columnIndex) => {
        if (MODAL_COLUMNS.includes(columnIndex)) {
          if (cell && cell.trim()) {
            const truncated = truncateContent(cell.replace(/<[^>]*>/g, ""), 50);
            return `
                <div class="modal-cell-content">
                  <div class="truncated-text">${truncated}</div>
                  ${createViewButton(rowIndex, columnIndex)}
                </div>
              `;
          } else {
            return '<em class="text-muted">No data</em>';
          }
        }
        return cell;
      });

      newTbody.append(
        "<tr>" + processedRow.map((c) => `<td>${c}</td>`).join("") + "</tr>",
      );
    });

    $("#logTable").append(newThead).append(newTbody);
  }

  function renderTable(rows) {
    rawData = [...rows];

    if (dt) {
      rebuildTable(rows);
    }

    const thead = $("#logTable thead").empty();
    thead.append(
      "<tr>" + HEADERS.map((h) => `<th>${h}</th>`).join("") + "</tr>",
    );
    const tbody = $("#logTable tbody").empty();
    rows.forEach((row, rowIndex) => {
      const processedRow = row.map((cell, columnIndex) => {
        if (columnIndex === 0) {
          return truncateId(cell);
        } else if (MODAL_COLUMNS.includes(columnIndex)) {
          return renderColumnWithModal(rowIndex, columnIndex, cell);
        }
        return cell;
      });

      tbody.append(
        "<tr>" + processedRow.map((c) => `<td>${c}</td>`).join("") + "</tr>",
      );
    });

    dt = $("#logTable")
      .DataTable({
        paging: true,
        searching: true,
        info: true,
        ordering: true,
        scrollX: true,
        autoWidth: false,
        scrollY: "60vh", // optional: vertical height before scroll
        scrollCollapse: true,
        autoWidth: true, // let DataTables calculate widths
        fixedHeader: {
          // keeps header aligned & sticky on Y-scroll
          header: true,
          footer: false,
        },
        dom: '<"d-flex justify-content-between align-items-center mb-2"lf>rt<"d-flex justify-content-between align-items-center mt-2"ip>',
      })
      .columns.adjust();
    // Add click handler for view details buttons
    $("#logTable")
      .off("click", ".view-details-btn")
      .on("click", ".view-details-btn", function (e) {
        e.preventDefault();
        e.stopPropagation();

        const rowIndex = parseInt($(this).data("row"));
        const columnIndex = parseInt($(this).data("column"));

        showModal(rowIndex, columnIndex);
      });

    $("#tableSection").removeClass("d-none");
  }

  global.LogTable = { renderTable };
})(window, jQuery);
