#include <stdarg.h> /* va_list */
#include <stddef.h> /* NULL */
#include <stdint.h> /* int64_t */
#include <stdlib.h> /* int64_t */
#include <stdio.h>  /* printf */
#include <string.h>

#include <kcgi.h>
#include <kcgihtml.h>
#include "smtp.h"

/**
 * CGI program for HN "/signup" backend
 */

// These two must be kept in synch!!
enum key { // names
  KEY_NAMES,
  KEY_EMAIL,
  KEY_EMAIL_HOME_OR_WORK,
  KEY_TYPE,
  KEY_DONATION,
  KEY_NEW_OR_RENEW,
  KEY_TOWN,
  KEY_PHONE1,
  KEY_EMAIL_HOME_OR_WORK_1,
  KEY_PHONE2,
  KEY_EMAIL_HOME_OR_WORK_2,
  KEY_INTERESTS,
  KEY_SUGGESTIONS,
  KEY__MAX
};

static const struct kvalid keys[KEY__MAX] = {
  { kvalid_stringne, "names" },
  { kvalid_stringne, "email" },
  { kvalid_stringne, "email_home_or_work" },
  { kvalid_stringne, "type" },
  { kvalid_stringne, "donation" },
  { kvalid_stringne, "new_or_renew" },
  { kvalid_stringne, "town" },
  { kvalid_stringne, "phone1" },
  { kvalid_stringne, "email_home_or_work_1" },
  { kvalid_stringne, "phone2" },
  { kvalid_stringne, "email_home_or_work_2" },
  { kvalid_stringne, "interests" },
  { kvalid_stringne, "suggestions" },
};

const char* 
  names,
  email,
  email_home_or_work,
  type,
  donation,
  new_or_renew,
  town,
  phone1,
  email_home_or_work_1,
  phone2,
  email_home_or_work_2,
  interests,
  suggestions;

/** Handle the request, sending it to the admin and maybe to the database. */
static void process_safe(struct kreq *r) {
  struct kpair *p;
  struct khtmlreq req;

  khtml_open(&req, r, 0);

  khtml_elem(&req, KELEM_HEAD);
  khtml_elem(&req, KELEM_TITLE);
  khtml_puts(&req, "Contact Response");
  khtml_closeelem(&req, 1); /* title */
  khtml_attr(&req, KELEM_LINK,
    KATTR_REL, "stylesheet",
    KATTR_HREF, "/css/darwinsys.css", KATTR__MAX);
  khtml_closeelem(&req, 1); /* head */

  khtml_elem(&req, KELEM_H1);
  khtml_puts(&req, "Contact Response");
  khtml_closeelem(&req, 1); /* h1 */

  // Name
  khtml_elem(&req, KELEM_P);
  khtml_puts(&req, "Your name(s) were given as ");
  if ((p = r->fieldmap[KEY_NAMES])) {
	khtml_puts(&req, name = p->parsed.s);
  } else if (r->fieldnmap[KEY_NAMES]) {
	khtml_elem(&req, KELEM_I);
	khtml_puts(&req, "failed parse");
  } else {
	khtml_elem(&req, KELEM_I);
	khtml_puts(&req, "not provided - a fatal error");
	khtml_elem(&req, KELEM_BR);
  }

  // Email
  khtml_elem(&req, KELEM_P);
  khtml_puts(&req, "Your email was given as ");
  if ((p = r->fieldmap[KEY_EMAIL])) {
	khtml_puts(&req, email = p->parsed.s);
  } else if (r->fieldnmap[KEY_EMAIL]) {
	khtml_elem(&req, KELEM_I);
	khtml_puts(&req, "failed parse");
  } else {
	khtml_elem(&req, KELEM_I);
	khtml_puts(&req, "not provided");
	khtml_elem(&req, KELEM_BR);
  }

  // XXX Need above dozen lines for EACH FIELD

  char* remote_ip = getenv("REMOTE_ADDR");

  setup_email(NULL);
  if (remote_ip != NULL) {
	unsigned long buflen =  + strlen("Remote IP: \n\n") + + strlen(remote_ip) + strlen(message) + 1;
	char *buf = malloc(buflen);
	if (buf == NULL) {
		/* Grrr, out of mem. Realy a CANTHAPPEN, but oh well... */
		send_email(names, email, subject, message);	// Send without logging IP.
		goto getout;
	}
	snprintf(buf, buflen, "Remote IP: %s\n\n%s", remote_ip, message);
	send_email(names, email, subject, buf);
	free(buf);
  } else {
	  send_email(names, email, subject, message);
  }

getout:
  end_email();

  khtml_elem(&req, KELEM_P);
  if (fail) {
    khtml_puts(&req, "Your message was not sent.");
  } else {
    khtml_puts(&req, "Your message has been sent. If:"
		"It isn't SPAM, and "
		"You wrote something interesting, and "
		"You gave a working email address "
		"Then you should receive a reply shortly. Thank you!");
  }
  khtml_closeelem(&req, 1); /* p */

  khtml_attr(&req, KELEM_A,
    KATTR_HREF, "/",
    KATTR__MAX);
  khtml_puts(&req, "Back");
  khtml_closeelem(&req, 1); /* a */

  khtml_close(&req);
}

enum page {
  PAGE_INDEX,
  PAGE__MAX
};
/* Must be an array for khttp_parse */
const char *const pages[PAGE__MAX] = {
  "index", /* PAGE_INDEX */
};

static enum khttp sanitise(const struct kreq *r) {
  if (r->page != PAGE_INDEX)
	return KHTTP_404;
  // else if (*r->path != '\0') /* no index/xxxx */
  // return KHTTP_404;
  else if (r->mime != KMIME_TEXT_HTML)
	return KHTTP_404;
  else if (r->method != KMETHOD_POST)
	return KHTTP_405;
  return KHTTP_200;
}

int main(void) {
  struct kreq r;
  enum khttp er;
  if (khttp_parse(&r, keys, KEY__MAX,
	  pages, PAGE__MAX, PAGE_INDEX) != KCGI_OK)
	return 0;
  if ((er = sanitise(&r)) != KHTTP_200) {
	khttp_head(&r, kresps[KRESP_STATUS],
	  "%s", khttps[er]);
	khttp_head(&r, kresps[KRESP_CONTENT_TYPE],
	  "%s", kmimetypes[KMIME_TEXT_PLAIN]);
	khttp_body(&r);
	if (KMIME_TEXT_HTML == r.mime)
	  khttp_puts(&r, "Could not service request.");
  } else {
	khttp_head(&r, kresps[KRESP_STATUS],
	  "%s", khttps[KHTTP_200]);
	khttp_head(&r, kresps[KRESP_CONTENT_TYPE],
	  "%s", kmimetypes[r.mime]);
	khttp_body(&r);
	process_safe(&r);
  }
  khttp_free(&r);
  return 0;
};
