/*
 * Glue between Renew.xhtml and HelcimPay.js (https://secure.helcim.app/helcim-pay/services/start.js,
 * which must be loaded first, and defines appendHelcimPayIframe).
 *
 * The server starts a checkout and calls HNHelcim.start(checkoutToken). When the payment window
 * reports SUCCESS we hand its report to the server by filling the hidden field and clicking the
 * hidden submit button. Nothing here is trusted: the server verifies the report.
 */
window.HNHelcim = (function () {
	'use strict';
	var listener = null;

	function removeFrame() {
		var frame = document.getElementById('helcimPayIframe');
		if (frame) {
			frame.remove();
		}
	}

	function stop() {
		if (listener) {
			window.removeEventListener('message', listener);
			listener = null;
		}
	}

	function start(checkoutToken) {
		stop();
		listener = function (event) {
			var d = event.data;
			if (!d || d.eventName !== 'helcim-pay-js-' + checkoutToken) {
				return;
			}
			if (d.eventStatus === 'SUCCESS') {
				var msg = (typeof d.eventMessage === 'string') ? d.eventMessage : JSON.stringify(d.eventMessage);
				document.getElementById('payForm:helcimResult').value = msg;
				removeFrame();
				stop();
				document.getElementById('payForm:complete').click();
			} else if (d.eventStatus === 'HIDE') {
				removeFrame();		// they closed the window without paying
				stop();
			}
			// ABORTED = card declined; Helcim's window shows the reason and lets them try again
		};
		window.addEventListener('message', listener);
		appendHelcimPayIframe(checkoutToken);
	}

	return { start: start };
})();
