package org.usfirst.frc.team1002.robot;

import edu.wpi.first.wpilibj.ADXRS450_Gyro;
import edu.wpi.first.wpilibj.Encoder;
import edu.wpi.first.wpilibj.GenericHID;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.drive.MecanumDrive;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

public class MarioDrive {

	private static MecanumDrive marioDrive;
	private double x, y, t;

	private static double smooth(double value) {

		if (value > 0.9)
			return 1;
		else if (value < 0.15 && value > -0.15)
			return 0;
		else if (value < -0.9) {
			return -1;
		} else
			return Math.sin(value);
	}

	ADXRS450_Gyro gyro;
	Encoder encL;
	Encoder encR;
	
	public MarioDrive() {

		marioDrive = new MecanumDrive(RobotData.kFrontLeft, RobotData.kBackLeft, RobotData.kFrontRight,
				RobotData.kBackRight);

		gyro = new ADXRS450_Gyro();
		encL = new Encoder(RobotData.enc1PortA, RobotData.enc1PortB, false, Encoder.EncodingType.k4X);
		encR = new Encoder(RobotData.enc2PortA, RobotData.enc2PortB, false, Encoder.EncodingType.k4X);
		encR.setDistancePerPulse(RobotData.driveDistancePerPulse);
		encL.setDistancePerPulse(RobotData.driveDistancePerPulse);
	}

	int count = 0;
	double prev_x = 0.0;
	double prev_y = 0.0;
	double prev_t = 0.0;
	static final double protectedConstant = 20;
	static final double maxSpeedDiff = 0.30;

	public void teleOp() {

		x = smooth(Robot.driver.getY(GenericHID.Hand.kLeft));
		y = smooth((Robot.driver.getX(GenericHID.Hand.kLeft) * -1));
		if (!Robot.driver.getBackButton()) {
			t = smooth((Robot.driver.getX(GenericHID.Hand.kRight) * -1));
		} else {
			t = 0;
		}
		if (Math.abs(x - prev_x) > maxSpeedDiff) {
			x = (prev_x + (x - prev_x) / protectedConstant);
		}
		if (Math.abs(y - prev_y) > maxSpeedDiff) {
			y = (prev_y + (y - prev_y) / protectedConstant);
		}
		if (Math.abs(t - prev_t) > maxSpeedDiff) {
			t = (prev_t + (t - prev_t) / protectedConstant);
		}

		marioDrive.driveCartesian(y, x, t);
		prev_x = x;
		prev_y = y;
		prev_t = t;
		SmartDashboard.putNumber("teleCount", count++);
		SmartDashboard.putNumber("Controller Left/Right", x);
		SmartDashboard.putNumber("Controller Forward/Reverse", -y);
		SmartDashboard.putNumber("Controller Twist", t);
		SmartDashboard.putNumber("Gyro Count Degrees", gyro.getAngle());
	}

	public void autoDrive(double speed, double time, double dist) {
		speed *= -1;
		boolean forward = true;
		double currentTime = Timer.getFPGATimestamp();
		double endTime = Timer.getFPGATimestamp() + time;
		double desiredDistance = dist;
		encL.reset();
		encR.reset();
		double desiredHeading = gyro.getAngle();
		if (dist < 0)
			forward = false;
		if (!forward)
			speed *= -1;

		while (currentTime < endTime) {
			if (Math.min(Math.abs(encL.getDistance()), Math.abs(encR.getDistance())) > Math.abs(desiredDistance))
				break;
			double currentHeading = gyro.getAngle();
			double degChange = ((desiredHeading - currentHeading) * -1) / 50;
			marioDrive.driveCartesian(0.0, speed, degChange);
			currentTime = Timer.getFPGATimestamp();
			SmartDashboard.putNumber("Degree Change", degChange);
			SmartDashboard.putNumber("Current Heading", currentHeading);
			SmartDashboard.putNumber("Desired Heading", desiredHeading);
			SmartDashboard.putNumber("Left Encoder Count", encL.get());
			SmartDashboard.putNumber("Left Encoder Distance", encL.getDistance());
			SmartDashboard.putNumber("Right Encoder Count", encR.get());
			SmartDashboard.putNumber("Right Encoder Distance", encR.getDistance());
			Timer.delay(0.05);
			// wait 5ms to avoid hogging CPU cycles.
		}
		marioDrive.driveCartesian(0.0, 0.0, 0.0);
	}

	public void autoTurn(double turn, double time) {
		double currentTime = Timer.getFPGATimestamp();
		double endTime = Timer.getFPGATimestamp() + time;

		double desiredHeading = turn;

		while (currentTime < endTime) {
			double twist = 0.0;
			double currentHeading = gyro.getAngle();
			double degChange = (desiredHeading - currentHeading);
			if (Math.abs(degChange) < 1)
				break;

			if (degChange < 0.0)
				twist = 0.3;
			else
				twist = -0.3;

			marioDrive.driveCartesian(0.0, 0.0, twist);
			currentTime = Timer.getFPGATimestamp();
			Timer.delay(0.05);
			// wait 5ms to avoid hogging CPU cycles.
		}
	}

}
