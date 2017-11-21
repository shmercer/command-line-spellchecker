package com.gsaqui.checker

import org.languagetool.JLanguageTool
import org.languagetool.Language
import org.languagetool.language.AmericanEnglish
import org.languagetool.language.CanadianEnglish
import org.languagetool.rules.RuleMatch
import org.languagetool.rules.spelling.hunspell.Hunspell

class Checker {

    Language dictionary
    String pathOfFiles
    String outputFile
    JLanguageTool languageTool
    Hunspell.Dictionary hunsDictionary

    Checker(Language dict, String path, String output, File dictionaryFile) {
        this.dictionary = dict
        this.pathOfFiles = path
        this.outputFile = output

        languageTool = new JLanguageTool(this.dictionary)
        println Hunspell.libName()
        hunsDictionary = Hunspell.getInstance().getDictionary(dictionaryFile.getAbsolutePath().replace('.dic', '').replace('.aff', ''))


        File baseDirectory = new File(this.pathOfFiles)
        if (!baseDirectory.exists()) {
            println "Base directory doesnt exist - found: $path"
            System.exit(-1)
        }

        File csvFile = new File(output)
        csvFile.text = '"Filename", "Grammar Errors", "Spelling Error"\n'
        println csvFile.getAbsolutePath()

        baseDirectory.eachFileRecurse { File f ->
            if (f.isFile() && f.name.contains('txt') && !f.name.contains('_c.txt')) {
                ErrorCount count = new ErrorCount()
                String text = f.text
                text = text.replaceAll("[\\u2018\\u2019]", "'")
                        .replaceAll("[\\u201C\\u201D]", "\"")
                f.text = text
                count.grammaticalErrors = checkGrammar(text)
                def (int spellingErrors, String updatedText) = spelling(text)

                count.spellingErrors = spellingErrors
//                println "${f.name} : " + updatedText
//                println "${f.name} : " + count.grammaticalErrors + ', ' + count.spellingErrors
                File outputFile = new File(f.parentFile.path, f.name.tokenize('.').first() + '_c.txt')
                outputFile.text = updatedText

                csvFile.text += '"' + f.name + '", ' + "$count.grammaticalErrors, $count.spellingErrors\n"

            }
        }
    }

    static void main(String[] args) {

        if (args.length != 3) {
            println "java -jar checker.jar <path to data files> <output filename (output.csv)> <path to dictionary file> "
            System.exit(1)
        }

        Language dict = new CanadianEnglish()
        if (args[2].toLowerCase().contains('en_us')) {
            dict = new AmericanEnglish()
        }

        File dictionaryFile = new File(args[2])
        if(!dictionaryFile.exists()){
            println "Unable to find file: "+dictionaryFile.absolutePath
            System.exit(1)
        }


        String path = args[0]
        String output = args[1]

        println "path=$path, output=$output, dictionary=${dictionaryFile.absolutePath}"

        //find all files in directory
        new Checker(dict, path, output, dictionaryFile)
    }


    private int checkGrammar(String text) {
//        languageTool.disableCategory(new CategoryId('TYPOS'))
//        languageTool.enableRule('HUNSPELL_RULE')
        languageTool.disableRule('HUNSPELL_RULE')
        languageTool.disableRule('MORFOLOGIK_RULE_EN_CA')
        languageTool.disableRule('MORFOLOGIK_RULE_EN_US')
        List<RuleMatch> matches = languageTool.check(text)
        return matches.size()
    }

    private def spelling(String text) {
        List newText = []
        int count = 0
        text.split(/\s/).each { String word ->
            if (hunsDictionary.misspelled(word)) {
                List words = hunsDictionary.suggest(word)
                String newWord = words.size() > 0 ? words.first() : word

                if (word.matches(/.*\p{Punct}$/) && !newWord.matches(/.*\p{Punct}$/)) {
                    newWord += word.substring(word.length() - 1)
                }

                if (word != newWord) {
//                    println "Wrong " + word + " new word " + newWord
                    newText << newWord
                    count++
                } else {
                    newText << word
                }

            } else {
                newText << word
            }
        }

        return [count, newText.join(' ')]
    }

}
