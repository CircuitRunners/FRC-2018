package org.usfirst.frc.team1002.robot;
import edu.wpi.first.wpilibj.GenericHID;
import edu.wpi.first.wpilibj.IterativeRobot;
import edu.wpi.first.wpilibj.drive.MecanumDrive;
import edu.wpi.first.wpilibj.SpeedController;
import edu.wpi.first.wpilibj.VictorSP;
import edu.wpi.first.wpilibj.XboxController;

public class Robot extends IterativeRobot {
	MecanumDrive robotDrive;

	SpeedController frontLeftMotor = new VictorSP(8);
	SpeedController rearLeftMotor = new VictorSP(6);
	SpeedController frontRightMotor = new VictorSP(7);
	SpeedController rearRightMotor = new VictorSP(9);
	
	final int joystickChannel = 0;

	XboxController stick = new XboxController(joystickChannel);

	public Robot() {
		robotDrive = new MecanumDrive(frontLeftMotor, rearLeftMotor, frontRightMotor, rearRightMotor);
		//robotDrive.setExpiration(0.1);
	}

	public void teleopPeriodic() {
		robotDrive.driveCartesian(stick.getY(GenericHID.Hand.kLeft), stick.getX(GenericHID.Hand.kLeft), stick.getX(GenericHID.Hand.kRight));
	}
}