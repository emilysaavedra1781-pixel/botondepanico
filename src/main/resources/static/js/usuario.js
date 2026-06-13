(() => {
    const isSecure = window.isSecureContext || location.hostname === 'localhost';
    const permissionKey = 'botonPanico.permissionsReady';
    const dashboard = document.querySelector('[data-user-dashboard]');
    const locationText = document.querySelector('[data-location-text]');
    const latInput = document.querySelector('[name="latitud"]');
    const lngInput = document.querySelector('[name="longitud"]');
    const addressInput = document.querySelector('[name="direccion"]');
    let sharedStream = null;
    let lastPosition = null;
    let locationWatchId = null;

    function secureMessage(feature) {
        return `${feature} requiere abrir la app por HTTPS. Usa https://IP-DE-TU-PC:8443 y acepta el certificado local.`;
    }

    function setLocation(lat, lng) {
        lastPosition = { lat, lng, address: `Lat ${lat}, Lng ${lng}` };
        if (latInput) latInput.value = lat;
        if (lngInput) lngInput.value = lng;
        if (addressInput) addressInput.value = lastPosition.address;
        if (locationText) locationText.textContent = `Latitud ${lat}, Longitud ${lng}`;
    }

    function setStatus(element, message) {
        if (element) element.textContent = message;
    }

    function getPositionOnce() {
        return new Promise((resolve) => {
            if (!navigator.geolocation || !isSecure) {
                resolve(null);
                return;
            }
            navigator.geolocation.getCurrentPosition(
                (position) => {
                    const lat = position.coords.latitude.toFixed(6);
                    const lng = position.coords.longitude.toFixed(6);
                    setLocation(lat, lng);
                    resolve(lastPosition);
                },
                () => resolve(lastPosition),
                { enableHighAccuracy: true, timeout: 12000, maximumAge: 3000 }
            );
        });
    }

    function startLocationWatch() {
        if (!navigator.geolocation || !isSecure || locationWatchId !== null) return;
        locationWatchId = navigator.geolocation.watchPosition(
            (position) => setLocation(position.coords.latitude.toFixed(6), position.coords.longitude.toFixed(6)),
            () => {},
            { enableHighAccuracy: true, maximumAge: 0, timeout: 30000 }
        );
    }

    function stopLocationWatch() {
        if (locationWatchId !== null && navigator.geolocation) {
            navigator.geolocation.clearWatch(locationWatchId);
        }
        locationWatchId = null;
    }

    async function ensureMediaStream() {
        if (sharedStream && sharedStream.active) return sharedStream;
        if (!navigator.mediaDevices || !navigator.mediaDevices.getUserMedia) {
            throw new Error('Tu navegador no permite usar camara o microfono.');
        }
        if (!isSecure) {
            throw new Error(secureMessage('La camara y el microfono'));
        }
        sharedStream = await navigator.mediaDevices.getUserMedia({
            audio: true,
            video: { facingMode: { ideal: 'environment' } }
        });
        return sharedStream;
    }

    function bindStream(video) {
        if (video && sharedStream && video.srcObject !== sharedStream) {
            video.srcObject = sharedStream;
        }
    }

    async function requestAllPermissions() {
        await getPositionOnce();
        await ensureMediaStream();
        localStorage.setItem(permissionKey, 'true');
        document.querySelectorAll('video[data-photo-video], video[data-video-preview], video[data-post-camera]').forEach(bindStream);
        return true;
    }

    function initPermissionModal() {
        if (!dashboard) return;
        const modalElement = document.getElementById('permissionModal');
        const button = document.querySelector('[data-request-permissions]');
        const status = document.querySelector('[data-permission-status]');
        if (!modalElement || !button) return;

        const modal = bootstrap.Modal.getOrCreateInstance(modalElement);
        if (!localStorage.getItem(permissionKey)) modal.show();
        else requestAllPermissions().catch(() => localStorage.removeItem(permissionKey));

        button.addEventListener('click', async () => {
            try {
                setStatus(status, 'Solicitando permisos del navegador...');
                await requestAllPermissions();
                setStatus(status, 'Permisos activados.');
                modal.hide();
            } catch (error) {
                localStorage.removeItem(permissionKey);
                setStatus(status, error.message || 'No se pudieron activar los permisos.');
            }
        });
    }

    document.querySelector('[data-detect-location]')?.addEventListener('click', () => getPositionOnce());
    initPermissionModal();

    const sosButton = document.querySelector('[data-sos-button]');
    const sosStatus = document.querySelector('[data-sos-status]');
    let sosRecorder = null;
    let sosChunks = [];
    let sosRecognition = null;
    let sosTranscript = '';
    let sosRecording = false;
    let sosPointerId = null;
    let currentSosCaseId = null;

    function supportedAudioOptions() {
        if (!window.MediaRecorder) return undefined;
        const types = ['audio/webm;codecs=opus', 'audio/webm', 'audio/mp4'];
        const type = types.find((candidate) => MediaRecorder.isTypeSupported(candidate));
        return type ? { mimeType: type } : undefined;
    }

    function audioExtension(contentType) {
        return contentType && contentType.includes('mp4') ? 'm4a' : 'webm';
    }

    function createSpeechRecognition() {
        const SpeechRecognition = window.SpeechRecognition || window.webkitSpeechRecognition;
        if (!SpeechRecognition) return null;
        const recognition = new SpeechRecognition();
        recognition.lang = 'es-PE';
        recognition.continuous = true;
        recognition.interimResults = true;
        recognition.onresult = (event) => {
            let text = '';
            for (let i = 0; i < event.results.length; i++) {
                text += event.results[i][0].transcript + ' ';
            }
            sosTranscript = text.trim();
            if (sosTranscript) setStatus(sosStatus, `Grabando y transcribiendo: ${sosTranscript}`);
        };
        recognition.onerror = () => {
            if (!sosTranscript) setStatus(sosStatus, 'Grabando audio. Transcripcion no disponible en este navegador.');
        };
        return recognition;
    }

    function stopRecorder(recorder) {
        return new Promise((resolve) => {
            if (!recorder || recorder.state === 'inactive') {
                resolve();
                return;
            }
            recorder.addEventListener('stop', resolve, { once: true });
            recorder.stop();
        });
    }

    async function uploadEvidenceJson(file, type, description, position, options = {}) {
        const data = new FormData();
        data.set('archivo', file);
        data.set('tipo', type);
        data.set('descripcion', description || '');
        data.set('latitud', position?.lat || lastPosition?.lat || '0');
        data.set('longitud', position?.lng || lastPosition?.lng || '0');
        data.set('direccion', position?.address || lastPosition?.address || 'Ubicacion no registrada');
        if (options.emergenciaId) data.set('emergenciaId', options.emergenciaId);
        if (options.nuevoCaso) data.set('nuevoCaso', 'true');
        const response = await fetch('/usuario/subir-evidencia-json', {
            method: 'POST',
            body: data,
            credentials: 'same-origin'
        });
        if (!response.ok) {
            const body = await response.json().catch(() => ({}));
            throw new Error(body.error || 'No se pudo enviar la evidencia.');
        }
        return response.json();
    }

    async function startSosAudio(event) {
        if (!sosButton || sosRecording) return;
        event.preventDefault();
        sosPointerId = event.pointerId;
        sosButton.setPointerCapture?.(event.pointerId);
        try {
            await requestAllPermissions();
            if (!window.MediaRecorder) throw new Error('Tu navegador no permite grabar audio.');
            sosChunks = [];
            sosTranscript = '';
            startLocationWatch();
            const audioOnlyStream = new MediaStream(sharedStream.getAudioTracks());
            sosRecorder = new MediaRecorder(audioOnlyStream, supportedAudioOptions());
            sosRecorder.ondataavailable = (recordEvent) => {
                if (recordEvent.data && recordEvent.data.size > 0) sosChunks.push(recordEvent.data);
            };
            sosRecorder.start();
            sosRecording = true;
            sosButton.classList.add('recording');
            setStatus(sosStatus, 'Grabando audio y ubicacion en tiempo real...');
            sosRecognition = createSpeechRecognition();
            try { sosRecognition?.start(); } catch { sosRecognition = null; }
        } catch (error) {
            setStatus(sosStatus, error.message || 'No se pudo iniciar SOS.');
        }
    }

    async function stopSosAudio(event) {
        if (!sosButton || !sosRecording) return;
        if (event && sosPointerId !== null && event.pointerId !== sosPointerId) return;
        event?.preventDefault();
        sosRecording = false;
        sosPointerId = null;
        sosButton.classList.remove('recording');
        setStatus(sosStatus, 'Procesando audio, transcripcion y ubicacion...');
        try { sosRecognition?.stop(); } catch {}
        await stopRecorder(sosRecorder);
        stopLocationWatch();

        const contentType = sosRecorder?.mimeType || 'audio/webm';
        const audioBlob = new Blob(sosChunks, { type: contentType });
        if (!audioBlob.size) {
            setStatus(sosStatus, 'No se capturo audio. Mantén presionado un poco mas.');
            return;
        }
        const position = await getPositionOnce();
        const description = sosTranscript || 'Audio SOS enviado sin transcripcion automatica.';
        const file = new File([audioBlob], `audio_sos.${audioExtension(contentType)}`, { type: contentType });
        try {
            const result = await uploadEvidenceJson(file, 'AUDIO', description, position, { nuevoCaso: true });
            currentSosCaseId = result.emergenciaId;
            setStatus(sosStatus, 'Audio SOS enviado. Puedes adjuntar foto o video.');
            showPostSosModal();
        } catch (error) {
            setStatus(sosStatus, error.message || 'No se pudo enviar el audio SOS.');
        }
    }

    if (sosButton) {
        sosButton.addEventListener('pointerdown', startSosAudio);
        sosButton.addEventListener('pointerup', stopSosAudio);
        sosButton.addEventListener('pointercancel', stopSosAudio);
        sosButton.addEventListener('contextmenu', (event) => event.preventDefault());
    }

    const postModalElement = document.getElementById('postSosModal');
    const postCamera = document.querySelector('[data-post-camera]');
    const postCanvas = document.querySelector('[data-post-canvas]');
    const postRecord = document.querySelector('[data-post-record]');
    const postPhoto = document.querySelector('[data-post-photo]');
    const postSend = document.querySelector('[data-send-post-evidence]');
    const postSkip = document.querySelector('[data-skip-post-evidence]');
    const postDescription = document.querySelector('[data-post-description]');
    const postPreview = document.querySelector('[data-post-preview]');
    const postStatus = document.querySelector('[data-post-status]');
    let postRecorder = null;
    let postChunks = [];
    let postFile = null;
    let postFileType = null;

    async function waitForVideo(videoElement) {
        if (!videoElement) throw new Error('No se encontro la vista de camara.');
        if (videoElement.readyState >= 2 && videoElement.videoWidth > 0) return;
        await new Promise((resolve, reject) => {
            const timeout = setTimeout(() => reject(new Error('La camara aun no esta lista.')), 8000);
            videoElement.addEventListener('loadedmetadata', () => {
                clearTimeout(timeout);
                resolve();
            }, { once: true });
        });
    }

    function canvasToBlob(canvas, type = 'image/jpeg', quality = 0.9) {
        return new Promise((resolve) => canvas.toBlob(resolve, type, quality));
    }

    async function capturePhoto(videoElement, canvas) {
        await ensureMediaStream();
        bindStream(videoElement);
        await waitForVideo(videoElement);
        const width = videoElement.videoWidth || 1280;
        const height = videoElement.videoHeight || 720;
        canvas.width = width;
        canvas.height = height;
        canvas.getContext('2d').drawImage(videoElement, 0, 0, width, height);
        const blob = await canvasToBlob(canvas);
        if (!blob) throw new Error('No se pudo capturar la foto.');
        return new File([blob], 'evidencia_foto.jpg', { type: 'image/jpeg' });
    }

    function setPostFile(file, type) {
        postFile = file;
        postFileType = type;
        if (postSend) postSend.disabled = false;
        if (!postPreview) return;
        const url = URL.createObjectURL(file);
        postPreview.innerHTML = type === 'FOTO'
            ? `<img src="${url}" alt="Foto capturada">`
            : `<video src="${url}" controls></video>`;
    }

    async function showPostSosModal() {
        if (!postModalElement) {
            window.location.href = '/usuario/evidencias';
            return;
        }
        const modal = bootstrap.Modal.getOrCreateInstance(postModalElement);
        modal.show();
        try {
            await ensureMediaStream();
            bindStream(postCamera);
        } catch (error) {
            setStatus(postStatus, error.message || 'No se pudo activar la camara.');
        }
    }

    postPhoto?.addEventListener('click', async () => {
        try {
            setStatus(postStatus, 'Tomando foto...');
            setPostFile(await capturePhoto(postCamera, postCanvas), 'FOTO');
            setStatus(postStatus, 'Foto lista para enviar.');
        } catch (error) {
            setStatus(postStatus, error.message || 'No se pudo tomar la foto.');
        }
    });

    postRecord?.addEventListener('click', async () => {
        try {
            await ensureMediaStream();
            bindStream(postCamera);
            if (postRecorder && postRecorder.state === 'recording') {
                await stopRecorder(postRecorder);
                postRecord.innerHTML = '<i class="bi bi-record-circle"></i> Grabar video';
                return;
            }
            postChunks = [];
            postRecorder = new MediaRecorder(sharedStream, MediaRecorder.isTypeSupported('video/webm') ? { mimeType: 'video/webm' } : undefined);
            postRecorder.ondataavailable = (event) => {
                if (event.data && event.data.size > 0) postChunks.push(event.data);
            };
            postRecorder.onstop = () => {
                setPostFile(new File([new Blob(postChunks, { type: 'video/webm' })], 'evidencia_video.webm', { type: 'video/webm' }), 'VIDEO');
                setStatus(postStatus, 'Video listo para enviar.');
            };
            postRecorder.start();
            postRecord.innerHTML = '<i class="bi bi-stop-circle"></i> Detener video';
            setStatus(postStatus, 'Grabando video...');
        } catch (error) {
            setStatus(postStatus, error.message || 'No se pudo grabar video.');
        }
    });

    postSend?.addEventListener('click', async () => {
        if (!postFile) return;
        try {
            postSend.disabled = true;
            setStatus(postStatus, 'Enviando evidencia...');
            await uploadEvidenceJson(
                postFile,
                postFileType,
                postDescription?.value || '',
                await getPositionOnce(),
                { emergenciaId: currentSosCaseId }
            );
            window.location.href = '/usuario/evidencias';
        } catch (error) {
            postSend.disabled = false;
            setStatus(postStatus, error.message || 'No se pudo enviar evidencia.');
        }
    });

    postSkip?.addEventListener('click', () => {
        window.location.href = '/usuario/evidencias';
    });

    const mapElement = document.getElementById('userMap');
    if (mapElement && window.L) {
        const lat = Number(mapElement.dataset.lat || '-12.0464');
        const lng = Number(mapElement.dataset.lng || '-77.0428');
        const map = L.map('userMap').setView([lat, lng], 15);
        L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', { maxZoom: 19, attribution: '&copy; OpenStreetMap' }).addTo(map);
        L.marker([lat, lng]).addTo(map).bindPopup('Ubicacion registrada').openPopup();
    }

    const cameraModule = document.querySelector('[data-camera-module]');
    if (!cameraModule) return;

    const photoVideo = document.querySelector('[data-photo-video]');
    const photoCanvas = document.querySelector('[data-photo-canvas]');
    const recordButton = document.querySelector('[data-record-video]');
    const videoStatus = document.querySelector('[data-video-status]');
    const captureForms = document.querySelectorAll('[data-capture-form]');
    const sharedDescription = document.querySelector('[data-shared-description]');
    let manualRecorder = null;
    let manualChunks = [];
    let recordedVideo = null;

    ensureMediaStream()
        .then(() => bindStream(photoVideo))
        .catch((error) => setStatus(videoStatus, error.message || 'No se pudo activar la camara.'));

    recordButton?.addEventListener('click', async () => {
        try {
            await ensureMediaStream();
            bindStream(photoVideo);
            if (manualRecorder && manualRecorder.state === 'recording') {
                await stopRecorder(manualRecorder);
                recordButton.innerHTML = '<i class="bi bi-record-circle"></i> Grabar video';
                return;
            }
            manualChunks = [];
            recordedVideo = null;
            manualRecorder = new MediaRecorder(sharedStream, MediaRecorder.isTypeSupported('video/webm') ? { mimeType: 'video/webm' } : undefined);
            manualRecorder.ondataavailable = (event) => {
                if (event.data && event.data.size > 0) manualChunks.push(event.data);
            };
            manualRecorder.onstop = () => {
                recordedVideo = new File([new Blob(manualChunks, { type: 'video/webm' })], 'evidencia_video.webm', { type: 'video/webm' });
                setStatus(videoStatus, 'Video listo para enviar.');
            };
            manualRecorder.start();
            recordButton.innerHTML = '<i class="bi bi-stop-circle"></i> Detener video';
            setStatus(videoStatus, 'Grabando video...');
        } catch (error) {
            setStatus(videoStatus, error.message || 'No se pudo grabar video.');
        }
    });

    captureForms.forEach((form) => {
        form.addEventListener('submit', async (event) => {
            event.preventDefault();
            try {
                await getPositionOnce();
                form.querySelectorAll('[data-evidence-description]').forEach((field) => field.value = sharedDescription?.value || '');
                form.querySelectorAll('[data-evidence-lat]').forEach((field) => field.value = lastPosition?.lat || '0');
                form.querySelectorAll('[data-evidence-lng]').forEach((field) => field.value = lastPosition?.lng || '0');
                form.querySelectorAll('[data-evidence-address]').forEach((field) => field.value = lastPosition?.address || 'Ubicacion no registrada');
                const type = form.dataset.captureType;
                const file = type === 'FOTO'
                    ? await capturePhoto(photoVideo, photoCanvas)
                    : recordedVideo;
                if (!file) throw new Error('Primero graba un video para enviarlo.');
                const data = new FormData(form);
                data.set('archivo', file);
                const response = await fetch(form.action, { method: 'POST', body: data, credentials: 'same-origin' });
                if (!response.ok) throw new Error('No se pudo enviar la evidencia.');
                window.location.href = response.url || '/usuario/evidencias';
            } catch (error) {
                setStatus(videoStatus, error.message || 'No se pudo enviar la evidencia.');
            }
        });
    });
})();
