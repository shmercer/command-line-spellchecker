package com.gsaqui.checker

import org.languagetool.JLanguageTool
import org.languagetool.Language
import org.languagetool.language.AmericanEnglish
import org.languagetool.language.CanadianEnglish
import org.languagetool.rules.CategoryId
import org.languagetool.rules.RuleMatch
import org.languagetool.rules.spelling.SpellingCheckRule
import org.languagetool.rules.spelling.hunspell.Hunspell

class Checker {

    Language dictionary
    String pathOfFiles
    String outputFile
    JLanguageTool languageTool
    Hunspell.Dictionary hunsDictionary

    Checker(Language dict, String path, String output){
        this.dictionary = dict
        this.pathOfFiles = path
        this.outputFile = output

        languageTool = new JLanguageTool(this.dictionary)
        println Hunspell.libName()
        hunsDictionary = Hunspell.getInstance().getDictionary('/Users/gsaqui/workspace/side-project/commandline-spellchecker/libs/dict/en_CA')



        File basedirectory = new File(this.pathOfFiles)
        if(!basedirectory.exists()){
            println  "Base directory doesnt exist - found: $path"
            System.exit(-1)
        }

        basedirectory.eachFileRecurse { File f ->
            if(f.isFile() && f.name.contains('txt')){
                ErrorCount count = new ErrorCount()
                String text = f.text
                text = text.replaceAll("[\\u2018\\u2019]", "'")
                        .replaceAll("[\\u201C\\u201D]", "\"")
                count.grammaticalErrors = checkGrammar(text)
                def (int spellingErrors, String updatedText) = spelling(text)

                count.spellingErrors = spellingErrors
                println "${f.name} : "+ updatedText
                println "${f.name} : "+ count.grammaticalErrors +', '+count.spellingErrors
                File outputFile = new File(f.parentFile.path, f.name.tokenize('.').first()+'_c.txt')
                outputFile.text = updatedText

//                outputFile.text = updatedText

            }
        }
    }

    static void main(String[] args) {


        def cli = new CliBuilder(usage: 'checker.jar -[DhPO] ')
        // Create the list of options.
        cli.with {
            h longOpt: 'help', 'Show usage information'
            'D' longOpt: 'dictionary', args:1, argName:'data',  'Which dictionary to use (EN_CA or EN_US)'
            'P' longOpt: 'path',  args:1, argName:'path', 'Path to input files'
            'O' longOpt: 'outputFileName', args:1, argName:'output', 'Output file name'
        }

        def options = cli.parse(args)
        if (!options) {
            return
        }
        // Show usage text when -h or --help option is used.
        if (options.h) {
            cli.usage()
            return
        }

        Language dict = new CanadianEnglish()
        if(options.D || options.dictionary){
            String d = options.D ? options.D : options.dictionary
            if(d.toLowerCase().contains('us')){
                dict = new AmericanEnglish()
            }
        }

        String path = '.'
        if(options.P || options.path){
            path = options.P ? options.P : options.path
        }

        String output = 'Output.tmp'
        if(options.O || options.output){
            output = options.O ? options.O : options.outputFileName
        }



        //find all files in directory
        Checker checker = new Checker(dict, path, output)
    }


    private int checkGrammar(String text) {
//        languageTool.disableCategory(new CategoryId('TYPOS'))
//        languageTool.enableRule('HUNSPELL_RULE')
        languageTool.disableRule('HUNSPELL_RULE')
        languageTool.disableRule('MORFOLOGIK_RULE_EN_CA')
        languageTool.disableRule('MORFOLOGIK_RULE_EN_US')
        List<RuleMatch> matches = languageTool.check(text)

//        RuleMatch match = matches[0]
//
//        matches.each{
//            println it.rule.class.simpleName
//        }
//        println("Potential error at characters " +
//                match.getFromPos() + "-" + match.getToPos() + ": " +
//                match.getMessage());
//        println("Suggested correction(s): " +
//                match.getSuggestedReplacements());
//
//        String firstPart = text.substring(0, match.getFromPos())
//        String endPart = text.substring(match.getToPos(), text.length())
//        text = firstPart+match.getSuggestedReplacements()[0]+endPart
//
        return matches.size()
    }

    private def spelling(String text){
        List newText = []
        int count = 0
//        String dir = "/usr/share/hunspell";
        text.split(/\s/).each{ String word ->
            if(hunsDictionary.misspelled(word)){
                List words = hunsDictionary.suggest(word)
                String newWord = words.size() > 0 ? words.first() : word

                if(word.matches(/.*\p{Punct}$/)){
                    newWord += word.substring(word.length()-1)
                }

                if(word != newWord){
                    println "Wrong "+ word +" new word "+newWord
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
