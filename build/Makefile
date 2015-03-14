JCC = javac

# libraries
#LIB = .:json-simple-1.1.jar:jsoup-1.7.3.jar:pdfbox-app-1.8.6.jar:commons-codec-1.9.jar:mongo-java-driver-2.12.2.jar
LIB = .:*.jar #include all jar
# pattern rule
.SUFFIXES: .java .class

.java.class:
	$(JCC) -cp $(LIB) $<


# sources
CLASSES = \
	SolrConnector.java \
	HtmlParser.java \
	Bing.java \
	KeywordReader.java \
	FileTypeFilter.java \
	GUI.java


default: classes

classes: $(CLASSES:.java=.class)

clean:
	$(RM) *.class

run:
	java -cp $(LIB) Main
	