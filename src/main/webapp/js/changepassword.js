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

    // Short wording for the "still needed" message, in display order.
    var RULE_LABELS = [
        ['length',  'at least ' + MIN_LENGTH + ' characters'],
        ['upper',   'an uppercase letter'],
        ['lower',   'a lowercase letter'],
        ['digit',   'a digit'],
        ['special', 'a special character']
    ];

    /** Labels of the rules this password does NOT yet satisfy, in display order. */
    function unmetRules(pw) {
        var r = checkRules(pw);
        var out = [];
        for (var i = 0; i < RULE_LABELS.length; i++) {
            if (!r[RULE_LABELS[i][0]]) {
                out.push(RULE_LABELS[i][1]);
            }
        }
        return out;
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
        unmetRules: unmetRules,
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
            // Unmet rules stay neutral until the user starts typing, then turn red.
            var started = pw1.value.length > 0;
            for (var i = 0; i < items.length; i++) {
                var met = !!rules[items[i].getAttribute('data-rule')];
                items[i].classList.toggle('ok', met);
                items[i].classList.toggle('bad', started && !met);
            }
            var mismatch = showMismatch(pw1.value, pw2.value);
            // Once they are into the confirm field, say what is still wrong, in the same place.
            var unmet = pw2.value.length > 0 ? unmetRules(pw1.value) : [];
            var msgs = [];
            if (unmet.length) {
                msgs.push('Password still needs: ' + unmet.join(', ') + '.');
            }
            if (mismatch) {
                msgs.push("Passwords don't match.");
            }
            errBox.textContent = msgs.join(' ');
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
