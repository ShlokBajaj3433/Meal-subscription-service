/**
 * admin-meals.js — Toggles the create-meal form section.
 *
 * Why external file? CSP script-src 'self' blocks inline onclick handlers.
 */
window.addEventListener('DOMContentLoaded', function () {
    var createBtn = document.getElementById('btn-create-meal');
    var cancelBtn = document.getElementById('btn-cancel-meal');
    var section   = document.getElementById('createMealSection');

    if (createBtn && section) {
        createBtn.addEventListener('click', function () {
            section.classList.remove('hidden');
            createBtn.textContent = '✕ Close Form';
            // Scroll to form so headless browser viewport includes it
            section.scrollIntoView({ behavior: 'smooth', block: 'start' });
        });
    }

    if (cancelBtn && section) {
        cancelBtn.addEventListener('click', function () {
            section.classList.add('hidden');
            if (createBtn) createBtn.textContent = '+ Create Meal';
        });
    }
});
