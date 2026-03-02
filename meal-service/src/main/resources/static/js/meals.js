/**
 * meals.js — Submits the dietary filter form when the select changes.
 *
 * Why external file? CSP script-src 'self' blocks inline event handlers.
 * The form is a regular GET form so the page reloads with server-side-rendered
 * meal cards — Selenium's stalenessOf() condition is satisfied by the page reload.
 */
window.addEventListener('DOMContentLoaded', function () {
    var sel = document.getElementById('dietaryTypeFilter');
    if (sel) {
        sel.addEventListener('change', function () {
            var form = document.getElementById('filterForm');
            if (form) form.submit();
        });
    }
});
