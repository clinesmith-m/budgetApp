JFLAGS = -g
.SUFFIXES: .java .class
.java.class:
	javac $(JFLAGS) $*.java

CLASSES = src/CategoryManager.java src/TransactionLogger.java src/TestClass.java\
        src/MonthlyManager.java src/Main.java

default: compile

run: compile
	java src.Main

compile: $(CLASSES:.java=.class)

clean:
	rm src/*.class
