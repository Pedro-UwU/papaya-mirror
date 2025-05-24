$(function () {
  function escapeHtml(text = "") {
    return text.replace(
      /[&<>"']/g,
      (ch) =>
        ({
          "&": "&amp;",
          "<": "&lt;",
          ">": "&gt;",
          '"': "&quot;",
          "'": "&#39;",
        })[ch],
    );
  }

  function prettyMaybeJson(val) {
    if (val === null || val === undefined) return "";
    if (typeof val === "object") {
      return `<pre>${escapeHtml(JSON.stringify(val, null, 2))}</pre>`;
    }
    if (typeof val === "string") {
      const trimmed = val.trim();
      if (
        (trimmed.startsWith("{") && trimmed.endsWith("}")) ||
        (trimmed.startsWith("[") && trimmed.endsWith("]"))
      ) {
        try {
          const obj = JSON.parse(trimmed);
          return `<pre>${escapeHtml(JSON.stringify(obj, null, 2))}</pre>`;
        } catch {
          /* not JSON */
        }
      }
      return escapeHtml(val);
    }
    return escapeHtml(String(val));
  }

  /** TSV parsing – skip header row only if it matches our fixed columns **/
  function parseTSV(text) {
    const lines = text.replace(/\r?\n$/, "").split(/\r?\n/);
    if (!lines.length) return [];
    // Check if first row looks like a header (starts with ID or matches HEADERS length)
    const firstCols = lines[0].split("\t");
    const hasHeader =
      firstCols[0].trim().toLowerCase() === "id" ||
      (firstCols.length === HEADERS.length &&
        firstCols.every(
          (c, i) => c.trim().toLowerCase() === HEADERS[i].toLowerCase(),
        ));
    const dataLines = hasHeader ? lines.slice(1) : lines;
    return dataLines.map((l) => l.split("\t"));
  }

  /** JSON parsing with nested extraction **/
  function parseJSON(raw) {
    let arr;
    try {
      arr = JSON.parse(raw);
    } catch (e) {
      alert("Invalid JSON");
      return [];
    }
    if (!Array.isArray(arr)) {
      alert("JSON must be an array");
      return [];
    }
    return arr.map((o) => [
      prettyMaybeJson(o.id ?? ""),
      prettyMaybeJson(o.execution?.start),
      prettyMaybeJson(o.execution?.end),
      prettyMaybeJson(o.execution?.status),
      prettyMaybeJson(o.execution?.errorMessage),
      prettyMaybeJson(o.request?.queryParams ?? {}),
      prettyMaybeJson(o.request?.body ?? {}),
      prettyMaybeJson(o.response?.code),
      prettyMaybeJson(o.response?.headers ?? {}),
      prettyMaybeJson(o.response?.body),
      prettyMaybeJson(o.response?.bodySize),
    ]);
  }

  $("#logFile").on("change", function (e) {
    const file = e.target.files[0];
    if (!file) return;

    // Show loading state
    const wrapper = $(".file-input-wrapper");
    const originalText = wrapper.html();
    wrapper.html('<i class="fas fa-spinner fa-spin"></i> Processing...');

    const reader = new FileReader();
    reader.onload = (evt) => {
      try {
        const content = evt.target.result;
        const rows = file.name.toLowerCase().endsWith(".json")
          ? parseJSON(content)
          : parseTSV(content);
        if (!rows.length) {
          wrapper.html(originalText);
          return;
        }

        LogTable.renderTable(rows);
        LogCharts.updateCharts(rows);
        // Success feedback
        wrapper.html('<i class="fas fa-check"></i> File Loaded Successfully!');
        setTimeout(() => {
          wrapper.html(originalText);
        }, 3000);
      } catch (error) {
        console.error("Error processing file:", error);
        wrapper.html(
          '<i class="fas fa-exclamation-triangle"></i> Error Processing File',
        );
        setTimeout(() => {
          wrapper.html(originalText);
        }, 4000);
      }
    };
    reader.onerror = () => {
      wrapper.html(
        '<i class="fas fa-exclamation-triangle"></i> Error Reading File',
      );
      setTimeout(() => {
        wrapper.html(originalText);
      }, 4000);
    };

    reader.readAsText(file);
  });
});
