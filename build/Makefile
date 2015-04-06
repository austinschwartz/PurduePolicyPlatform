JCC = javac

# libraries
SOURCE = ../src/
OUT = .
LIB = ..:$(OUT):$(JARS)
JARS = $(shell echo $(wildcard ../lib/*) | tr " " ":")

# pattern rule
.SUFFIXES: .java .class

.java.class:
	$(JCC) -sourcepath $(SOURCE) -classpath $(LIB) -d $(OUT) $<


# sources
CLASSES = \
	../src/HtmlParser.java \
	../src/Bing.java \
	../src/KeywordReader.java \
	../src/FileTypeFilter.java \
	../src/Parser.java \
	../src/GUI.java 


default: classes

classes: $(CLASSES:.java=.class)

clean:
	$(RM) ../bin/*.class

run:
	java -cp $(JARS):. GUI
	
