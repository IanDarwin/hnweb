/*
 * Header for twivial SMTP sender.
 */
extern int setup_email(char *server);

extern int send_email(const char *sender_name, const char *sender_email, const char *subject, const char *message);

extern void end_email();

#ifdef	_SM_PRIVATE
int getreply(FILE *fi);
void sendnext(FILE *fo, const char *line);
#endif
