(function (global, $) {
  /** CONSTANTS **/
  /** Colors **/
  const BOOT_COLORS = [
    "#0d6efd",
    "#6610f2",
    "#6f42c1",
    "#d63384",
    "#dc3545",
    "#fd7e14",
    "#ffc107",
    "#198754",
    "#20c997",
    "#0dcaf0",
  ];

  const CHART_COLORS = [
    "#667eea",
    "#764ba2",
    "#f093fb",
    "#f5576c",
    "#4facfe",
    "#00f2fe",
    "#fa709a",
    "#fee140",
    "#ff9a9e",
    "#fecfef",
    "#a8edea",
    "#fed6e3",
  ];

  const SUCCESS_COLORS = ["#10b981", "#059669", "#047857"];
  const ERROR_COLORS = ["#ef4444", "#dc2626", "#b91c1c"];
  const WARNING_COLORS = ["#f59e0b", "#d97706", "#b45309"];

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
  const STATUS_IDX = 3;
  const CODE_IDX = 7;
  const ERROR_IDX = 4;
  const START_IDX = 1;
  const END_IDX = 2;
  const BODY_SIZE_IDX = 10;

  /** Globals **/
  let charts = {};
  let statusChart = null;
  let codeChart = null;

  function destroyChart(chartName) {
    if (charts[chartName]) {
      charts[chartName].destroy();
      delete charts[chartName];
    }
  }

  function makeDoughnut(id, dataObj, title, prev) {
    destroyChart(prev);
    if (!Object.keys(dataObj).length) return null;
    const ctx = document.getElementById(id).getContext("2d");
    const labels = Object.keys(dataObj);
    const data = labels.map((l) => dataObj[l]);
    const colors = labels.map((_, i) => BOOT_COLORS[i % BOOT_COLORS.length]);
    return new Chart(ctx, {
      type: "doughnut",
      data: { labels, datasets: [{ data, backgroundColor: colors }] },
      options: {
        responsive: true,
        plugins: {
          legend: { position: "bottom" },
          title: { display: true, text: title },
        },
      },
    });
  }

  function createChart(canvasId, config) {
    const ctx = document.getElementById(canvasId);
    if (!ctx) return null;
    return new Chart(ctx.getContext("2d"), config);
  }

  function updateStats(rows) {
    const totalRequests = rows.length;
    const successCount = rows.filter((r) => r[STATUS_IDX] === "SUCCESS").length;
    const errorCount = rows.filter((r) => r[STATUS_IDX] === "EXCEPTION").length;
    const successRate =
      totalRequests > 0 ? Math.round((successCount / totalRequests) * 100) : 0;

    // Calculate response times
    const responseTimes = rows
      .map((r) => {
        const start = new Date(r[START_IDX]);
        const end = new Date(r[END_IDX]);
        return end - start;
      })
      .filter((t) => !isNaN(t))
      .sort((a, b) => a - b);

    const maxResponseTime =
      responseTimes.length > 0 ? responseTimes[responseTimes.length - 1] : 0;
    const minResponseTime = responseTimes.length > 0 ? responseTimes[0] : 0;
    const avgResponseTime =
      responseTimes.length > 0
        ? Math.round(
            responseTimes.reduce((a, b) => a + b, 0) / responseTimes.length,
          )
        : 0;

    // Calculate body sizes
    const bodySizes = rows.map((r) => {
      const size = parseInt(r[BODY_SIZE_IDX]);
      return isNaN(size) ? 0 : size;
    });
    const avgBodySize =
      bodySizes.length > 0
        ? Math.round(
            (bodySizes.reduce((a, b) => a + b, 0) / bodySizes.length / 1024) *
              100,
          ) / 100
        : 0;

    // Calculate timespan
    const timestamps = rows
      .map((r) => new Date(r[START_IDX]))
      .filter((d) => !isNaN(d));
    const timespan =
      timestamps.length > 1
        ? Math.round((Math.max(...timestamps) - Math.min(...timestamps)) / 1000)
        : 0;

    // Update DOM
    $("#totalRequests").text(totalRequests.toLocaleString());
    $("#successRate").text(`${successRate}%`);
    $("#maxResponseTime").text(`${maxResponseTime}ms`);
    $("#minResponseTime").text(`${minResponseTime}ms`);
    $("#avgResponseTime").text(`${avgResponseTime}ms`);
    $("#errorCount").text(errorCount.toLocaleString());
    $("#avgBodySize").text(`${avgBodySize}KB`);
    $("#timespan").text(`${timespan}s`);

    $("#statsSection").removeClass("d-none");
  }

  function createStatusChart(statusCounts, rows) {
    destroyChart("statusChart");
    if (Object.keys(statusCounts).length) {
      charts.statusChart = createChart("statusChart", {
        type: "doughnut",
        data: {
          labels: Object.keys(statusCounts),
          datasets: [
            {
              data: Object.values(statusCounts),
              backgroundColor: Object.keys(statusCounts).map((status, i) =>
                status === "SUCCESS"
                  ? SUCCESS_COLORS[i % SUCCESS_COLORS.length]
                  : status === "EXCEPTION"
                    ? ERROR_COLORS[i % ERROR_COLORS.length]
                    : CHART_COLORS[i % CHART_COLORS.length],
              ),
              borderWidth: 0,
              hoverOffset: 8,
            },
          ],
        },
        options: {
          responsive: true,
          maintainAspectRatio: false,
          plugins: {
            legend: {
              position: "bottom",
              labels: {
                padding: 20,
                font: { size: 12 },
              },
            },
            tooltip: {
              callbacks: {
                label: function (context) {
                  const total = context.dataset.data.reduce((a, b) => a + b, 0);
                  const percentage = ((context.parsed * 100) / total).toFixed(
                    1,
                  );
                  return `${context.label}: ${context.parsed} (${percentage}%)`;
                },
              },
            },
          },
        },
      });
    }
  }

  function createCodesChart(codeCounts, rows) {
    destroyChart("codeChart");
    if (Object.keys(codeCounts).length) {
      charts.codeChart = createChart("codeChart", {
        type: "doughnut",
        data: {
          labels: Object.keys(codeCounts),
          datasets: [
            {
              data: Object.values(codeCounts),
              backgroundColor: Object.keys(codeCounts).map((code, i) =>
                code.toString().startsWith("2")
                  ? SUCCESS_COLORS[i % SUCCESS_COLORS.length]
                  : code.toString().startsWith("4") ||
                      code.toString().startsWith("5")
                    ? ERROR_COLORS[i % ERROR_COLORS.length]
                    : CHART_COLORS[i % CHART_COLORS.length],
              ),
              borderWidth: 0,
              hoverOffset: 8,
            },
          ],
        },
        options: {
          responsive: true,
          maintainAspectRatio: false,
          plugins: {
            legend: {
              position: "bottom",
              labels: {
                padding: 20,
                font: { size: 12 },
              },
            },
            tooltip: {
              callbacks: {
                label: function (context) {
                  const total = context.dataset.data.reduce((a, b) => a + b, 0);
                  const percentage = ((context.parsed * 100) / total).toFixed(
                    1,
                  );
                  return `Code ${context.label}: ${context.parsed} (${percentage}%)`;
                },
              },
            },
          },
        },
      });
    }
  }

  function createErrorTypesChart(errorTypes, rows) {
    destroyChart("errorChart");
    if (Object.keys(errorTypes).length) {
      charts.errorChart = createChart("errorChart", {
        type: "doughnut",
        data: {
          labels: Object.keys(errorTypes),
          datasets: [
            {
              data: Object.values(errorTypes),
              backgroundColor: Object.keys(errorTypes).map(
                (_, i) => ERROR_COLORS[i % ERROR_COLORS.length],
              ),
              borderWidth: 0,
              hoverOffset: 8,
            },
          ],
        },
        options: {
          responsive: true,
          maintainAspectRatio: false,
          plugins: {
            legend: {
              position: "bottom",
              labels: {
                padding: 20,
                font: { size: 12 },
              },
            },
            tooltip: {
              callbacks: {
                label: function (context) {
                  const total = context.dataset.data.reduce((a, b) => a + b, 0);
                  const percentage = ((context.parsed * 100) / total).toFixed(
                    1,
                  );
                  return `${context.label}: ${context.parsed} (${percentage}%)`;
                },
              },
            },
          },
        },
      });
    }
  }

  function createSizeDistributionChart(sizeRanges, rows) {
    destroyChart("sizeChart");
    const sizeData = Object.values(sizeRanges);
    const hasData = sizeData.some((val) => val > 0);

    if (hasData) {
      charts.sizeChart = createChart("sizeChart", {
        type: "bar",
        data: {
          labels: Object.keys(sizeRanges),
          datasets: [
            {
              label: "Request Count",
              data: sizeData,
              backgroundColor: CHART_COLORS.slice(0, 4),
              borderColor: CHART_COLORS.slice(0, 4).map((color) =>
                color.replace("0.8", "1"),
              ),
              borderWidth: 2,
              borderRadius: 8,
              borderSkipped: false,
            },
          ],
        },
        options: {
          responsive: true,
          maintainAspectRatio: false,
          scales: {
            x: {
              title: {
                display: true,
                text: "Response Size Range",
              },
              grid: {
                display: false,
              },
            },
            y: {
              title: {
                display: true,
                text: "Number of Requests",
              },
              beginAtZero: true,
              grid: {
                color: "rgba(0,0,0,0.1)",
              },
            },
          },
          plugins: {
            legend: {
              display: false,
            },
            tooltip: {
              callbacks: {
                label: function (context) {
                  const total = context.dataset.data.reduce((a, b) => a + b, 0);
                  const percentage = ((context.parsed.y * 100) / total).toFixed(
                    1,
                  );
                  return `Count: ${context.parsed.y} (${percentage}%)`;
                },
              },
            },
          },
        },
      });
    }
  }

  function createTimeDistributionChart(timeRanges, rows) {
    destroyChart("timeChart");
    const timeData = Object.values(timeRanges);
    const hasData = timeData.some((val) => val > 0);

    if (hasData) {
      charts.timeChart = createChart("timeChart", {
        type: "bar",
        data: {
          labels: Object.keys(timeRanges),
          datasets: [
            {
              label: "Request Count",
              data: timeData,
              backgroundColor: CHART_COLORS.slice(0, 4),
              borderColor: CHART_COLORS.slice(0, 4).map((color) =>
                color.replace("0.8", "1"),
              ),
              borderWidth: 2,
              borderRadius: 8,
              borderSkipped: false,
            },
          ],
        },
        options: {
          responsive: true,
          maintainAspectRatio: false,
          scales: {
            x: {
              title: {
                display: true,
                text: "Response Time Range",
              },
              grid: {
                display: false,
              },
            },
            y: {
              title: {
                display: true,
                text: "Number of Requests",
              },
              beginAtZero: true,
              grid: {
                color: "rgba(0,0,0,0.1)",
              },
            },
          },
          plugins: {
            legend: {
              display: false,
            },
            tooltip: {
              callbacks: {
                label: function (context) {
                  const total = context.dataset.data.reduce((a, b) => a + b, 0);
                  const percentage = ((context.parsed.y * 100) / total).toFixed(
                    1,
                  );
                  return `Count: ${context.parsed.y} (${percentage}%)`;
                },
              },
            },
          },
        },
      });
    }
  }

  function updateCharts(rows) {
    // Status Distribution
    const statusCounts = {};
    const codeCounts = {};
    const errorTypes = {};
    const timelineData = [];
    const sizeRanges = { "0-1KB": 0, "1-10KB": 0, "10-100KB": 0, "100KB+": 0 };
    const timeRanges = {
      "0-10ms": 0,
      "10-100ms": 0,
      "100-300ms": 0,
      "300ms+": 0,
    };

    const responseTimes = rows
      .map((r) => {
        const start = new Date(r[START_IDX]);
        const end = new Date(r[END_IDX]);
        return end - start;
      })
      .filter((t) => !isNaN(t));

    rows.forEach((r, idx) => {
      const status = r[STATUS_IDX] || "Unknown";
      statusCounts[status] = (statusCounts[status] || 0) + 1;

      const code = r[CODE_IDX];
      if (code && code !== "null" && code !== null) {
        codeCounts[code] = (codeCounts[code] || 0) + 1;
      }

      if (status === "EXCEPTION" && r[ERROR_IDX]) {
        const errorMsg = r[ERROR_IDX].toString();
        const errorType =
          errorMsg.split(" ")[0] || errorMsg.substring(0, 20) || "Unknown";
        errorTypes[errorType] = (errorTypes[errorType] || 0) + 1;
      }

      // Timeline data - response time over time
      const start = new Date(r[START_IDX]);
      const end = new Date(r[END_IDX]);
      const diffTime = end - start;
      if (!isNaN(start) && !isNaN(end)) {
        timelineData.push({
          x: start.getTime(),
          y: diffTime,
          timestamp: start,
        });
      }

      // Size distribution
      const bodySize = parseInt(r[BODY_SIZE_IDX]) || 0;
      if (bodySize === 0) return;
      if (bodySize < 1024) sizeRanges["0-1KB"]++;
      else if (bodySize < 10240) sizeRanges["1-10KB"]++;
      else if (bodySize < 102400) sizeRanges["10-100KB"]++;
      else sizeRanges["100KB+"]++;

      // Time distribution
      if (start === 0 || end === 0) return;
      if (diffTime < 10) timeRanges["0-10ms"]++;
      else if (diffTime < 100) timeRanges["10-100ms"]++;
      else if (diffTime < 300) timeRanges["100-300ms"]++;
      else timeRanges["300ms+"]++;
    });

    // Status Chart
    createStatusChart(statusCounts, rows);

    // Code Chart
    createCodesChart(codeCounts, rows);

    // Error Types Chart
    createErrorTypesChart(errorTypes, rows);

    // Size Distribution Chart
    createSizeDistributionChart(sizeRanges, rows);

    // Size Distribution Chart
    createTimeDistributionChart(timeRanges, rows);

    // Update stats as well
    updateStats(rows);

    $("#chartsSection").removeClass("d-none");
    $("#chartsSection").addClass("visible");
  }

  global.LogCharts = { updateCharts };
})(window, jQuery);
