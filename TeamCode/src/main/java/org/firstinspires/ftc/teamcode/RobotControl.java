package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;

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
 * Servos:
 * - flywheelGate: Not sure which buttons
 */

public class RobotControl {
    private DcMotor driveMotor0;
    private DcMotor driveMotor1;
    private DcMotor driveMotor2;
    private DcMotor driveMotor3;

    private DcMotor intakeMotor;
    private DcMotor flywheelMotor;
    private DcMotor kickerWheelMotor;
    private Servo flywheelGate;
    public RobotControl(HardwareMap hwMap) {
        driveMotor0 =      hwMap.dcMotor.get("driveMotor0");
        driveMotor1 =      hwMap.dcMotor.get("driveMotor1");
        driveMotor2 =      hwMap.dcMotor.get("driveMotor2");
        driveMotor3 =      hwMap.dcMotor.get("driveMotor3");

        driveMotor0.setDirection(DcMotor.Direction.REVERSE); // Left motors
        driveMotor2.setDirection(DcMotor.Direction.REVERSE);
        driveMotor1.setDirection(DcMotor.Direction.FORWARD); // Right motors
        driveMotor3.setDirection(DcMotor.Direction.FORWARD);

        //intakeMotor =      hwMap.dcMotor.get("intakeMotor");
        //flywheelMotor =    hwMap.dcMotor.get("flywheelMotor");
        //kickerWheelMotor = hwMap.dcMotor.get("kickerWheelMotor");
        //flywheelGate =   z  hwMap.servo.get("flywheelGate");
    }

    // gamepad1.left_stick_x, gamepad1.left_stick_y, gamepad1.right_stick_x, gamepad1.left_bumper, gamepad1.right_bumper, gamepad1.right_trigger
    public void step(float leftStickX, float leftStickY, float rightStickX, boolean leftBumper, boolean rightBumper, boolean rightTrigger) {
        double turn = rightStickX;
        double forward = -leftStickY;
        double strafe = leftStickX;

        double wheelPower0 = forward + strafe + turn;
        double wheelPower1 = forward - strafe - turn;
        double wheelPower2 = forward - strafe + turn;
        double wheelPower3 = forward + strafe - turn;

        double max = Math.max(1.0, Math.max(
                Math.max(wheelPower0, wheelPower1),
                Math.max(wheelPower2, wheelPower3)));

        driveMotor0.setPower(wheelPower0 / max);
        driveMotor1.setPower(wheelPower1 / max);
        driveMotor2.setPower(wheelPower2 / max);
        driveMotor3.setPower(wheelPower3 / max);
    }
}