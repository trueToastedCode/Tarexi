package com.trueToastedCode.Tarexi;

import java.io.IOException;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Combinator extends Thread {

    int name, min, max, threads, maxTimesPerMaxWordCount, maxWordCount;
    ArrayList<String> keywords;
    ArrayList<String> binder;
    String fName;
    boolean active;

    Combinator(int name, int min, int max, int threads, String fName, ArrayList<String> keywords, int maxWordCount, int maxTimesPerMaxWordCount, ArrayList<String> binder, boolean active) {
        this.name = name;
        this.min = min;
        this.max = max;
        this.keywords = keywords;
        this.threads = threads;
        this.fName = fName;
        this.maxWordCount = maxWordCount;
        this.maxTimesPerMaxWordCount = maxTimesPerMaxWordCount;
        this.binder = binder;
        this.active = active;
    }

    public void run() {
        BufferedWriter writer = null;
        if (active) {
            // prepare file and writer
            File file;
            if(threads == 1) {
                file = new File(fName);
            }else {
                file = new File(injectStrForExtension(fName, "_" + name));
            }
            try {
                writer = new BufferedWriter(new FileWriter(file));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // combine
        for(int block=min; block  <= max; block++) {
            // create table
            int[] table = new int[block];
            for(int pos = block-1; pos >= 0; pos--) {
                if(pos == block-1) {
                    table[pos] = name;
                }else {
                    table[pos] = 0;
                }
            }
            // combine current table
            boolean con, con2;
            con = con2 = true;
            while(true) {
                // update table
                for (int pos = block - 1; pos >= 0; pos--) {
                    if(table[pos] > keywords.size()-1) {
                        if(pos == 0) {
                            con = false;
                        }else {
                            int rest = table[pos] % keywords.size();
                            table[pos-1] += table[pos] / keywords.size();
                            table[pos] = rest;
                        }
                    }
                }
                if(con) {
                    if (active) {
                        // check maxWordCount and maxWordsPerWordCount
                        if(maxWordCount != 0) {
                            if(getWordsOverstepped(table, maxWordCount, maxTimesPerMaxWordCount)) {
                                con2 = false;
                            }else {
                                con2 = true;
                            }
                        }
                        // output
                        if(con2) {
                            try {
                                // need to differentiate because of binder algorithm
                                if(block != 1) {
                                    String[] out = new String[binder.size()];
                                    for(int i=0; i < binder.size(); i++) {
                                        out[i] = "";
                                    }

                                    for(int pos=0; pos < block; pos++) {
                                        String word = keywords.get(table[pos]);
                                        for(int i=0; i < binder.size(); i++) {
                                            out[i] += word;
                                            if(pos != block-1) {
                                                out[i] += binder.get(i);
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
                    }
                    // increase table & progress bar
                    table[block-1] += threads;
                    Main.progressBar.addProgress(1);
                }else {
                    break;
                }
            }
        }

        if (active) {
            // close writer
            try {
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // investigate a list ti max word count and how often the limit exists, return true or false if list equates to parameters
    private static boolean getWordsOverstepped(int[] list, int maxWordCount, int maxWordsPerMaxWordCount) {
        ArrayList<Integer> table = new ArrayList<Integer>();
        table.add(list[0]);
        table.add(0);
        int maxWordCounter = 0;
        for(int num : list) {
            for(int pos = 0; pos < table.size(); pos+=2) {
                if(num == table.get(pos)) {
                    table.set(pos+1, Integer.valueOf(table.get(pos+1))+1);
                    int val = table.get(pos+1);
                    if(val > maxWordCount) {
                        return true;
                    }else if(maxWordsPerMaxWordCount != -1 && val == maxWordCount) {
                        maxWordCounter++;
                        if(maxWordCounter > maxWordsPerMaxWordCount) {
                            return true;
                        }
                    }
                    break;
                }else if(pos == table.size()-2) {
                    table.add(num);
                    table.add(0);
                }
            }
        }
        return false;
    }

    // inject a string before the extension of a file name
    private String injectStrForExtension(String base_str, String str_inject) {
        List<String> str_split = Arrays.asList(base_str.split("\\."));
        String extension = "." + str_split.get(str_split.size()-1);
        String new_str = "";
        for(int x = 0; x < base_str.length()-extension.length(); x++) {
            new_str = new_str + base_str.charAt(x);
        }
        new_str = new_str + str_inject + extension;
        return new_str;
    }

}
