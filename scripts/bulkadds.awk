#!/bin/sh

# Convert lines like
# Robin Smith foo@bar.com
# into valid SQL creation lines:
# insert into member(firstname,lastname,membertype,email) values('Robin', 'Smith', 'Individual', 'foo@bar.com');
while read first last email
do
	printf "insert into member(firstname,lastname,membertype,email) values(\'%s\', \'%s\', \'Individual\', \'%s\');\n" $first $last $email;
done
