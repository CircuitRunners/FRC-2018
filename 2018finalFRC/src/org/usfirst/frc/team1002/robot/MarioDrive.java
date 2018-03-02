package org.usfirst.frc.team1002.robot;

import edu.wpi.first.wpilibj.ADXRS450_Gyro;
import edu.wpi.first.wpilibj.Encoder;
import edu.wpi.first.wpilibj.GenericHID;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.drive.MecanumDrive;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

public class MarioDrive {
	private static final int IDLE = 0;
	private static final int AUTOTURN = 1;
	private static final int AUTODRIVE = 2;
	private static final int TELEOP = 3;

	int currentJob = IDLE;
	private static MecanumDrive marioDrive;
	private double x, y, t;

	private static double smooth(double value) {

		if (value > 0.9)
			return 1;
		else if (value < 0.22 && value > -0.22)
			return 0;
		else if (value < -0.9) {
			return -1;
		} else
			return Math.sin(value) * (Math.PI / 2);
	}

	ADXRS450_Gyro gyro;
	Encoder encL;
	Encoder encR;

	public MarioDrive() {

		marioDrive = new MecanumDrive(RobotData.driveFrontLeft, RobotData.driveBackLeft, RobotData.driveFrontRight,
				RobotData.driveBackRight);

		gyro = new ADXRS450_Gyro();
		encL = new Encoder(RobotData.driveEnc1PortA, RobotData.driveEnc1PortB, false, Encoder.EncodingType.k4X);
		encR = new Encoder(RobotData.driveEnc2PortA, RobotData.driveEnc2PortB, false, Encoder.EncodingType.k4X);
		encR.setDistancePerPulse(RobotData.driveDistancePerPulse);
		encL.setDistancePerPulse(RobotData.driveDistancePerPulse);
	}

	int count = 0;
	double prev_x = 0.0;
	double prev_y = 0.0;
	double prev_t = 0.0;
	static final double protectedConstant = 20;
	static final double maxSpeedDiff = 0.30;

	public boolean isIdle() {
		return (currentJob == IDLE);
	}

	public void teleOp() {

		currentJob = TELEOP;

		x = smooth(Robot.driver.getY(GenericHID.Hand.kLeft));
		y = smooth((Robot.driver.getX(GenericHID.Hand.kLeft) * -1));
		t = smooth((Robot.driver.getX(GenericHID.Hand.kRight) * -1));
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

	double desiredHeading = 0;
	double desiredDistance = 0;
	double currentTime = 0;
	double desiredSpeed = 0;
	double degChange = 0;
	double currentHeading = 0;
	double endTime = 0;

	void displayAD() {
		SmartDashboard.putNumber("Degree Change", degChange);
		SmartDashboard.putNumber("Current Heading", currentHeading);
		SmartDashboard.putNumber("Desired Heading", desiredHeading);
		SmartDashboard.putNumber("Left Encoder Count", encL.get());
		SmartDashboard.putNumber("Left Encoder Distance", encL.getDistance());
		SmartDashboard.putNumber("Right Encoder Count", encR.get());
		SmartDashboard.putNumber("Right Encoder Distance", encR.getDistance());
	}

	public void checkStatus() {
		switch (currentJob) {
		case IDLE:
			break;
		case TELEOP:
			break;
		case AUTODRIVE:
			this.checkStatusAD();
			displayAD();
			break;
		case AUTOTURN:
			this.checkStatusAT();
			break;
		}
	}

	void checkStatusAT() {
		double twist = 0.0;
		currentHeading = gyro.getAngle();
		degChange = (desiredHeading - currentHeading);
		if (Math.abs(degChange) < 1) {
			currentJob = IDLE;
			marioDrive.stopMotor();
		}
		currentTime = Timer.getFPGATimestamp();
		if (currentTime > endTime) {
			currentJob = IDLE;
			marioDrive.stopMotor();
		}
		if (degChange < 0.0)
			twist = 0.3;
		else
			twist = -0.3;

		marioDrive.driveCartesian(0.0, 0.0, twist);

	}

	void checkStatusAD() {
		if (Math.min(Math.abs(encL.getDistance()), Math.abs(encR.getDistance())) > Math.abs(desiredDistance)) {
			currentJob = IDLE;
			marioDrive.stopMotor();
		}
		currentTime = Timer.getFPGATimestamp();
		if (currentTime > endTime) {
			currentJob = IDLE;
			marioDrive.stopMotor();
		}
		currentHeading = gyro.getAngle();
		degChange = ((desiredHeading - currentHeading) * -1) / 50;
		marioDrive.driveCartesian(0.0, desiredSpeed, degChange);
		SmartDashboard.putNumber("Desired Speed", desiredSpeed);
		SmartDashboard.putNumber("Degree Change", degChange);

		currentTime = Timer.getFPGATimestamp();
		displayAD();
	}

	public void autoDrive(double speed, double time, double dist) {

		currentJob = AUTODRIVE;

		speed *= -1;

		boolean forward = true;

		endTime = Timer.getFPGATimestamp() + time;

		encL.reset();
		encR.reset();

		if (dist < 0)
			forward = false;
		if (!forward)
			speed *= -1;

		desiredSpeed = speed;
		desiredDistance = dist;
	}

	public void autoTurn(double turn, double time) {

		currentJob = AUTOTURN;

		endTime = Timer.getFPGATimestamp() + time;
		desiredHeading = turn;
	}

}
