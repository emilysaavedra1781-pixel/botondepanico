(() => {
    'use strict';

    const forms = document.querySelectorAll('.needs-validation');

    forms.forEach((form) => {
        form.addEventListener('submit', (event) => {
            const password = form.querySelector('[data-password]');
            const confirm = form.querySelector('[data-confirm-password]');

            if (password && confirm) {
                confirm.setCustomValidity(password.value === confirm.value ? '' : 'Las contraseñas no coinciden');
            }

            if (!form.checkValidity()) {
                event.preventDefault();
                event.stopPropagation();
            }

            form.classList.add('was-validated');
        });
    });
})();