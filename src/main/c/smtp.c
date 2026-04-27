/*
 * smtp -- talk to remote smtp server (debugging)
 * Implements the barest bones of SMTP, for use only behind firewall!
 */

#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <netdb.h>
#include <ctype.h>
#include <string.h>
#include <errno.h>
#include <err.h>
#define	_SM_PRIVATE
#include "smtp.h"

#define	DEFHOST	"127.0.0.1"		/* host we want to honk at. */
#define MAXSTR	32767			/* how long lines can grow. */

char *progname;
int debug = 0, echo = 0;
int sock = 0;
FILE *sfi, *sfo;

#define	RECIPIENT "mail_contact"

#ifdef	DEBUG
int
main(int argc, char **argv) {
	debug = 1;
	setup_email(NULL);
	send_email("Robin Smith", "robin@erewhon.com", "Testing", "This is my test, to follow that star...");
	end_email();
}
#endif

/*
 * setup_email -- setup tcp/ip connection to/from server
 */
int
setup_email(char *server) {

	struct hostent *hp;
	struct servent *sp;
	struct sockaddr_in sin;

	if (server == NULL)
		server = DEFHOST;

	bzero((char *)&sin, sizeof(sin));

	if ((hp = gethostbyname(server)) == (struct hostent *) NULL) {
		warn("unknown host (%s).\n", server);
		return -1;
	}

	bcopy(hp->h_addr, &sin.sin_addr, hp->h_length);

	// We live in a chroot, and SMTP is unlikely to get assigned a different port# soon.
	sin.sin_port = htons(25);
	sin.sin_family = AF_INET;

	if ((sock = rresvport(&sock)) < 0) {
		warn("setup - resvport");
		return -1;
	}

	if (connect(sock, (struct sockaddr *) &sin, sizeof (sin)) < 0) {
		warn("setup - connect");
		return -1;
	}

	if (((sfi = fdopen(sock, "r")) == (FILE *) NULL) ||
	    ((sfo = fdopen(sock, "w")) == (FILE *) NULL)) {
		warn("setup - fdopen");
		return -1;
	}

	return 0;
}

/*
 * return 0 if str is OK, 1 if it has bad chars
 */
int
sanitize(const char *str) {
	return (strchr(str, '\r') == NULL && strchr(str, '\n') == NULL);
}

int
send_email(const char *sender_name, const char *sender_email, const char *subject, const char *message) {

	char newstr[MAXSTR];

	if (!sanitize(sender_name)) {
		warn("Monkey business in sender's name");
		return -1;
	}
	if (!sanitize(sender_email)) {
		warn("Monkey business in sender's email");
		return -1;
	}
	if (!sanitize(subject)) {
		warn("Monkey business in subject");
		return -1;
	}
	char *smessage = (char*) message;
	for (char *p = smessage; *p; p++)
		if (*p == '\r' || *p == '\n')
			*p = ' ';

	getreply(sfi); // s/b 220 servername
	sendnext(sfo,"HELO localhost");
	getreply(sfi);	// s/b 250 Hello
	(void) sprintf(newstr, "MAIL From: <%s>", sender_email);
	sendnext(sfo, newstr);
	getreply(sfi);	// s/b 250 OK
	sprintf(newstr, "RCPT To:<%s>", RECIPIENT);
	sendnext(sfo, newstr);
	getreply(sfi);	// S/B 250 2.1.5 Dest OK
	sendnext(sfo, "DATA");
	getreply(sfi);	// S/B 354 Enter Data
	sprintf(newstr, "From: \"%s\" <%s>", sender_name, sender_email);
	sendnext(sfo, newstr);
	sprintf(newstr, "To: %s", RECIPIENT);
	sendnext(sfo, newstr);
	(void) sprintf(newstr, "Subject: %s", subject);
	sendnext(sfo, newstr);
	sendnext(sfo, "");
	sendnext(sfo, smessage);
	sendnext(sfo, "");
	sendnext(sfo, ".");
	getreply(sfi);	// s/b 250 OK
	sendnext(sfo, "QUIT");
	getreply(sfi);	
	return 0;
}

void end_email() {
	(void) fclose(sfi);
	(void) fclose(sfo);
	(void) close(sock);
}

int
getreply(FILE *fi) {
	char line[MAXSTR];

	(void) strcpy(line, "");
	(void) fgets(line, MAXSTR, fi);
	if (ferror(fi)) {
		warn("error reading from smtp");
		return -1;
	}
	if (debug)
		fprintf(stderr, "%s", line);
	if (feof(fi)) {
		warn("read eof from smtp");
		return -1;
	}
	line[3] = 0;
	return atoi(line);
}

void
sendnext(FILE *fo, const char *line) {

	char *vis();

	(void) fputs(line, fo); putc('\r', fo); putc('\n', fo);
	(void) fflush(fo);
	if (ferror(fo))
		err(1, "error writing to smtp");
	if (echo)
		fprintf(stderr, "wrote <%s> to smtp\n", line);
}
