package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.hardware.Gamepad;

import java.util.Arrays;
import java.util.concurrent.*;

public class Recorder {
    private static final byte TPS = 30; // recording ticks per second
    private static final byte RECORDING_FRAME_SIZE = 2; // how many list items a full frame takes up
    
    /*
    * Recording data is an array that repeats every couple of items
    * Each input is two longs and is used as a bitset where
    * The high long and low long are combined into a 128-bit bitset
    * - Bits 0-31 are the float of gamepad1.left_stick_x
    * - Bits 32-63 are the float of gamepad1.left_stick_y
    * - Bits 64-95 are the float of gamepad1.right_stick_x
    * - Bits 65-127 are the 1-bit booleans of gamepad1.aWasPressed(), gamepad1.bWasPressed(), gamepad1.xWasPressed(), gamepad1.yWasPrssed(), gamepad1.left_bumper, gamepad1.right_bumper, gamepad1.dpadLeftWasPressed(), gamepad1.dpadUpWasPressed(), gamepad1.dpadRightWasPressed(), gamepad1.dpadDownWasPressed(), gamepad1.left_trigger > 0, gamepad1.right_bumper > 0 in that order (12 bits used, 20 bits wasted).
    * recordingData[0] = <high long of input on clock 1>
    * recordingData[1] = <low long of input on clock 1>
    * recordingData[2] = <high long of input on clock 2>
    * recordingData[3] = <low long of input on clock 2>
    */

    /*
    * Theoretically supports 2.3 years worth of recording.
    * (30 tps)*(2 items per tick) = items per second, or how many items take up one section 
    * (4294967296 items (the max value of recordingIndex)) / (60 items per second) = 71,582,788.26666667 seconds worth of max recording time
    * (71,582,788.26666667 seconds) / (60 seconds per minute) = 1,193,046.471111111 minutes worth of max recording time
    * (1,193,046.471111111 minutes) / (60 minutes per hour) = 19,884.10785185185 hours worth of max recording time
    * (19,884.10785185185 hours) / (24 hours per day) = 828.5044938271605 days worth of max recording time
    * (828.5044938271605 days) / (30 days per month) = 27.61681646090535 months worth of max recording time
    * (27.61681646090535 months) / (12 months per year) = 2.301401371742112 years worth of max recording time
    */
    private long[] recordingData;
    private int recordingIndex;
    private int playbackIndex;
    
    private Gamepad gamepad1;
    private ScheduledExecutorService scheduler;

    private long long1;
    private long long2;

    private Runnable playbackCallback;
    private Runnable playbackStoppedCallback;

    private ScheduledFuture<?> future;

    /*
    * These fields are public to avoid the potential overhead of
    * getter methods like getPlaybackLeftStickX()
    * It may not be 100% super clean code with perfect encapsulation, but
    * it definitely helps with performance when you're trying to read these variables
    * 30 times per second from another class
    */
    public float playbackLeftStickX;
    public float playbackLeftStickY;
    public float playbackRightStickX;
    public boolean playbackAWasPressed;
    public boolean playbackBWasPressed;
    public boolean playbackXWasPressed;
    public boolean playbackYWasPressed;
    public boolean playbackLeftBumper;
    public boolean playbackRightBumper;
    public boolean playbackDpadLeftWasPressed;
    public boolean playbackDpadRightWasPressed;
    public boolean playbackDpadUpWasPressed;
    public boolean playbackDpadDownWasPressed;
    public boolean playbackLeftTrigger;
    public boolean playbackRightTrigger;
    
    public Recorder(Gamepad gamepad, Runnable playbackCallback, Runnable playbackStoppedCallback) {
        this.gamepad1 = gamepad;
        this.scheduler = Executors.newScheduledThreadPool(1);
        this.playbackCallback = playbackCallback;
        this.playbackStoppedCallback = playbackStoppedCallback;
    }

    private void stepRecording() {
        // Add 30 seconds more time worth of array size if less than 5 seconds remaining left of time
        if ((recordingData.length - recordingIndex) < (TPS * 5 * RECORDING_FRAME_SIZE)) {
            recordingData = Arrays.copyOf(recordingData, TPS * 30 * RECORDING_FRAME_SIZE);
        }


        // Convert gamepad data to two longs
        long1 = ((long) Float.floatToIntBits(gamepad1.left_stick_x) << 32) | Float.floatToIntBits(gamepad1.left_stick_y);
        long2 = ((long) Float.floatToIntBits(gamepad1.right_stick_x) << 32)  | ((gamepad1.aWasPressed() ? 1 : 0) << 30) |
                ((gamepad1.bWasPressed() ? 1 : 0) << 29) | ((gamepad1.xWasPressed() ? 1 : 0) << 28) |
                ((gamepad1.yWasPressed() ? 1 : 0) << 27) | ((gamepad1.left_bumper ? 1 : 0) << 26) |
                ((gamepad1.right_bumper ? 1 : 0) << 25) | ((gamepad1.dpadLeftWasPressed() ? 1 : 0) << 24) |
                ((gamepad1.dpadUpWasPressed() ? 1 : 0) << 23) | ((gamepad1.dpadRightWasPressed() ? 1 : 0) << 22) |
                ((gamepad1.dpadDownWasPressed() ? 1 : 0) << 21) | ((gamepad1.left_trigger > 0 ? 1 : 0) << 20) |
                ((gamepad1.right_trigger > 0 ? 1 : 0) << 19);
        
        // Record it
        recordingData[recordingIndex++] = long1;
        recordingData[recordingIndex++] = long2;
    }

    public void startRecording() {
        this.recordingData = new long[TPS * 30 * RECORDING_FRAME_SIZE]; // 30sec worth of recording preallocated
        this.recordingIndex = 0;
        future = scheduler.scheduleWithFixedDelay(this::stepRecording, 0, 1000/TPS, TimeUnit.MILLISECONDS);
    }

    public boolean stopRecording() {
        future.cancel(false);
        return true;
/*
        // attempt graceful shutdown
        scheduler.shutdown();

        try {
            // if it takes longer than 5 seconds don't do a graceful shutdown
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow(); 
                
                // wait 5 sec for tasks to respond to being interrupted
                if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                    // pool did not terminate
                    return false;
                }
            }
            return true;
        } catch (InterruptedException ie) {
            // recancel if current thread also interrupted
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
            return true;
        }*/
    }

    private void stepPlayback() {
        if (playbackIndex > recordingIndex) {
            this.playbackStoppedCallback.run();
        }
        long1 = recordingData[playbackIndex++];
        long2 = recordingData[playbackIndex++];

        playbackLeftStickX = Float.intBitsToFloat((int) ((long1 & (((long) Math.pow(2, 32)-1) << 32L)) >> 32));
        playbackLeftStickY = Float.intBitsToFloat((int) (long1 & ((long) Math.pow(2, 32)-1)));
        playbackRightStickX = Float.intBitsToFloat((int) ((long2 & (((long) Math.pow(2, 32)-1) << 32L)) >> 32));
        playbackAWasPressed = (long2 & (1 << 30)) > 0;
        playbackBWasPressed = (long2 & (1 << 29)) > 0;
        playbackXWasPressed = (long2 & (1 << 28)) > 0;
        playbackYWasPressed = (long2 & (1 << 27)) > 0;
        playbackLeftBumper = (long2 & (1 << 26)) > 0;
        playbackRightBumper = (long2 & (1 << 25)) > 0;
        playbackDpadLeftWasPressed = (long2 & (1 << 24)) > 0;
        playbackDpadUpWasPressed = (long2 & (1 << 23)) > 0;
        playbackDpadRightWasPressed = (long2 & (1 << 22)) > 0;
        playbackDpadDownWasPressed = (long2 & (1 << 21)) > 0;
        playbackLeftTrigger = (long2 & (1 << 20)) > 0;
        playbackRightTrigger = (long2 & (1 << 19)) > 0;

        this.playbackCallback.run();
    }

    public void startPlayback() {
        playbackIndex = 0;
        future = scheduler.scheduleWithFixedDelay(this::stepPlayback, 0, 1000/TPS, TimeUnit.MILLISECONDS);
    }

    public boolean stopPlayback() {
        return stopRecording();
    }
}