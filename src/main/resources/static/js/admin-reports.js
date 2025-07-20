/* src/main/resources/static/js/admin-reports.js */
class ReportManager {
    constructor() {
        this.currentReportType = 'revenue';
        this.charts = {};
        this.initializeEventListeners();
        this.loadDefaultReport();
    }

    initializeEventListeners() {
        // Report type buttons
        document.querySelectorAll('[onclick^="showReportType"]').forEach(btn => {
            btn.addEventListener('click', (e) => {
                e.preventDefault();
                const reportType = e.target.closest('button').onclick.toString().match(/'([^']+)'/)[1];
                this.showReportType(reportType);
            });
        });

        // Generate report button
        const generateBtn = document.querySelector('[onclick="generateReport"]');
        if (generateBtn) {
            generateBtn.addEventListener('click', (e) => {
                e.preventDefault();
                this.generateReport();
            });
        }

        // Export button
        const exportBtn = document.querySelector('[onclick="exportReport"]');
        if (exportBtn) {
            exportBtn.addEventListener('click', (e) => {
                e.preventDefault();
                this.exportReport();
            });
        }
    }

    showReportType(type) {
        // Update active button
        document.querySelectorAll('.btn-group .btn').forEach(btn => {
            btn.classList.remove('active');
        });

        // Find and activate the correct button
        const buttons = document.querySelectorAll('.btn-group .btn');
        buttons.forEach(btn => {
            if (btn.onclick && btn.onclick.toString().includes(`'${type}'`)) {
                btn.classList.add('active');
            }
        });

        this.currentReportType = type;

        // Show/hide analytics cards
        const analyticsCards = document.getElementById('analytics-cards');
        if (type === 'analytics') {
            analyticsCards.style.display = 'block';
        } else {
            analyticsCards.style.display = 'none';
        }

        this.loadReportData(type);
    }

    async loadReportData(type) {
        try {
            this.showLoading();

            const period = document.getElementById('report-period').value;
            const startDate = document.getElementById('report-date-from').value;
            const endDate = document.getElementById('report-date-to').value;

            let url = '';
            let params = new URLSearchParams();

            switch (type) {
                case 'revenue':
                    url = '/admin/reports/api/revenue';
                    params.append('type', period);
                    if (startDate) params.append('startDate', startDate);
                    if (endDate) params.append('endDate', endDate);
                    if (period === 'quarterly' || period === 'yearly') {
                        params.append('year', new Date().getFullYear());
                    }
                    break;

                case 'products':
                    url = '/admin/reports/api/products';
                    params.append('startDate', startDate || this.getDefaultStartDate());
                    params.append('endDate', endDate || this.getDefaultEndDate());
                    params.append('limit', '20');
                    break;

                case 'analytics':
                    url = '/admin/reports/api/user-analytics';
                    params.append('startDate', startDate || this.getDefaultStartDate());
                    params.append('endDate', endDate || this.getDefaultEndDate());
                    break;
            }

            const response = await fetch(`${url}?${params.toString()}`);
            const data = await response.json();

            this.hideLoading();

            if (data.success) {
                this.renderReportData(type, data);
                this.updateReportChart(type, data);
                this.updateReportSummary(type, data);

                if (type === 'analytics') {
                    this.updateAnalyticsCards(data);
                }
            } else {
                this.showError('Không thể tải dữ liệu báo cáo: ' + data.message);
            }

        } catch (error) {
            this.hideLoading();
            console.error('Error loading report data:', error);
            this.showError('Lỗi khi tải dữ liệu báo cáo');
        }
    }

    renderReportData(type, data) {
        const tableHead = document.getElementById('report-table-head');
        const tableBody = document.getElementById('report-table-body');

        // Clear existing content
        tableHead.innerHTML = '';
        tableBody.innerHTML = '';

        switch (type) {
            case 'revenue':
                this.renderRevenueTable(data.data, tableHead, tableBody);
                break;
            case 'products':
                this.renderProductsTable(data.topProducts, tableHead, tableBody);
                break;
            case 'analytics':
                this.renderAnalyticsTable(data, tableHead, tableBody);
                break;
        }
    }

    renderRevenueTable(data, tableHead, tableBody) {
        // Create table headers
        const headerRow = document.createElement('tr');
        const headers = ['Thời gian', 'Doanh thu giao dịch', 'Hoa hồng đại lý', 'Phí subscription', 'Chi phí vận hành', 'Lợi nhuận ròng'];

        headers.forEach(header => {
            const th = document.createElement('th');
            th.textContent = header;
            headerRow.appendChild(th);
        });
        tableHead.appendChild(headerRow);

        // Create table rows
        if (data && data.length > 0) {
            data.forEach(report => {
                const row = document.createElement('tr');
                row.innerHTML = `
                    <td>${this.formatDate(report.reportDate)}</td>
                    <td>${this.formatCurrency(report.totalTransactionRevenue)}</td>
                    <td>${this.formatCurrency(report.agencyCommissionRevenue)}</td>
                    <td>${this.formatCurrency(report.subscriptionRevenue)}</td>
                    <td>${this.formatCurrency(report.operatingCosts)}</td>
                    <td class="${report.netProfit >= 0 ? 'text-success' : 'text-danger'}">
                        ${this.formatCurrency(report.netProfit)}
                    </td>
                `;
                tableBody.appendChild(row);
            });
        } else {
            const row = document.createElement('tr');
            row.innerHTML = `<td colspan="6" class="text-center text-muted">Không có dữ liệu trong khoảng thời gian này</td>`;
            tableBody.appendChild(row);
        }
    }

    renderProductsTable(data, tableHead, tableBody) {
        const headerRow = document.createElement('tr');
        const headers = ['Sản phẩm', 'Danh mục', 'Lượt xem', 'Đơn hàng', 'Doanh thu', 'Tỷ lệ chuyển đổi'];

        headers.forEach(header => {
            const th = document.createElement('th');
            th.textContent = header;
            headerRow.appendChild(th);
        });
        tableHead.appendChild(headerRow);

        if (data && data.length > 0) {
            data.forEach((product, index) => {
                const row = document.createElement('tr');
                row.innerHTML = `
                   <td>
                       <div class="d-flex align-items-center">
                           <span class="badge bg-primary me-2">#${index + 1}</span>
                           ${product.productName}
                       </div>
                   </td>
                   <td><span class="badge bg-secondary">${product.categoryName}</span></td>
                   <td>${product.viewCount.toLocaleString()}</td>
                   <td>${product.orderCount}</td>
                   <td>${this.formatCurrency(product.revenue)}</td>
                   <td>${product.conversionRate.toFixed(2)}%</td>
               `;
                tableBody.appendChild(row);
            });
        } else {
            const row = document.createElement('tr');
            row.innerHTML = `<td colspan="6" class="text-center text-muted">Không có dữ liệu sản phẩm</td>`;
            tableBody.appendChild(row);
        }
    }

    renderAnalyticsTable(data, tableHead, tableBody) {
        const headerRow = document.createElement('tr');
        const headers = ['Chỉ số', 'Giá trị', 'Thay đổi'];

        headers.forEach(header => {
            const th = document.createElement('th');
            th.textContent = header;
            headerRow.appendChild(th);
        });
        tableHead.appendChild(headerRow);

        const metrics = [
            { name: 'Tổng số session', value: data.engagement?.totalSessions || 0, change: '+12%' },
            { name: 'Tổng lượt xem trang', value: data.engagement?.totalPageViews || 0, change: '+8%' },
            { name: 'Thời gian session trung bình', value: (data.engagement?.averageSessionDuration || 0) + ' phút', change: '+5%' },
            { name: 'Tỷ lệ bounce', value: (data.engagement?.bounceRate || 0) + '%', change: '-3%' },
            { name: 'Tỷ lệ chuyển đổi', value: (data.engagement?.conversionRate || 0) + '%', change: '+15%' }
        ];

        metrics.forEach(metric => {
            const row = document.createElement('tr');
            const changeClass = metric.change.startsWith('+') ? 'text-success' : 'text-danger';
            row.innerHTML = `
               <td><strong>${metric.name}</strong></td>
               <td>${metric.value}</td>
               <td class="${changeClass}"><i class="fas fa-${metric.change.startsWith('+') ? 'arrow-up' : 'arrow-down'}"></i> ${metric.change}</td>
           `;
            tableBody.appendChild(row);
        });
    }

    updateReportChart(type, data) {
        const ctx = document.getElementById('reportChart').getContext('2d');
        const chartTitle = document.getElementById('report-chart-title');

        // Destroy existing chart if exists
        if (this.charts.reportChart) {
            this.charts.reportChart.destroy();
        }

        let chartConfig = {};

        switch (type) {
            case 'revenue':
                chartTitle.textContent = 'Biểu đồ doanh thu theo thời gian';
                chartConfig = this.createRevenueChartConfig(data.data);
                break;
            case 'products':
                chartTitle.textContent = 'Top 10 sản phẩm bán chạy nhất';
                chartConfig = this.createProductsChartConfig(data.topProducts);
                break;
            case 'analytics':
                chartTitle.textContent = 'Phân tích thiết bị người dùng';
                chartConfig = this.createAnalyticsChartConfig(data);
                break;
        }

        this.charts.reportChart = new Chart(ctx, chartConfig);
    }

    createRevenueChartConfig(data) {
        return {
            type: 'line',
            data: {
                labels: data ? data.map(item => this.formatDate(item.reportDate)) : [],
                datasets: [
                    {
                        label: 'Tổng doanh thu',
                        data: data ? data.map(item => item.totalTransactionRevenue + item.agencyCommissionRevenue + item.subscriptionRevenue) : [],
                        borderColor: '#667eea',
                        backgroundColor: 'rgba(102, 126, 234, 0.1)',
                        tension: 0.4,
                        fill: true
                    },
                    {
                        label: 'Lợi nhuận ròng',
                        data: data ? data.map(item => item.netProfit) : [],
                        borderColor: '#f093fb',
                        backgroundColor: 'rgba(240, 147, 251, 0.1)',
                        tension: 0.4,
                        fill: true
                    }
                ]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: {
                        position: 'top',
                    },
                    tooltip: {
                        callbacks: {
                            label: function(context) {
                                return context.dataset.label + ': ' +
                                    new Intl.NumberFormat('vi-VN', {
                                        style: 'currency',
                                        currency: 'VND'
                                    }).format(context.parsed.y);
                            }
                        }
                    }
                },
                scales: {
                    y: {
                        beginAtZero: true,
                        ticks: {
                            callback: function(value) {
                                return new Intl.NumberFormat('vi-VN', {
                                    style: 'currency',
                                    currency: 'VND',
                                    notation: 'compact'
                                }).format(value);
                            }
                        }
                    }
                }
            }
        };
    }

    createProductsChartConfig(data) {
        const chartData = data ? data.slice(0, 10) : [];
        return {
            type: 'bar',
            data: {
                labels: chartData.map(item => item.productName.length > 20 ? item.productName.substring(0, 20) + '...' : item.productName),
                datasets: [{
                    label: 'Số đơn hàng',
                    data: chartData.map(item => item.orderCount),
                    backgroundColor: 'rgba(102, 126, 234, 0.8)',
                    borderColor: '#667eea',
                    borderWidth: 1
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: {
                        display: false
                    }
                },
                scales: {
                    y: {
                        beginAtZero: true
                    }
                }
            }
        };
    }

    createAnalyticsChartConfig(data) {
        const devices = data.devices || { 'Mobile': 60, 'Desktop': 30, 'Tablet': 10 };
        return {
            type: 'doughnut',
            data: {
                labels: Object.keys(devices),
                datasets: [{
                    data: Object.values(devices),
                    backgroundColor: [
                        '#667eea',
                        '#f093fb',
                        '#4facfe',
                        '#43e97b'
                    ]
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: {
                        position: 'bottom'
                    }
                }
            }
        };
    }

    updateReportSummary(type, data) {
        const summaryContainer = document.getElementById('report-summary');
        summaryContainer.innerHTML = '';

        let summaryContent = '';

        switch (type) {
            case 'revenue':
                if (data.data && data.data.length > 0) {
                    const totalRevenue = data.data.reduce((sum, item) =>
                        sum + item.totalTransactionRevenue + item.agencyCommissionRevenue + item.subscriptionRevenue, 0);
                    const totalProfit = data.data.reduce((sum, item) => sum + item.netProfit, 0);

                    summaryContent = `
                       <div class="summary-card mb-3">
                           <h6 class="text-muted">Tổng doanh thu</h6>
                           <h4 class="text-primary">${this.formatCurrency(totalRevenue)}</h4>
                       </div>
                       <div class="summary-card mb-3">
                           <h6 class="text-muted">Tổng lợi nhuận</h6>
                           <h4 class="text-success">${this.formatCurrency(totalProfit)}</h4>
                       </div>
                       <div class="summary-card mb-3">
                           <h6 class="text-muted">Tỷ suất lợi nhuận</h6>
                           <h4 class="text-info">${totalRevenue > 0 ? ((totalProfit / totalRevenue) * 100).toFixed(1) : '0.0'}%</h4>
                       </div>
                   `;
                } else {
                    summaryContent = `
                       <div class="text-center text-muted">
                           <i class="fas fa-chart-line fa-3x mb-3"></i>
                           <p>Không có dữ liệu doanh thu</p>
                       </div>
                   `;
                }
                break;

            case 'products':
                const totalProducts = data.topProducts?.length || 0;
                const totalOrders = data.topProducts?.reduce((sum, item) => sum + item.orderCount, 0) || 0;
                const totalProductRevenue = data.topProducts?.reduce((sum, item) => sum + parseFloat(item.revenue), 0) || 0;

                summaryContent = `
                   <div class="summary-card mb-3">
                       <h6 class="text-muted">Sản phẩm đang bán</h6>
                       <h4 class="text-primary">${totalProducts.toLocaleString()}</h4>
                   </div>
                   <div class="summary-card mb-3">
                       <h6 class="text-muted">Tổng đơn hàng</h6>
                       <h4 class="text-success">${totalOrders.toLocaleString()}</h4>
                   </div>
                   <div class="summary-card mb-3">
                       <h6 class="text-muted">Doanh thu sản phẩm</h6>
                       <h4 class="text-info">${this.formatCurrency(totalProductRevenue)}</h4>
                   </div>
               `;
                break;

            case 'analytics':
                summaryContent = `
                   <div class="summary-card mb-3">
                       <h6 class="text-muted">Tổng sessions</h6>
                       <h4 class="text-primary">${(data.engagement?.totalSessions || 0).toLocaleString()}</h4>
                   </div>
                   <div class="summary-card mb-3">
                       <h6 class="text-muted">Tỷ lệ chuyển đổi</h6>
                       <h4 class="text-success">${(data.engagement?.conversionRate || 0).toFixed(2)}%</h4>
                   </div>
                   <div class="summary-card mb-3">
                       <h6 class="text-muted">Thời gian trung bình</h6>
                       <h4 class="text-info">${(data.engagement?.averageSessionDuration || 0)} phút</h4>
                   </div>
               `;
                break;
        }

        summaryContainer.innerHTML = summaryContent;
    }

    updateAnalyticsCards(data) {
        document.getElementById('total-page-views').textContent = (data.engagement?.totalPageViews || 0).toLocaleString();
        document.getElementById('total-sessions').textContent = (data.engagement?.totalSessions || 0).toLocaleString();
        document.getElementById('bounce-rate').textContent = (data.engagement?.bounceRate || 0) + '%';
        document.getElementById('conversion-rate').textContent = (data.engagement?.conversionRate || 0).toFixed(2) + '%';
    }

    async generateReport() {
        const reportPeriod = document.getElementById('report-period').value;
        const startDate = document.getElementById('report-date-from').value;
        const endDate = document.getElementById('report-date-to').value;

        if (!startDate || !endDate) {
            this.showError('Vui lòng chọn khoảng thời gian');
            return;
        }

        if (new Date(startDate) > new Date(endDate)) {
            this.showError('Ngày bắt đầu không thể lớn hơn ngày kết thúc');
            return;
        }

        await this.loadReportData(this.currentReportType);
    }

    async exportReport() {
        try {
            const requestData = {
                reportType: document.getElementById('report-period').value,
                startDate: document.getElementById('report-date-from').value,
                endDate: document.getElementById('report-date-to').value
            };

            if (!requestData.startDate || !requestData.endDate) {
                this.showError('Vui lòng chọn khoảng thời gian trước khi xuất báo cáo');
                return;
            }

            this.showLoading();

            let exportUrl = '/admin/reports/api/export/';
            if (this.currentReportType === 'revenue') {
                exportUrl += 'revenue';
            } else {
                exportUrl += 'user-analytics';
            }

            const response = await fetch(exportUrl, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(requestData)
            });

            this.hideLoading();

            if (response.ok) {
                const blob = await response.blob();
                const url = window.URL.createObjectURL(blob);
                const a = document.createElement('a');
                a.style.display = 'none';
                a.href = url;

                const contentDisposition = response.headers.get('content-disposition');
                const filename = contentDisposition
                    ? contentDisposition.split('filename=')[1]
                    : `report_${this.currentReportType}_${Date.now()}.xlsx`;

                a.download = filename;
                document.body.appendChild(a);
                a.click();
                window.URL.revokeObjectURL(url);
                document.body.removeChild(a);

                this.showSuccess('Xuất báo cáo thành công');
            } else {
                throw new Error('Export failed');
            }

        } catch (error) {
            this.hideLoading();
            console.error('Error exporting report:', error);
            this.showError('Không thể xuất báo cáo');
        }
    }

    loadDefaultReport() {
        // Set default dates
        const today = new Date();
        const thirtyDaysAgo = new Date(today.getTime() - (30 * 24 * 60 * 60 * 1000));

        document.getElementById('report-date-from').value = thirtyDaysAgo.toISOString().split('T')[0];
        document.getElementById('report-date-to').value = today.toISOString().split('T')[0];

        // Load default revenue report
        this.loadReportData('revenue');
    }

    getDefaultStartDate() {
        const thirtyDaysAgo = new Date();
        thirtyDaysAgo.setDate(thirtyDaysAgo.getDate() - 30);
        return thirtyDaysAgo.toISOString().split('T')[0];
    }

    getDefaultEndDate() {
        return new Date().toISOString().split('T')[0];
    }

    formatDate(dateString) {
        const date = new Date(dateString);
        return date.toLocaleDateString('vi-VN');
    }

    formatCurrency(amount) {
        return new Intl.NumberFormat('vi-VN', {
            style: 'currency',
            currency: 'VND'
        }).format(amount);
    }

    showLoading() {
        const tableBody = document.getElementById('report-table-body');
        tableBody.innerHTML = `
           <tr>
               <td colspan="6" class="text-center">
                   <div class="loading">
                       <i class="fas fa-spinner fa-spin fa-2x"></i>
                       <p class="mt-2">Đang tải dữ liệu...</p>
                   </div>
               </td>
           </tr>
       `;
    }

    hideLoading() {
        // Loading will be hidden when new data is rendered
    }

    showError(message) {
        // Create toast notification
        this.showToast('error', message);
    }

    showSuccess(message) {
        // Create toast notification
        this.showToast('success', message);
    }

    showToast(type, message) {
        // Create toast container if not exists
        let toastContainer = document.querySelector('.toast-container');
        if (!toastContainer) {
            toastContainer = document.createElement('div');
            toastContainer.className = 'toast-container position-fixed top-0 end-0 p-3';
            document.body.appendChild(toastContainer);
        }

        // Create toast
        const toast = document.createElement('div');
        toast.className = `toast align-items-center text-white bg-${type === 'error' ? 'danger' : 'success'} border-0`;
        toast.setAttribute('role', 'alert');
        toast.innerHTML = `
           <div class="d-flex">
               <div class="toast-body">
                   <i class="fas fa-${type === 'error' ? 'exclamation-circle' : 'check-circle'} me-2"></i>
                   ${message}
               </div>
               <button type="button" class="btn-close btn-close-white me-2 m-auto" data-bs-dismiss="toast"></button>
           </div>
       `;

        toastContainer.appendChild(toast);

        // Initialize and show toast
        const bsToast = new bootstrap.Toast(toast);
        bsToast.show();

        // Remove toast after hide
        toast.addEventListener('hidden.bs.toast', function () {
            toast.remove();
        });
    }
}

// Initialize when DOM is loaded
document.addEventListener('DOMContentLoaded', function() {
    window.reportManager = new ReportManager();
});

// Global functions for onclick handlers (backward compatibility)
function showReportType(type) {
    if (window.reportManager) {
        window.reportManager.showReportType(type);
    }
}

function generateReport() {
    if (window.reportManager) {
        window.reportManager.generateReport();
    }
}

function exportReport() {
    if (window.reportManager) {
        window.reportManager.exportReport();
    }
}