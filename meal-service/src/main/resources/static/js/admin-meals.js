/**
 * admin-meals.js — Toggles the create-meal form section.
 *
 * Why external file? CSP script-src 'self' blocks inline onclick handlers.
 */
window.addEventListener('DOMContentLoaded', function () {
    var createBtn = document.getElementById('btn-create-meal');
    var section   = document.getElementById('createMealSection');

    if (createBtn && section) {
        createBtn.addEventListener('click', function () {
            section.classList.remove('hidden');
            // Scroll to form so headless browser viewport includes it
            section.scrollIntoView({ behavior: 'instant' });
        });
    }
});
