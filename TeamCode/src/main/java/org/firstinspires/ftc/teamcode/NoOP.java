// this opmode does nothing
package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;

@TeleOp // Defines class as a teleOP routine
public class NoOP extends LinearOpMode {
    /*
     * Motors:
     * - driveMotor1/2/3/4: 312 RPM motors
     * - driveMotor arrangement:
     * - 1====================2
     * - |                    |
     * - 3====================4
     * - intakeMotor: 1150 RPM motor for intake
     * - intakeToShooterMotor: Motor used to feed the flywheel
     * - spinFlywheelMotor: 6000 RPM motor used for spinning the flywheel
     * Servos:
     * - flywheelServo: CRServo used to adjust how big the flywheel is (the black 3d printed part)
     */
    private DcMotor spinFlywheelMotor;
    private DcMotor spinIntakeMotor;
    private DcMotor driveMotor0;
    private DcMotor driveMotor1;
    private DcMotor driveMotor2;
    private DcMotor driveMotor3;
    private DcMotor[] driveMotors;
    private DcMotor intakeToShooterMotor;
    private CRServo flywheelServo;
    @Override
    public void runOpMode() throws InterruptedException { // Main execution function
        // init
        intakeToShooterMotor = hardwareMap.dcMotor.get("intakeToShooterMotor");
        spinFlywheelMotor = hardwareMap.dcMotor.get("spinFlywheelMotor");
        spinIntakeMotor = hardwareMap.dcMotor.get("spinIntakeMotor");
        driveMotor0 = hardwareMap.dcMotor.get("driveMotor0");
        driveMotor1 = hardwareMap.dcMotor.get("driveMotor1");
        driveMotor2 = hardwareMap.dcMotor.get("driveMotor2");
        driveMotor3 = hardwareMap.dcMotor.get("driveMotor3");

        driveMotors = new DcMotor[]{driveMotor0, driveMotor1, driveMotor2, driveMotor3};

        flywheelServo = hardwareMap.crservo.get("flywheelServo");

        driveMotor0.setDirection(DcMotor.Direction.REVERSE); // Left motors
        driveMotor2.setDirection(DcMotor.Direction.REVERSE);
        driveMotor1.setDirection(DcMotor.Direction.FORWARD); // Right motors
        driveMotor3.setDirection(DcMotor.Direction.FORWARD);
        spinFlywheelMotor.setDirection(DcMotorSimple.Direction.REVERSE);

        telemetry.addData("Status", "Initialized");
        telemetry.update();

        waitForStart();

        if (isStopRequested()) return;

        // CONTROL LOOP
        while (opModeIsActive()) {
            telemetry.addData("Status", "Running");
            telemetry.update();
        }
    }
}