package com.trueToastedCode.Tarexi;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        ArrayList<String> keywords, binders;
        int min, max, maxWordCount, maxCountPerMaxWordCount=-1, splits, threads, activeSplits[] = null, similesNearby, timesSimilesNearby=-1;
        String fName;

        System.out.println(convertStreamToString(Main.class.getResourceAsStream("art.txt")));
        System.out.println("\n              Tarexi@BETA3.3 by trueToastedCode\n");

        min = Integer.valueOf(input("Min: "));
        max = Integer.valueOf(input("Max: "));
        threads = Integer.valueOf(input("Threads: "));

        splits = Integer.valueOf(input("Splits: "));

        if (splits == 0) {
            activeSplits = new int[threads];
            for (int i=0; i<threads; i++)
                activeSplits[i] = i;
            splits = threads;
        }else {
            String strActiveSplits = input("Active splits (1-" + splits + ", 0 for all): ");
            if (strActiveSplits.intern() == "0") {
                activeSplits = new int[splits];
                for (int i=0; i<splits; i++)
                    activeSplits[i] = i;
            }else if (strActiveSplits.contains("-")) {
                System.out.println("Negative's are invalid!");
                System.exit(1);
            }else {
                activeSplits = new int[strActiveSplits.length()];
                for (int i=0; i<strActiveSplits.length(); i++) {
                    activeSplits[i] = Integer.valueOf(String.valueOf(strActiveSplits.charAt(i))) - 1;
                    if (activeSplits[i] >= splits) {
                        System.out.println((activeSplits[i] + 1) + " is invalid!");
                        System.exit(1);
                    }
                }
            }
        }

        if (activeSplits.length > threads) {
            System.out.print("Every active split needs at least one thread!");
            System.exit(1);
        }

        fName = input("File name: ");

        maxWordCount = Integer.valueOf(input("maxWordCount(-1 to deactivate): "));
        if (maxWordCount != -1) {
            maxCountPerMaxWordCount = Integer.valueOf(input("maxTimesMaxWordCount(-1 to deactivate): "));
        }

        similesNearby = Integer.valueOf(input("maxSimilesNearby (-1 to deactivate): "));
        if (similesNearby != -1) {
            timesSimilesNearby = Integer.valueOf(input("maxTimesSimilesNearby (-1 to deactivate): "));
        }

        System.out.println("Seperate with ;");
        keywords = new ArrayList<String>();

        for (String str : input("Keywords: ").split(";")) {
            keywords.add(str);
        }

        if (input("Create more words from keyword (Splits the keywords in all possible sizes and adds the slitted and the splitted with changed case of first letter to keywords) (y/n): ").intern() == "y") {
            ArrayList<String> moreWords = new ArrayList<String>();
            for (String str : keywords) {
                for (int currentSplitLenght=2; currentSplitLenght <= str.length()-1; currentSplitLenght++) {
                    for (int strPos = 0; strPos < str.length(); strPos++) {
                        if (strPos + currentSplitLenght <= str.length()) {
                            String newWord = "";
                            for (int strPos2 = strPos; strPos2 < strPos + currentSplitLenght; strPos2++) {
                                newWord += String.valueOf(str.charAt(strPos2));
                            }
                            moreWords.add(newWord);
                            Character ch = newWord.charAt(0);
                            if (Character.isLetter(ch)) {
                                if (Character.isUpperCase(ch)) {
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
            }

            keywords.addAll(moreWords);
            System.out.println("Added!");
        }

        binders = new ArrayList<String>();
        binders.add("");
        if(input("Set binders (y/n): ").intern() == "y") {
            System.out.println("Seperate with ;");
            String in = input("Binders: ");
            String[] split = in.split(";");
            for (String str : split) {
                binders.add(str);
            }
        }

        // set substring parts
        boolean pointFound = false;
        int subStrNum = -1;
        for (int i=fName.length()-1; i>=0; i--) {
            if (String.valueOf(fName.charAt(i)).intern() == ".") {
                pointFound = true;
                subStrNum = i;
            }
        }
        if (!pointFound) {
            subStrNum = fName.length();
        }

        String[] fNameParts = new String[2];
        fNameParts[0] = fName.substring(0, subStrNum);
        fNameParts[1] = fName.substring(subStrNum, fName.length());

        // set threads per active split
        int threadsPerSplit[] = new int[activeSplits.length];
        int threadCounter = threads;
        while (threadCounter > 0) {
            if (threadCounter >= activeSplits.length) {
                int x = (threads-(threadCounter % activeSplits.length)) / activeSplits.length;
                for (int i=0; i<threadsPerSplit.length; i++)
                    threadsPerSplit[i] = x;
                threadCounter -= x * threadsPerSplit.length;
            }else {
                for (int i=0; i<threadsPerSplit.length && threadCounter > 0; i++) {
                    threadsPerSplit[i] += 1;
                    threadCounter -= 1;
                }
            }
        }

        System.out.println("\nThread classification:");

        int addVals[] = new int[activeSplits.length];
        int startVals[] = new int[threads];
        int y = 0;
        for (int i=0; i<activeSplits.length; i++) {
            System.out.println("Worker " + (activeSplits[i]+1) + ":");
            addVals[i] = splits * threadsPerSplit[i];
            System.out.println("Add value = " + addVals[i]);

            for (int x=0; x<threadsPerSplit[i]; x++) {
                startVals[y] = activeSplits[i] + (splits * x);
                System.out.println("Thread[" + y + "] Start value = " + startVals[y]);
                y++;
            }
            System.out.print("\n");
        }

        BigInteger numComplete = BigInteger.ZERO;

        for (int block = min; block <= max; block++) {
            BigInteger maxVal = BigInteger.valueOf(keywords.size()).pow(block);
            for (int i=0; i<activeSplits.length; i++) {
                for (int x=0; x<threadsPerSplit[i]; x++) {
                    numComplete = numComplete.add(maxVal.subtract(maxVal.remainder(BigInteger.valueOf(addVals[i]))).divide(BigInteger.valueOf(addVals[i])));
                }
            }
        }

        System.out.println(numComplete + " combinations (" + min + "-" + max + ", " + keywords.size() + " keywords) will be generated, checked and potentially written.\n");

        // start combiner
        Combiner combiners[] = new Combiner[threads];
        y = 0;
        for (int i=0; i<activeSplits.length; i++) {
            for (int x=0; x<threadsPerSplit[i]; x++) {
                fName = fNameParts[0] + "_" + (activeSplits[i] + 1) + "_" + x + fNameParts[1];
                combiners[y] = new Combiner(startVals[y], addVals[i], min, max, fName, keywords, maxWordCount, maxCountPerMaxWordCount, similesNearby, timesSimilesNearby, binders);
                y++;
            }
        }

        ProgressBar progressBar = new ProgressBar(combiners, numComplete);
        System.out.println("Combining...");
        progressBar.start();

        for (Combiner combiner : combiners)
            combiner.start();
    }

    private static String input(String msg) {
        Scanner sc = new Scanner(System.in);
        System.out.print(msg);
        String in = sc.nextLine();
        return in;
    }

    private static String convertStreamToString(java.io.InputStream is) {
        java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }
}