
compile:
	javac -g density/ParamsPre.java
	java density.ParamsPre typesafe > density/Params.java
	javac -g density/*.java density/tools/*.java
	java density.Params density/*.java
	cat density/help.html.pre > density/help.html; java density.Params write >> density/help.html; echo "</blockquote><br></body></html>" >> density/help.html
	javadoc -d html density/Runner.java density/Params.java density/ParamsPre.java density/Evaluate.java
	zip maxentdoc.zip html/density/Runner.html html/density/Params.html html/density/ParamsPre.html html/density/Evaluate.html

distribution: compile
	jar cvfm maxent.jar density/mc.mf density/*.class density/*.html gnu/getopt/* gui/layouts/*.class com/mindprod/ledatastream/LEData*.class density/tools/*.class ptolemy/plot/*.class density/parameters.csv
	zip maxent.zip maxent.jar readme.txt maxent.bat

clean:
	rm density/*.class
