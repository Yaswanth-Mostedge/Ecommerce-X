function togglePassword(button) {
    var input = button.previousElementSibling;
    var visible = button.classList.toggle('is-visible');
    input.type = visible ? 'text' : 'password';
    button.setAttribute('aria-label', visible ? 'Hide password' : 'Show password');
}
