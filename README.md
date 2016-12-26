# xsdiff
Compare XML XSD schemas providing simple and straight differences report

## How to build

`mvn clean install` is all you need


## How to run

To compare control schemas in `a/` folder vs new schema in `b/` folder, run:
~~~~
java -jar XsDiff-app/target/xsdiff-app-1.0.0.jar a/ b/
~~~~

You need to have schema.lst file in `b/` folder, that lists the files to compare.

Like this:
~~~~
f1.xsd
f2.xsd
~~~~

`report-yyyy-MM-dd` folder will be created to hold generate html report files.
