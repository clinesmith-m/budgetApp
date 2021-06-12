JFLAGS = -g
.SUFFIXES: .java .class
.java.class:
	javac $(JFLAGS) $*.java

CLASSES = src/Logger.java src/TestClass.java src/Main.java

default: run

run: compile
	java src.Main

compile: $(CLASSES:.java=.class)

clean:
	rm src/*.class
