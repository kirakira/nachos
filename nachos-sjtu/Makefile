JC = javac -d bin -cp bin
.PHONY: all

all:
	find . -name "*.java" > sources.txt
	$(JC) @sources.txt
