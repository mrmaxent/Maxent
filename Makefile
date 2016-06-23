onefourtwofiles = `ls density/*.java | grep -v ParallelRun`

compile:
	/usr/common/jdk1.5.0_06/bin/javac -g density/ParamsPre.java
	java density.ParamsPre typesafe > density/Params.java
	/usr/common/jdk1.5.0_06/bin/javac -g density/*.java density/tools/*.java
	java density.Params density/*.java
	cat density/help.html.pre > density/help.html; java density.Params write >> density/help.html; echo "</blockquote><br></body></html>" >> density/help.html
	javadoc -d html -subpackages density
	zip maxentdoc.zip html/density/Runner.html html/density/Params.html html/density/ParamsPre.html html/density/Evaluate.html

distribution: compile
	jar cvfm maxent.jar density/mc.mf density/*.class density/*.html gnu/getopt/* gui/layouts/*.class com/mindprod/ledatastream/LEData*.class density/tools/*.class ptolemy/plot/*.class density/parameters.csv csv/CsvWriter.class
	zip maxent.zip maxent.jar readme.txt maxent.bat

jar: compile
	jar cvfm maxent.jar density/mc.mf density/*.class density/*.html density/*.java gnu/getopt/* gui/layouts/* density/tools/*.java density/tools/*.class com/mindprod/ledatastream/LEData*.class ptolemy/plot/*.class density/parameters.csv density/mc.mf csv/CsvWriter.java csv/CsvWriter.class
	echo -n "mv maxent.jar maxentJarFiles/maxent_"; grep version density/Utils.java | sed -e 's/.*= "//' -e 's/".*/.withSrc.jar; make distribution/' | cat
	echo "Check that readme.txt is in DOS format -- open with Notepad."

class:
	jar cvf classify.jar classify/*.class classify/*.java gnu/getopt/* density/DoubleIndexSort.class

clean:
	rm density/*.class

PMedianRand.jar:
	mkdir tmp;
	cp density/tools/*.class tmp; cd tmp; ln -s ../density density; jar cvf PMedianRand.jar density/*.class *.class; cp PMedianRand.jar ..; cd ..; rm -r tmp
