REM Just.bat
set ver=15
set oldver=14
set package=ledatastream
set qpackage=com\mindprod\%package%


set title=LEDataStream little endian DataInputStream/DataOutputStream
REM no jar

set zipname=E:\mindprod\zips\java\%package%%ver%.zip

del /E  E:\mindprod\zips\java\%package%%oldver%.zip

C:
CD \%qpackage%
javac *.java
describe %package%.use "Precis of %package%"
copy %package%.use E:\mindprod\zips\java\%package%.txt

REM add with full folder names
WZZIP -uP %zipname% C:\%qpackage%\*.class C:\%qpackage%\*.java C:\%qpackage%\*.use C:\%qpackage%\*.bat MasterDistribution.site
describe %zipname% "%title%"

REM -30-


