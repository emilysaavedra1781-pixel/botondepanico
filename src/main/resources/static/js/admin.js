(() => {
    function parseMap(raw) {
        const data = {};
        if (!raw) return data;
        raw.replace(/^\{|\}$/g, '').split(',').forEach((pair) => {
            const index = pair.lastIndexOf('=');
            if (index <= 0) return;
            const key = pair.slice(0, index).trim();
            const value = Number(pair.slice(index + 1).trim());
            data[key] = Number.isNaN(value) ? 0 : value;
        });
        return data;
    }

    if (window.Chart) {
        document.querySelectorAll('[data-chart]').forEach((canvas) => {
            const data = parseMap(canvas.dataset.values || '');
            const labels = Object.keys(data).length ? Object.keys(data) : ['Sin datos'];
            const values = Object.keys(data).length ? Object.values(data) : [0];
            new Chart(canvas, {
                type: canvas.dataset.chart,
                data: {
                    labels,
                    datasets: [{
                        data: values,
                        backgroundColor: ['#dc2626', '#0f172a', '#2563eb', '#16a34a', '#d97706', '#7c3aed'],
                        borderWidth: 1
                    }]
                },
                options: { responsive: true, plugins: { legend: { position: 'bottom' } } }
            });
        });
    }

    const search = document.querySelector('[data-table-search]');
    const table = document.querySelector('[data-search-table]');
    if (search && table) {
        search.addEventListener('input', () => {
            const query = search.value.toLowerCase();
            table.querySelectorAll('tbody tr').forEach((row) => {
                row.style.display = row.textContent.toLowerCase().includes(query) ? '' : 'none';
            });
        });
    }

    function addTileLayer(map) {
        L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
            maxZoom: 19,
            attribution: '&copy; OpenStreetMap'
        }).addTo(map);
    }

    const adminMap = document.getElementById('adminMap');
    if (adminMap && window.L) {
        const singleLat = Number(adminMap.dataset.lat);
        const singleLng = Number(adminMap.dataset.lng);
        const map = L.map(adminMap).setView([Number.isNaN(singleLat) ? -12.0464 : singleLat, Number.isNaN(singleLng) ? -77.0428 : singleLng], 12);
        addTileLayer(map);
        const bounds = [];

        if (!Number.isNaN(singleLat) && !Number.isNaN(singleLng)) {
            L.marker([singleLat, singleLng]).addTo(map).bindPopup(adminMap.dataset.title || 'Emergencia').openPopup();
            bounds.push([singleLat, singleLng]);
        }

        document.querySelectorAll('.map-points [data-lat]').forEach((point) => {
            const lat = Number(point.dataset.lat);
            const lng = Number(point.dataset.lng);
            if (Number.isNaN(lat) || Number.isNaN(lng)) return;
            bounds.push([lat, lng]);
            L.circleMarker([lat, lng], {
                radius: 9,
                color: point.dataset.status === 'PENDIENTE' ? '#f97316' : '#dc2626',
                fillColor: point.dataset.status === 'PENDIENTE' ? '#f97316' : '#dc2626',
                fillOpacity: 0.85
            }).addTo(map).bindPopup(`#${point.dataset.id} ${point.dataset.title || 'Emergencia'}`);
        });
        if (bounds.length) map.fitBounds(bounds, { padding: [30, 30] });
    }

    const wsAlert = document.querySelector('[data-ws-alert]');
    if (wsAlert && window.WebSocket) {
        const protocol = location.protocol === 'https:' ? 'wss:' : 'ws:';
        const socket = new WebSocket(`${protocol}//${location.host}/ws/operador-alertas`);
        socket.onmessage = (event) => {
            try {
                const data = JSON.parse(event.data);
                wsAlert.textContent = `Nueva alerta #${data.id}`;
                wsAlert.classList.remove('red');
                wsAlert.classList.add('amber');
            } catch {
                wsAlert.textContent = 'Nueva alerta recibida';
            }
        };
    }
})();
