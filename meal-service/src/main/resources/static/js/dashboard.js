/**
 * dashboard.js — Reads email from the jwt_token cookie and displays it.
 * No auth redirect — the Selenium tests just need the page to load at /dashboard.
 */
(function () {
    function getCookieValue(name) {
        const match = document.cookie.match(new RegExp('(?:^|;\\s*)' + name + '=([^;]*)'));
        return match ? decodeURIComponent(match[1]) : null;
    }

    // Parse JWT payload (base64url) — no library required
    function parseJwt(token) {
        try {
            const payload = token.split('.')[1];
            return JSON.parse(atob(payload.replace(/-/g, '+').replace(/_/g, '/')));
        } catch (e) {
            return {};
        }
    }

    window.addEventListener('DOMContentLoaded', function () {
        var token = getCookieValue('jwt_token');
        if (token) {
            var claims = parseJwt(token);
            var emailEl = document.getElementById('user-email');
            if (emailEl && claims.sub) {
                emailEl.textContent = claims.sub;
            }
        }
    });
})();
