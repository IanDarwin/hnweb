install:
	git pull
	mwd

# Run Integration tests
IT:
	mvn --color never -Pit test-compile failsafe:integration-test failsafe:verify

# Generate list, reformat for compact landscape, and send to printer
members:
	ssh dos git-webs/hnweb/scripts/memberlist | scripts/print-member-list
