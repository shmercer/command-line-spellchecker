# command-line-spellchecker
Command line tool to recursively look for .txt files and count the grammar mistakes and count and fix the spelling mistakes

## Running Code

- Download the checker-all.jar file
- Download and store in a directory all the dictionary files https://github.com/gsaqui/command-line-spellchecker/tree/master/libs/dict
- Make sure you have java 8+ on your machine
- run `java -jar checker-all.jar <path of the base directory that holds all the data files> <output file name eg output.csv> <dictionary to use>`
   - example : `java -jar checker-all.jar datafiles/ ca-results.csv dict/en_CA.dic`


## Building the source

- Have jdk 8+ installed
- Install Gradle (https://gradle.org/install/)
- `git clone` the repo
- From the root directory run `gradle.bat shadowJar`
- The newly craeted jar file will be in `build\libs`

If you want to change the version of hunspell you'll need to go to their site and download the newest dll.  If you want
to update to the newest LanguageTool just modify build.gradle.