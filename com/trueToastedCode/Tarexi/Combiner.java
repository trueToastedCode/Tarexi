package com.trueToastedCode.Tarexi;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;

public class Combiner extends Thread {

    private int entryNum, min, max, maxWord, timesMaxWord, addVal, similesNearby, timesSimilesNearby;
    private String fName;
    private ArrayList<String> keywords, binders;
    private BigInteger progress;
    private boolean filterOn = true;

    public Combiner(int entryNum, int addVal, int min, int max, String fName, ArrayList<String> keywords, int maxWordCount, int maxCountPerMaxWordCount, int similesNearby, int timesSimilesNearby, ArrayList<String> binders) {
        this.entryNum = entryNum;
        this.addVal = addVal;
        this.min = min;
        this.max = max;
        this.maxWord = maxWordCount;
        this.timesMaxWord = maxCountPerMaxWordCount;
        this.similesNearby = similesNearby;
        this.timesSimilesNearby = timesSimilesNearby;
        this.fName = fName;
        this.keywords = keywords;
        this.binders = binders;
        progress = BigInteger.ZERO;
        //System.out.println("entryNum: " + entryNum + ", addVal: " + addVal);

        /*System.out.println("entryNum: " + entryNum);
        System.out.println("addVal: " + addVal);
        System.out.println("min: " + min);
        System.out.println("max: " + max);
        System.out.println("fName: " + fName);
        System.out.println("keywords: " + keywords);
        System.out.println("maxWordCount: " + maxWordCount);
        System.out.println("maxCountPerMaxWordCount: " + maxCountPerMaxWordCount);
        System.out.println("similesNearby: " + similesNearby);
        System.out.println("timesSimilesNearby: " + timesSimilesNearby);
        System.out.println("binders: " + binders);*/

        if (maxWord == -1 && similesNearby == -1)
            filterOn = false;
    }

    @Override
    public void run() {
        // prepare file and writer
        File file = new File(fName);
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(file));
        } catch (IOException e) {
            e.printStackTrace();
        }

        // combine
        for(int block=min; block <= max; block++) {
            // create table
            int[] table = new int[block];
            for(int pos = block-1; pos >= 0; pos--) {
                if(pos == block-1) {
                    table[pos] = entryNum;
                }else {
                    table[pos] = 0;
                }
            }

            boolean con, con2;
            con = con2 = true;

            while (true) {
                // update table
                for (int pos = block - 1; pos >= 0; pos--) {
                    if (table[pos] > keywords.size() - 1) {
                        if (pos == 0) {
                            con = false;
                        } else {
                            int rest = table[pos] % keywords.size();
                            table[pos - 1] += table[pos] / keywords.size();
                            table[pos] = rest;
                        }
                    }
                }
                if (!con)
                    break;

                // check maxWordCount and maxWordsPerWordCount
                if (filterOn) {
                    con2 = checkFilter(table, block);
                }

                if (con2) {
                    try {
                        // need to differentiate because of binder algorithm
                        if(block != 1) {
                            String[] out = new String[binders.size()];
                            for(int i=0; i < binders.size(); i++) {
                                out[i] = "";
                            }

                            for(int pos=0; pos < block; pos++) {
                                String word = keywords.get(table[pos]);
                                for(int i=0; i < binders.size(); i++) {
                                    out[i] += word;
                                    if(pos != block-1) {
                                        out[i] += binders.get(i);
                                    }
                                }
                            }

                            for(String str : out) {
                                writer.write(str);
                                writer.write("\n");
                            }
                        }else {
                            writer.write(keywords.get(table[0]));
                            writer.write("\n");
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                // increase table & progress bar
                table[block-1] += addVal;
                progress = progress.add(BigInteger.ONE);
            }
        }
        // close writer
        try {
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public BigInteger getProgress() {
        return progress;
    }

    private boolean checkFilter(int[] comb, int combLen) {
        ArrayList<Integer> characters = new ArrayList();
        boolean found = false;

        int lastCharac = comb[0], characInRowCounter = 0, maxCharcInRowCounter = 0, TimesMaxWordCounter = 0, val, charactersLen;

        for (int i0=0; i0<combLen; i0++) {

            if (similesNearby != -1) {
                // check for similesNearby & TimesSimilesNearby
                if (lastCharac == comb[i0]) {
                    if (characInRowCounter == similesNearby) {
                        // similesNearby
                        //System.out.println("similesNearby");
                        return false;
                    }
                    characInRowCounter++;

                    if (timesSimilesNearby != -1 && characInRowCounter == similesNearby) {
                        if (maxCharcInRowCounter == timesSimilesNearby) {
                            // TimesSimilesNearby
                            //System.out.println("TimesSimilesNearby");
                            return false;
                        }
                        maxCharcInRowCounter++;
                    }
                }else {
                    characInRowCounter = 1;
                    lastCharac = comb[i0];
                }
            }

            if (maxWord != -1) {
                // check maxWord & TimesMaxWord
                charactersLen = characters.size();

                for (int i1=0; i1<charactersLen; i1+=2) {
                    if (comb[i0] == characters.get(i1)) {
                        val = characters.get(i1 + 1) + 1;

                        if (val == maxWord) {
                            if (timesMaxWord != -1 && TimesMaxWordCounter == timesMaxWord) {
                                // timesMaxWord
                                //System.out.println("timesMaxWord");
                                return false;
                            }
                            TimesMaxWordCounter++;
                        }else if (val > maxWord) {
                            // maxWord
                            //System.out.println("maxWord");
                            return false;
                        }

                        characters.set(i1+1, val);
                        found = true;
                        break;
                    }
                }

                if (found) {
                    found = false;
                }else {
                    characters.add(comb[i0]);
                    characters.add(1);
                }
            }
        }

        return true;
    }
}
