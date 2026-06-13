(() => {
    const alertBox = document.querySelector('[data-ws-alert]');
    if (alertBox && window.WebSocket) {
        const protocol = location.protocol === 'https:' ? 'wss:' : 'ws:';
        const socket = new WebSocket(`${protocol}//${location.host}/ws/operador-alertas`);
        socket.onmessage = (event) => {
            try {
                const data = JSON.parse(event.data);
                alertBox.classList.remove('d-none');
                alertBox.textContent = `Nueva emergencia #${data.id}: ${data.emergencia || 'Sin tipo'} ${data.distrito ? '- ' + data.distrito : ''}`;
            } catch {
                alertBox.classList.remove('d-none');
                alertBox.textContent = 'Nueva emergencia recibida.';
            }
        };
    }

    function baseMap(element, lat, lng, title) {
        const map = L.map(element).setView([lat, lng], 14);
        L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
            maxZoom: 19,
            attribution: '&copy; OpenStreetMap'
        }).addTo(map);
        L.marker([lat, lng]).addTo(map).bindPopup(title || 'Emergencia').openPopup();
        return map;
    }

    const operatorMap = document.getElementById('operatorMap');
    if (operatorMap && window.L) {
        const singleLat = Number(operatorMap.dataset.lat);
        const singleLng = Number(operatorMap.dataset.lng);
        if (!Number.isNaN(singleLat) && !Number.isNaN(singleLng)) {
            baseMap(operatorMap, singleLat, singleLng, operatorMap.dataset.title);
        } else {
            const map = L.map(operatorMap).setView([-12.0464, -77.0428], 12);
            L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', { maxZoom: 19, attribution: '&copy; OpenStreetMap' }).addTo(map);
            const bounds = [];
            document.querySelectorAll('.map-points [data-lat]').forEach((point) => {
                const lat = Number(point.dataset.lat);
                const lng = Number(point.dataset.lng);
                if (Number.isNaN(lat) || Number.isNaN(lng)) return;
                bounds.push([lat, lng]);
                L.marker([lat, lng]).addTo(map).bindPopup(`#${point.dataset.id} ${point.dataset.title || 'Emergencia'}`);
            });
            if (bounds.length) map.fitBounds(bounds, { padding: [28, 28] });
        }
    }

    const trackingMap = document.getElementById('trackingMap');
    if (trackingMap && window.L) {
        const userLat = Number(trackingMap.dataset.lat || '-12.0464');
        const userLng = Number(trackingMap.dataset.lng || '-77.0428');
        const map = baseMap(trackingMap, userLat, userLng, 'Usuario');
        if (navigator.geolocation) {
            navigator.geolocation.watchPosition((position) => {
                const opLat = position.coords.latitude;
                const opLng = position.coords.longitude;
                L.marker([opLat, opLng]).addTo(map).bindPopup('Operador');
                L.polyline([[userLat, userLng], [opLat, opLng]], { color: '#dc2626', weight: 4 }).addTo(map);
                const distance = map.distance([userLat, userLng], [opLat, opLng]);
                const distanceEl = document.querySelector('[data-distance]');
                if (distanceEl) distanceEl.textContent = distance > 1000 ? `${(distance / 1000).toFixed(2)} km` : `${Math.round(distance)} m`;
            }, () => {}, { enableHighAccuracy: true, maximumAge: 5000 });
        }
    }

    const elapsed = document.querySelector('[data-elapsed]');
    if (elapsed && elapsed.dataset.start) {
        const start = new Date(elapsed.dataset.start);
        const update = () => {
            const diff = Math.max(0, Date.now() - start.getTime());
            const minutes = Math.floor(diff / 60000);
            elapsed.textContent = minutes < 60 ? `${minutes} min` : `${Math.floor(minutes / 60)} h ${minutes % 60} min`;
        };
        update();
        setInterval(update, 60000);
    }
})();
