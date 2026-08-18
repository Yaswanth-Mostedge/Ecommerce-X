function togglePassword(button) {
    var input = button.previousElementSibling;
    var visible = button.classList.toggle('is-visible');
    input.type = visible ? 'text' : 'password';
    button.setAttribute('aria-label', visible ? 'Hide password' : 'Show password');
}

function updateCartCount(count) {
    var link = document.getElementById('nav-cart-link');
    if (!link) return;

    var badge = link.querySelector('.cart-count');
    if (!badge) {
        badge = document.createElement('span');
        badge.className = 'cart-count';
        link.appendChild(badge);
    }

    badge.textContent = count;
    badge.classList.remove('cart-count-bump');
    void badge.offsetWidth;
    badge.classList.add('cart-count-bump');
}

// The shop grid hides/shows the stepper itself; the product page wraps it
// together with a "View cart" link in .product-qty-row and toggles that instead.
function stepperToggleTarget(stepper) {
    if (!stepper) return null;
    return stepper.closest('.product-qty-row') || stepper;
}

function swapToStepper(control, qty) {
    if (!control) return;
    var form = control.querySelector('.cart-form, .buy-row');
    var stepper = control.querySelector('.qty-stepper');
    if (form) form.classList.add('is-hidden');

    var toggleTarget = stepperToggleTarget(stepper);
    if (toggleTarget) toggleTarget.classList.remove('is-hidden');

    if (stepper) {
        var value = stepper.querySelector('.qty-value');
        if (value) value.textContent = qty;
    }
}

function swapToAddButton(control) {
    if (!control) return;
    var form = control.querySelector('.cart-form, .buy-row');
    var stepper = control.querySelector('.qty-stepper');

    var toggleTarget = stepperToggleTarget(stepper);
    if (toggleTarget) toggleTarget.classList.add('is-hidden');

    if (form) form.classList.remove('is-hidden');
}

// Shop grid / product page "Add to cart": submits in the background and, on the
// shop grid, swaps the button for a quantity stepper instead of navigating away.
document.addEventListener('submit', function (event) {
    var form = event.target;
    if (!form.classList.contains('cart-form') && !form.classList.contains('buy-row')) {
        return;
    }

    event.preventDefault();

    fetch(form.action, {
        method: 'POST',
        headers: { 'X-Requested-With': 'XMLHttpRequest' },
        body: new FormData(form)
    })
        .then(function (response) {
            if (!response.ok) {
                throw new Error('Could not add to cart');
            }
            return response.json();
        })
        .then(function (data) {
            updateCartCount(data.count);
            swapToStepper(form.closest('.cart-control'), data.qty);
        })
        .catch(function () {});
});

// Shop grid quantity stepper: +/- adjust the line in the background.
document.addEventListener('click', function (event) {
    var button = event.target.closest('.qty-increment, .qty-decrement');
    if (!button) return;

    var stepper = button.closest('.qty-stepper');
    var control = button.closest('.cart-control');
    var csrfInput = stepper.querySelector('input[name="_csrf"]');
    var productId = stepper.getAttribute('data-product-id');
    var max = parseInt(stepper.getAttribute('data-max'), 10);
    var increment = button.classList.contains('qty-increment');

    var body = new FormData();
    body.append('_csrf', csrfInput ? csrfInput.value : '');

    fetch('/cart/' + (increment ? 'add' : 'decrement') + '/' + productId, {
        method: 'POST',
        headers: { 'X-Requested-With': 'XMLHttpRequest' },
        body: body
    })
        .then(function (response) {
            if (!response.ok) {
                throw new Error('Could not update cart');
            }
            return response.json();
        })
        .then(function (data) {
            updateCartCount(data.count);

            if (data.qty > 0) {
                var value = stepper.querySelector('.qty-value');
                if (value) value.textContent = data.qty;

                var incrementButton = stepper.querySelector('.qty-increment');
                if (incrementButton) {
                    incrementButton.disabled = !isNaN(max) && data.qty >= max;
                }
            } else {
                swapToAddButton(control);
            }
        })
        .catch(function () {});
});
