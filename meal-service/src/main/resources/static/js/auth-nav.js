(function () {
    function getCookieValue(name) {
        var match = document.cookie.match(new RegExp('(?:^|;\\s*)' + name + '=([^;]*)'));
        return match ? decodeURIComponent(match[1]) : null;
    }

    function parseJwt(token) {
        try {
            var payload = token.split('.')[1];
            return JSON.parse(atob(payload.replace(/-/g, '+').replace(/_/g, '/')));
        } catch (error) {
            return {};
        }
    }

    window.addEventListener('DOMContentLoaded', function () {
        var token = getCookieValue('jwt_token');
        var claims = token ? parseJwt(token) : {};
        var loginItem = document.getElementById('nav-login-item');
        var logoutItem = document.getElementById('nav-logout-item');
        var chip = document.getElementById('nav-user-chip');
        var email = document.getElementById('nav-user-email');

        if (token && claims.sub) {
            if (loginItem) {
                loginItem.classList.add('d-none');
            }
            if (logoutItem) {
                logoutItem.classList.remove('d-none');
            }
            if (chip && email) {
                chip.classList.remove('d-none');
                email.textContent = claims.sub;
            }
            return;
        }

        if (logoutItem) {
            logoutItem.classList.add('d-none');
        }
        if (loginItem) {
            loginItem.classList.remove('d-none');
        }
    });
})();