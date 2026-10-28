/*
 * Change Password, step 2: client-side rules.
 * The pure functions are exported for Node-based testing; the DOM wiring
 * only runs in a browser. The server must still enforce the same rules.
 */
(function (root) {
    'use strict';

    var MIN_LENGTH = 10;
    // Don't show "Passwords don't match" until the confirm field has this many characters.
    var CONFIRM_THRESHOLD = 8;

    /** Which rules does this password satisfy? */
    function checkRules(pw) {
        return {
            length:  pw.length >= MIN_LENGTH,
            upper:   /\p{Lu}/u.test(pw),
            lower:   /\p{Ll}/u.test(pw),
            digit:   /\p{Nd}/u.test(pw),
            special: /[^\p{L}\p{N}\s]/u.test(pw)
        };
    }

    function meetsRules(pw) {
        var r = checkRules(pw);
        return r.length && r.upper && r.lower && r.digit && r.special;
    }

    /**
     * Show the mismatch error only once the confirm field has enough characters,
     * and only if it has diverged from the first entry. A confirm value that is
     * still a proper prefix of the first one is just "typing in progress".
     */
    function showMismatch(first, second) {
        return second.length >= CONFIRM_THRESHOLD && !first.startsWith(second);
    }

    /** Overall go/no-go for enabling the submit button. */
    function canSubmit(first, second) {
        return meetsRules(first) && first === second;
    }

    var api = {
        MIN_LENGTH: MIN_LENGTH,
        CONFIRM_THRESHOLD: CONFIRM_THRESHOLD,
        checkRules: checkRules,
        meetsRules: meetsRules,
        showMismatch: showMismatch,
        canSubmit: canSubmit
    };

    if (typeof module !== 'undefined' && module.exports) {
        module.exports = api;
    }
    if (typeof document === 'undefined') {
        return;
    }

    function init() {
        var form    = document.getElementById('step2');
        var pw1     = document.getElementById('newPassword');
        var pw2     = document.getElementById('confirmPassword');
        var eye     = document.getElementById('toggleVisibility');
        var submit  = document.getElementById('submitBtn');
        var errBox  = document.getElementById('mismatchError');
        var items   = document.querySelectorAll('.rules li[data-rule]');
        if (!form || !pw1 || !pw2 || !submit) {
            return;
        }

        function update() {
            var rules = checkRules(pw1.value);
            for (var i = 0; i < items.length; i++) {
                items[i].classList.toggle('ok', !!rules[items[i].getAttribute('data-rule')]);
            }
            var mismatch = showMismatch(pw1.value, pw2.value);
            errBox.textContent = mismatch ? "Passwords don't match" : '';
            pw2.setAttribute('aria-invalid', mismatch ? 'true' : 'false');
            submit.disabled = !canSubmit(pw1.value, pw2.value);
        }

        eye.addEventListener('click', function () {
            var showing = pw1.type === 'text';
            pw1.type = showing ? 'password' : 'text';
            eye.setAttribute('aria-pressed', showing ? 'false' : 'true');
            eye.setAttribute('aria-label', showing ? 'Show password' : 'Hide password');
            eye.title = showing ? 'Show password' : 'Hide password';
            pw1.focus();
        });

        pw1.addEventListener('input', update);
        pw2.addEventListener('input', update);
        // Belt and braces: never let an invalid form through (e.g. a scripted submit).
        form.addEventListener('submit', function (e) {
            if (!canSubmit(pw1.value, pw2.value)) {
                e.preventDefault();
            }
        });
        // Back/forward cache and browser autofill can restore values without input events.
        window.addEventListener('pageshow', update);

        pw1.focus();
        update();
    }

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', init);
    } else {
        init();
    }
})(this);
