/* Title: Asynchronous example with pledge() sandbox */
/* From a kristaps preso */

#include <err.h>
#include <stdlib.h>
#include <stdio.h>
#include <unistd.h>

int
main(int argc, char *argv[])
{
	int c, async = 0;
	pid_t pid;

	/*
	* Really, all we want is our response (stdio)
	* and to start the long-running process.
	* If we were to also have some sort of
	* database, we'd use the database things here.
	*/

	if (-1 == pledge("stdio rpath proc exec", NULL))
	err(EXIT_FAILURE, NULL);

	while (-1 != (c = getopt(argc, argv, "X")))
	switch (c) {
	case 'X':
	async = 1;
	break;
	default:
	return(EXIT_SUCCESS);
	}

	if (async) {
	/* Assume we'll want stdio... */

	if (-1 == pledge("stdio", NULL))
	err(EXIT_FAILURE, NULL);
	sleep(20);
	return(EXIT_SUCCESS);
	}

	puts("Status: 200 OK\r");
	puts("Content-Type: text/html\r");
	puts("\r");
	puts("Hello, world!");

	if (0 == (pid = fork())) {
	if (-1 == daemon(1, 0))
	err(EXIT_FAILURE, NULL);
	execl(argv[0], argv[0], "-X", (char *)NULL);
	err(EXIT_FAILURE, "%s", argv[0]);
	} else if (-1 == pid)
	err(EXIT_FAILURE, NULL);

	return(EXIT_SUCCESS);
}
