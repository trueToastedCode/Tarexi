package com.trueToastedCode.Tarexi;

import java.io.InputStream;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

public class Main {

    static ProgressBar progressBar;

    public static void main(String[] args) {
        // all available options
        ArrayList<String> keywords = new ArrayList<String>();
        ArrayList<String> binder = new ArrayList<String>();;
        int min, max, threads, maxWordCount, maxTimesPerMaxWordCount;
        String fname;

        // output art and info
        InputStream is = Main.class.getResourceAsStream("art.txt");
        System.out.println(convertStreamToString(is));
        System.out.println("\n                Tarexi@BETA3 by trueToastedCode\n");

        // get user choices
        min = Integer.valueOf(input("min: "));
        max = Integer.valueOf(input("max: "));
        threads = Integer.valueOf(input("threads: "));
        fname = input("file name: ");

        // set extension if needed
        for(int pos=fname.length()-1; pos >=0; pos--) {
            String ch = String.valueOf(fname.charAt(pos));
            if(ch.intern() == ".") {
                break;
            }else if(ch.intern() == "\\" || ch.intern() == "/" || pos == 0) {
                fname = fname.substring(0, fname.length()) + ".txt";
                System.out.println("fname -> " + fname);
                break;
            }
        }

        maxWordCount = Integer.valueOf(input("maxWordCount(0 to deactivate): "));
        maxTimesPerMaxWordCount = Integer.valueOf(input("maxTimesPerMaxWordCount(-1 to deactivate): "));
        System.out.println("seperate with ;");
        for(String str : input("keywords: ").split(";")) {
            keywords.add(str);
        }
        if(input("create more words from keyword(y/n): ").intern() == "y") {
            progressBar = new ProgressBar(BigInteger.valueOf(keywords.size()));
            System.out.println("\ncreating..");
            progressBar.start();
            keywords.addAll(createMoreWords(keywords));
            while (progressBar.isAlive()) { }
        }
        if(input("\nset binders(y/n): ").intern() == "y") {
            System.out.println("seperate with ;");
            String in = input("binders: ");
            String[] split = in.split(";");
            for(String str : split) {
                binder.add(str);
            }
        }
        binder.add("");

        // start combiner
        System.out.println("\ncombining..");

        BigInteger combinations = BigInteger.ZERO;
        for(int block = min; block <= max; block++) {
            combinations = combinations.add(BigInteger.valueOf(keywords.size()).pow(block));
        }

        progressBar = new ProgressBar(combinations);
        progressBar.start();

        Combinator combinator;
        for(int i=0; i<threads; i++) {
            combinator = new Combinator(i, min, max, threads, fname, keywords, maxWordCount, maxTimesPerMaxWordCount, binder);
            combinator.start();
        }

    }

    public static String convertStreamToString(java.io.InputStream is) {
        java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }

    private static String input(String msg) {
        Scanner sc = new Scanner(System.in);
        System.out.print(msg);
        String in = sc.nextLine();
        return in;
    }

    private static ArrayList<String> createMoreWords(ArrayList<String> keywords) {
        ArrayList<String> moreWords = new ArrayList<String>();
        for (String str : keywords) {
            for(int currentSplitLenght=2; currentSplitLenght <= str.length()-1; currentSplitLenght++) {
                for (int strPos = 0; strPos < str.length(); strPos++) {
                    if (strPos + currentSplitLenght <= str.length()) {
                        String newWord = "";
                        for (int strPos2 = strPos; strPos2 < strPos + currentSplitLenght; strPos2++) {
                            newWord += String.valueOf(str.charAt(strPos2));
                        }
                        moreWords.add(newWord);
                        Character ch = newWord.charAt(0);
                        if(Character.isLetter(ch)) {
                            if(Character.isUpperCase(ch)) {
                                moreWords.add(Character.toLowerCase(ch) + newWord.substring(1, newWord.length()));
                            }else {
                                moreWords.add(Character.toUpperCase(ch) + newWord.substring(1, newWord.length()));
                            }
                        }
                    } else {
                        break;
                    }
                }
            }
            progressBar.addProgress(1);
        }
        return moreWords;
    }

}
