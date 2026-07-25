let recognition = null;
let escuchando = false;

function toggleMic() {

    if (!('webkitSpeechRecognition' in window || 'SpeechRecognition' in window)) {
        alert('Tu navegador no soporta reconocimiento de voz. Usa Google Chrome.');
        return;
    }

    if (escuchando) {
        recognition.stop();
        return;
    }

    const SpeechRecognition =
        window.SpeechRecognition ||
        window.webkitSpeechRecognition;

    recognition = new SpeechRecognition();

    recognition.lang = 'es-PE';
    recognition.continuous = true;
    recognition.interimResults = false;

    recognition.onstart = () => {

        escuchando = true;

        document.getElementById('btnMic').textContent =
            '⏹ Detener Micrófono';

        document.getElementById('btnMic').classList.add('activo');

        document.getElementById('mic-status')
            .classList.remove('d-none');
    };

    recognition.onend = () => {

        escuchando = false;

        document.getElementById('btnMic').textContent =
            '🎤 Activar Micrófono';

        document.getElementById('btnMic').classList.remove('activo');

        document.getElementById('mic-status')
            .classList.add('d-none');
    };

    recognition.onresult = (event) => {

        const texto =
            event.results[event.results.length - 1][0]
                .transcript
                .toLowerCase();

        console.log('Escuchado:', texto);

        // =========================
        // NOMBRE Y APELLIDO
        // =========================

        if (texto.includes('me llamo') || texto.includes('mi nombre es')) {

            const partes =
                texto.split(/me llamo|mi nombre es/);

            if (partes[1]) {

                const palabras =
                    partes[1].trim().split(' ');

                if (palabras[0]) {
                    document.getElementById('nombre_ciudadano').value =
                        capitalizar(palabras[0]);
                }

                if (palabras[1]) {
                    document.getElementById('apellido_ciudadano').value =
                        capitalizar(palabras[1]);
                }
            }
        }

        // =========================
        // CELULAR
        // =========================

        const numeroCelular =
            texto.match(/\b9\d{8}\b/);

        if (numeroCelular) {

            document.getElementById('celular_ciudadano').value =
                numeroCelular[0];
        }

        // =========================
        // DISTRITOS
        // =========================

        const distritos = [
            'miraflores',
            'surco',
            'san isidro',
            'barranco',
            'lince',
            'jesús maría',
            'pueblo libre',
            'magdalena',
            'san borja',
            'la molina',
            'ate',
            'san juan de lurigancho',
            'comas',
            'independencia',
            'los olivos',
            'callao'
        ];

        distritos.forEach(distrito => {

            if (texto.includes(distrito)) {

                document.getElementById('distrito_ciudadano').value =
                    capitalizar(distrito);
            }

        });

        // =========================
        // UBICACIÓN
        // =========================

        if (texto.includes('estoy en')) {

            const partes =
                texto.split('estoy en');

            if (partes[1]) {

                const ubicacion =
                    capitalizar(partes[1].trim());

                const campoUbicacion =
                    document.getElementById('ubicacion');

                if (campoUbicacion) {
                    campoUbicacion.value = ubicacion;
                }
            }
        }

        // =========================
        // TIPO EMERGENCIA
        // =========================

        if (
            texto.includes('robo') ||
            texto.includes('asalto') ||
            texto.includes('delito')
        ) {

            document.getElementById('tipo_emergencia').value =
                'POLICIA';

        } else if (
            texto.includes('incendio') ||
            texto.includes('fuego')
        ) {

            document.getElementById('tipo_emergencia').value =
                'BOMBEROS';

        } else if (
            texto.includes('accidente') ||
            texto.includes('herido') ||
            texto.includes('médico')
        ) {

            document.getElementById('tipo_emergencia').value =
                'SAMU';

        } else if (
            texto.includes('comisaria') ||
            texto.includes('comisaría')
        ) {

            document.getElementById('tipo_emergencia').value =
                'POLICIA';
        }

        // =========================
        // DESCRIPCIÓN
        // =========================

        const descripcion =
            document.getElementById('descripcion');

        if (descripcion.value.length < 500) {

            descripcion.value +=
                (descripcion.value ? ' ' : '') + texto;
        }

    };

    recognition.start();
}

// ===================================
// CAPITALIZAR TEXTO
// ===================================

function capitalizar(texto) {

    return texto
        .split(' ')
        .map(palabra =>
            palabra.charAt(0).toUpperCase() +
            palabra.slice(1)
        )
        .join(' ');
}

// ===================================
// FECHA DE SESIÓN
// ===================================

document.addEventListener('DOMContentLoaded', () => {

    let fechaSesion =
        sessionStorage.getItem('fechaSesion');

    if (!fechaSesion) {

        fechaSesion =
            new Date().toLocaleString('es-PE', {
                day: '2-digit',
                month: '2-digit',
                year: 'numeric',
                hour: '2-digit',
                minute: '2-digit',
                second: '2-digit'
            });

        sessionStorage.setItem(
            'fechaSesion',
            fechaSesion
        );
    }

    const spanFecha =
        document.getElementById('fechaSesion');

    if (spanFecha) {
        spanFecha.textContent = fechaSesion;
    }

});