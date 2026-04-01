// CineRent — main JS
document.addEventListener('DOMContentLoaded', () => {
    // Auto-dismiss alerts after 5s
    document.querySelectorAll('.alert[class*="cr-alert"]').forEach(el => {
        setTimeout(() => { el.style.opacity = '0'; el.style.transition = 'opacity .5s'; setTimeout(() => el.remove(), 500); }, 5000);
    });
    // Confirm on delete buttons
    document.querySelectorAll('form button[type="submit"]').forEach(btn => {
        if (btn.textContent.trim().toLowerCase() === 'delete') {
            btn.addEventListener('click', e => { if (!confirm('Are you sure you want to delete this?')) e.preventDefault(); });
        }
    });
});
