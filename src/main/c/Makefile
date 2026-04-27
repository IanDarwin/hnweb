all:	membership-mailer.c smtp.c
	cc -static -o membership-mailer membership-mailer.c smtp.c `pkg-config --libs kcgi-html` -I/usr/local/include
install:	membership-mailer
	doas install -m 0555 membership-mailer /var/www/cgi-bin
smtest:	smtp.c
	cc -DDEBUG -o smtest smtp.c

clean:
	@rm -f *.o membership-mailer smtest
