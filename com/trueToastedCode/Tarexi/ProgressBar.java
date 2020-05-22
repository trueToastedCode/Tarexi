package com.trueToastedCode.Tarexi;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;

public class ProgressBar extends Thread {

    BigDecimal progress, complete, onePerc;
    //String base_msg = "▌ %s ▐ %s";
    String base_msg = "< %s > %s";
    String base_fill = "█";
    long timeStart;
    int sleep = 100;

    public ProgressBar(BigInteger complete) {
        this.progress = BigDecimal.ZERO;
        this.complete = new BigDecimal(complete);
        this.onePerc = this.complete.divide(BigDecimal.valueOf(100));
    }

    @Override
    public void run() {
        String msg;

        timeStart = System.currentTimeMillis();

        while (progress.compareTo(complete) == -1) {
            // print current message
            msg = getMsg();
            System.out.print(msg);
            // sleep 5s
            try {
                Thread.sleep(sleep);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            // clear old message
            for(int x = 0; x < msg.length(); x++) {
                System.out.print("\b");
            }
            if(sleep<5000) {
                sleep += 100;
            }
        }
        // print end message
        msg = getMsg();
        System.out.println(msg + "\nCompleted!");
    }

    public synchronized void addProgress(int value) {
        progress = progress.add(BigDecimal.valueOf(value));
    }

    private String getMsg() {
        double perc;
        if(progress.compareTo(BigDecimal.ZERO) == 0) {
            perc = 0;
        }else {
            perc = progress.divide(onePerc, 2 , RoundingMode.HALF_UP).doubleValue();
        }
        String fill = "";

        for(int x = 0; x < 20; x++) {
            if(5*x < perc) {
                fill = fill + base_fill;
            }else {
                fill = fill + " ";
            }
        }

        final long timeNow = System.currentTimeMillis();

        String msg = String.format(base_msg, fill, String.valueOf(perc)) + "%, " + ((timeNow-timeStart) / 1000) + "s";
        return msg;
    }

}
