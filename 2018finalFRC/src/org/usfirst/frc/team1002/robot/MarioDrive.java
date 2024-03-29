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

	private static double smooth(double value, double deadBand, double max) {
		double aValue = Math.abs(value);
		if (aValue > max)
			return (value / aValue);
		else if (aValue < deadBand)
			return 0;
		else
			return aValue * aValue * aValue * (value / aValue);
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

	double opScale = 1;

	private double safety(double cmdVal, double prevVal, double maxChange) {
		double diff = cmdVal - prevVal;
		if (Math.abs(diff) < maxChange) {
			return cmdVal;
		} else {
			if (diff > 0) {
				return prevVal + maxChange;
			} else {
				return prevVal - maxChange;
			}
		}
	}

	public void teleOp() {

		currentJob = TELEOP;

		x = smooth(Robot.driver.getY(GenericHID.Hand.kLeft), 0.22, 0.95) * opScale;
		y = smooth((Robot.driver.getX(GenericHID.Hand.kLeft) * -1), 0.22, 0.95);
		t = smooth((Robot.driver.getX(GenericHID.Hand.kRight) * -1), 0.22, 0.95) * opScale;

		x = safety(x, prev_x, maxSpeedDiff);
		y = safety(y, prev_y, maxSpeedDiff);
		t = safety(t, prev_t, maxSpeedDiff);

		marioDrive.driveCartesian(y, x, t);

		prev_x = x;
		prev_y = y;
		prev_t = t;

		SmartDashboard.putNumber("teleCount", count++);
		SmartDashboard.putNumber("Controller Left/Right", x);
		SmartDashboard.putNumber("Controller Forward/Reverse", -y);
		SmartDashboard.putNumber("Controller Twist", t);
		SmartDashboard.putNumber("Gyro Count Degrees", gyro.getAngle());
		SmartDashboard.putNumber("Right Encoder Count",encR.get());
		SmartDashboard.putNumber("Left Encoder Count", encL.get());
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
			SmartDashboard.putString("Drive State", "IDLE");
			break;
		case TELEOP:
			double speed = encL.getRate() * (5280 / 3600);
			SmartDashboard.putNumber("Speed MPH", speed);
			SmartDashboard.putString("Drive State", "TELEOP");
			break;
		case AUTODRIVE:
			revisedCheckStatusAD();// Testing new auto drive algorithm
			displayAD();
			SmartDashboard.putString("Drive State", "AUTODRIVE");
			break;
		case AUTOTURN:
			checkStatusAT();
			SmartDashboard.putString("Drive State", "AUTOTURN");
			break;
		}
	}

	double TURNSPEED = 0.5;
	double closeTurn = TURNSPEED / 2;
	double correctionCounter = 0;
	void checkStatusAT() {
		double twist = 0.0;
		currentHeading = gyro.getAngle();
		SmartDashboard.putNumber("Gyro Count Degrees", currentHeading);
		degChange = (desiredHeading - currentHeading);
		currentTime = Timer.getFPGATimestamp();
		if (currentTime > endTime) {
			currentJob = IDLE;
			marioDrive.stopMotor();
			System.out.println("AutoTurn timed out. Desired Heading: " + desiredHeading);
		} else if (Math.abs(degChange) < 1) {
			correctionCounter++;
			TURNSPEED = closeTurn;
		} else {
			correctionCounter = 0;
		}
		if(correctionCounter >= 10) {
			currentJob = IDLE;
			marioDrive.stopMotor();
		}else {
			if (degChange < -1)
				twist = TURNSPEED;
			if (degChange > 1)
				twist = -TURNSPEED;
		}

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
			System.out.println("AutoDrive timed out. desired Distance: " + desiredDistance);
		}
		currentHeading = gyro.getAngle();
		degChange = ((desiredHeading - currentHeading) * -1) / 50;
		marioDrive.driveCartesian(0.0, desiredSpeed, degChange);
		SmartDashboard.putNumber("Desired Speed", desiredSpeed);
		SmartDashboard.putNumber("Degree Change", degChange);

		currentTime = Timer.getFPGATimestamp();
		displayAD();
	}

	boolean forward = true;
	double absSpeed = 0;
	double absDistance = 0;

	public void autoDrive(double dist, double speed, double time) {

		currentJob = AUTODRIVE;

		forward = true;

		if (dist < 0)
			forward = false;
		endTime = Timer.getFPGATimestamp() + time;

		encL.reset();
		encR.reset();
		rampSpeed = 0.2;
		desiredSpeed = speed;
		absSpeed = Math.abs(speed);
		desiredDistance = dist;
		absDistance = Math.abs(dist);
		desiredHeading = gyro.getAngle();
	}

	public void autoTurn(double turn, double speed, double time) {

		currentJob = AUTOTURN;
		TURNSPEED = speed;
		endTime = Timer.getFPGATimestamp() + time;
		desiredHeading = turn;
	}

	double DZone(double input) {
		if (input > 0.9)
			return 1;
		else if (input < 0.22 && input > -0.22)
			return 0;
		else if (input < -0.9) {
			return -1;
		} else
			return input;
	}

	double rampSpeed = 0;
	double rampInc = 0.02;
	double nearby = 2.75;

	void revisedCheckStatusAD() {
		double absDistTravelled = Math.abs(Math.min(encL.getDistance(), encR.getDistance()));
		if (absDistTravelled > Math.abs(desiredDistance)) {
			currentJob = IDLE;
			marioDrive.stopMotor();
			System.out.println("AutoDrive:"+desiredDistance+" ft. sucessful.");
		}

		currentTime = Timer.getFPGATimestamp();

		if (currentTime > endTime) {
			currentJob = IDLE;
			marioDrive.stopMotor();
			System.out.println("AutoDrive timed out. desired Distance: " + desiredDistance);
		}

		currentHeading = gyro.getAngle();

		degChange = ((desiredHeading - currentHeading) * -1) / 50;
		if (desiredDistance < nearby) {
			rampSpeed = desiredSpeed;//if the total distance is less than the nearby distance no ramp occurs.
		} else {
			if (absDistTravelled < nearby) {
				rampSpeed += rampInc;
				rampSpeed = Math.min(rampSpeed, absSpeed);
			} else if (absDistTravelled >= nearby && absDistTravelled < absDistance - nearby) {
				rampSpeed = absSpeed;
			} else {
				rampSpeed -= rampInc;
				rampSpeed = Math.max(0.2, rampSpeed);
			}
		}

		marioDrive.driveCartesian(0.0, forward? -rampSpeed : rampSpeed, degChange);

		SmartDashboard.putNumber("Desired Speed", desiredSpeed);
		SmartDashboard.putNumber("Ramp Speed", rampSpeed);
		SmartDashboard.putNumber("Degree Change", degChange);

		currentTime = Timer.getFPGATimestamp();
		//displayAD();
	}
}
