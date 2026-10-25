#!/usr/bin/awk -f

# split up entries like "Bob + Josey" "Smith"
# Use psql & vi to create input lines like: 
# 743|Bob|Mary|Smith
# e.g., remove ALL spaces except in names like 'Billy Bob'
# Make sure no blank line at end.

BEGIN {
	OFS=""
}
NF=4 {
	id=$1
	master_firstname=$2
	secondary_firstname=$3
	lastname=$4

print "select id,membertype,firstname,lastname,expiryyear from member where firstname like '% and %';"
print "insert into member(firstname,lastname,membertype,mastermember_id)" \
	"values('" secondary_firstname "', '" lastname "','Secondary', " id ");"
print "update member set firstname = '" master_firstname "' where id = " id ";"
print "update member set mastermember_id = " id " where id = " \
	"(select id from member where firstname = '" secondary_firstname "' and lastname = '" lastname "');"
print "update member set expiryyear = 2025 where id = " id ";"
print "select id,firstname,lastname,expiryyear,membertype,mastermember_id from member where lastname = '" lastname "';"
print ""
}
