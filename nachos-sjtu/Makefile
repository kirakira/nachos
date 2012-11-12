JC = javac -d bin -cp bin
.PHONY: all

all:
	mkdir -p bin
	find . -name "*.java" > sources.txt
	$(JC) @sources.txt
