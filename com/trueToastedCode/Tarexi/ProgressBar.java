package com.trueToastedCode.Tarexi;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;

public class ProgressBar extends Thread {

    private BigDecimal onePerc;
    private BigInteger progress, numComplete;
    private Combiner[] combiners;
    private int sleep = 100;
    private long timeStart;
    private boolean maxTime;
    private String baseMsg = "<%s> %s", baseFill = "█";

    public ProgressBar(Combiner[] combiners, BigInteger numComplete) {
        this.combiners = combiners;
        this.numComplete = numComplete;
        this.progress = BigInteger.ZERO;
        this.onePerc = new BigDecimal(this.numComplete).divide(BigDecimal.valueOf(100));
        maxTime = false;
    }

    @Override
    public void run() {
        String msg;
        timeStart = System.currentTimeMillis();

        while (progress.compareTo(numComplete) == -1) {
            // get progress
            BigInteger bi = progress;
            progress = BigInteger.ZERO;
            for (Combiner combiner : combiners)
                progress = progress.add(combiner.getProgress());
            bi = progress.subtract(bi);
            // print current message
            msg = getMsg(bi);
            System.out.print(msg);
            // sleep
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
            }else if(!maxTime) {
                maxTime = true;
            }
        }
        // print end message
        msg = getMsg(null);
        System.out.println(msg + "\nCompleted!");
    }

    private String getMsg(BigInteger dif) {
        double perc;
        if(progress.compareTo(BigInteger.ZERO) == 0) {
            perc = 0;
        }else {
            perc = new BigDecimal(progress).divide(onePerc, 2 , RoundingMode.HALF_UP).doubleValue();
        }
        String fill = "";

        for(int x = 0; x < 20; x++) {
            if(5*x < perc) {
                fill = fill + baseFill;
            }else {
                fill = fill + " ";
            }
        }

        int x = 0;
        if (dif != null) {
            if (!maxTime) {
                if (sleep == 100) {
                    dif = null;
                }else {
                    x = dif.intValue() / (sleep - 100);
                }
            }
            if (dif != null) {
                x = dif.intValue() / sleep;
            }
        }

        final long timeNow = System.currentTimeMillis();
        String msg = String.format(baseMsg, fill, String.valueOf(perc)) + "%, ";
        if (dif != null) {
            msg += x + " combinations/s, ";
        }
        msg += ((timeNow-timeStart) / 1000) + "s";
        return msg;
    }
}
