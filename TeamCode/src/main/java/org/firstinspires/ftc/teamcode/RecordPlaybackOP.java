// Recording and playback are in the same file FOR NOW
package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.RobotControl;

@TeleOp // This is a TeleOP just for the purpose of recording
// The AutoOP will only play back
public class RecordPlaybackOP extends LinearOpMode {
    /*
     * Motors:
     * - driveMotor0/1/2/3: lstick/rstick
     * - driveMotor arrangement:
     * - 0====================1
     * - |                    |
     * - 2====================3
     * - intakeMotor: RB is forward, RT is backward / both buttons are direction presets / they turn off the motor if already on that preset / they start the motor if it is not started
     * - flywheelMotor: LB hold
     * - kickerWheelMotor: LB hold
     */
    private RobotControl robotControl;
    private Recorder recorder;

    private enum Modes {
        MENU,
        RECORDING,
        POST_RECORDING,
        PLAYBACK,
        POST_PLAYBACK,
        REPOSITION,
        POST_REPOSITION
    }

    private Modes mode;
    private boolean playbackHasStopped;

    private void stepMenu() {
        telemetry.addData("Status", "[Menu] Press (B) to record | Press (A) to play back | Press (Y) to reposition");
        if (gamepad1.bWasPressed()) {
            // recording
            recorder.startRecording();
            mode = Modes.RECORDING;
        } else if (gamepad1.aWasPressed()) {
            // playback
            recorder.startPlayback();
            mode = Modes.PLAYBACK;
        } else if (gamepad1.yWasPressed()) {
            // reposition
            mode = Modes.REPOSITION;
        }
    }

    private void stepRecording() {
        telemetry.addData("Status", "[Recording] Press (B) to stop");
        moveBot();
        if (gamepad1.bWasPressed()) {
            if (!recorder.stopRecording()) {
                telemetry.addData("Status", "Recording could not stop | (B) to continue");
            } else {
                telemetry.addData("Status", "Recording stopped | (B) to continue");
            }
            mode = Modes.POST_RECORDING;
        }
    }

    private void stepPlayback() {
        telemetry.addData("Status", "[Playback] Press (A) to cancel");
        if (gamepad1.aWasPressed() || playbackHasStopped) {
            playbackHasStopped = false;
            if (!recorder.stopPlayback()) {
                telemetry.addData("Status", "Playback could not stop | (A) to continue");
            } else {
                telemetry.addData("Status", "Playback stopped | (A) to continue");
            }
            mode = Modes.POST_PLAYBACK;
        }
    }

    private void stepReposition() {
        telemetry.addData("Status", "[Repositioning] Press (Y) to cancel");
        moveBot();
        if (gamepad1.yWasPressed()) {
            playbackHasStopped = false;
            telemetry.addData("Status", "Repositioning stopped | (Y) to continue");
            mode = Modes.POST_REPOSITION;
        }
    }

    private void stepPostRecording() {
        if (gamepad1.bWasPressed()) {
            mode = Modes.MENU;
        }
    }

    private void stepPostPlayback() {
        if (gamepad1.aWasPressed()) {
            mode = Modes.MENU;
        }
    }

    private void stepPostRepositioning() {
        if (gamepad1.yWasPressed()) {
            mode = Modes.MENU;
        }
    }

    private void moveBot() {
        if (mode == Modes.PLAYBACK) {
            // Yes, all the fields prefixed with recorder.playback are PUBLIC fields.
            // Yes, this is INTENTIONAL. It avoids the overhead of calling a bunch of getter methods instead.
            // step() method signature: public step(float leftStickX, float leftStickY, float rightStickX, boolean leftBumper, boolean rightBumper, float rightTrigger)
            robotControl.step(recorder.playbackLeftStickX, recorder.playbackLeftStickY, recorder.playbackRightStickX, recorder.playbackLeftBumper, recorder.playbackRightBumper, recorder.playbackRightTrigger);
        } else {
            robotControl.step(gamepad1.left_stick_x, gamepad1.left_stick_y, gamepad1.right_stick_x, gamepad1.left_bumper, gamepad1.right_bumper, gamepad1.right_trigger > 0);
        }
    }

    private void playbackStopped() {
        playbackHasStopped = true;
    }

    @Override
    public void runOpMode() throws InterruptedException {
        robotControl = new RobotControl(hardwareMap);

        mode = Modes.MENU;

        playbackHasStopped = false;
        recorder = new Recorder(gamepad1, this::moveBot, this::playbackStopped);

        telemetry.addData("Status", "Initialized");
        telemetry.update();

        waitForStart();

        if (isStopRequested()) return;

        // CONTROL LOOP
        while (opModeIsActive()) {
            switch (mode) {
                case MENU:
                    stepMenu();
                    break;
                case RECORDING:
                    stepRecording();
                    break;
                case PLAYBACK:
                    stepPlayback();
                    break;
                case REPOSITION:
                    stepReposition();
                    break;
                case POST_RECORDING:
                    stepPostRecording();
                    break;
                case POST_PLAYBACK:
                    stepPostPlayback();
                    break;
                case POST_REPOSITION:
                    stepPostRepositioning();
                    break;
                default:
                    telemetry.addData("Status", "ERROR: You are not supposed to see this screen");
            }

            telemetry.update();
        }
    }
}