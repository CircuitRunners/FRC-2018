package org.usfirst.frc.team1002.robot;

import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.VictorSP;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

public class Grabber {

	int loop;
	double currentTime;
	double endTime;
	double direction;
	final double OPENING = 0;
	final double CLOSING = 1;

	VictorSP grabberMotorLeft;
	VictorSP grabberMotorRight;
	public double maxAmperage = 0;

	public Grabber() {
		grabberMotorLeft = new VictorSP(2);
		grabberMotorRight = new VictorSP(5);
		grabberMotorLeft.setInverted(true);
	}

	static final double GRABBERPOWER = 0.8;

	public void display() {
		maxAmperage = Math.max(maxAmperage, Robot.pdp.getCurrent(0));
		SmartDashboard.putNumber("Grabber Amperage", Robot.pdp.getCurrent(0));
	}

	public void checkStatus() {

		currentTime = Timer.getFPGATimestamp();

		if (direction == OPENING) {
			if (currentTime < endTime) {// Later if limit switch added, edit this if statement to check.
				grabberMotorLeft.set(grabPower);
			} else {
				grabberMotorLeft.stopMotor();
				RobotData.grabIdle = true;
			}
		} else if (direction == CLOSING) {
			if (currentTime < endTime) {// Later if limit switch added, edit this if statement to check.
				grabberMotorLeft.set(-grabPower);
			} else {
				grabberMotorLeft.stopMotor();
				RobotData.grabIdle = true;
			}
		}
	}

	double grabPower = 0.0;

	public void moveGrabber(int angle, double power) {
		grabPower = power;
		switch (angle) {
		case 1:
			RobotData.grabIdle = false;
			endTime = Timer.getFPGATimestamp() + 0.025;
			direction = OPENING;

			break;

		case -1:
			RobotData.grabIdle = false;
			endTime = Timer.getFPGATimestamp() + 0.025;
			direction = CLOSING;

			break;
		default:
			RobotData.grabIdle = false;
			grabberMotorLeft.set(0);
			RobotData.grabIdle = true;
			break;
		}

	}

	public void autoRelease() {
		RobotData.grabIdle = false;
		endTime = Timer.getFPGATimestamp() + 0.4;
		direction = OPENING;
	}

	public boolean isIdle() {
		return RobotData.grabIdle;
	}

	double l = 0;
	double r = 0;

	int INTAKE = 99;
	int EJECT = 100;
	int objective = 0;

	void autoCheckStatus() {
		currentTime = Timer.getFPGATimestamp();
		if (objective == INTAKE) {
			if (currentTime > endTime) {
				grabberMotorLeft.stopMotor();
				grabberMotorRight.stopMotor();
				intakeSpeed = 0.0;
				RobotData.grabIdle = true;
			} else {
				grabberMotorLeft.set(intakeSpeed);
				grabberMotorRight.set(intakeSpeed);
			}
		}
		if (objective == EJECT) {
			if (currentTime > endTime) {
				grabberMotorLeft.stopMotor();
				grabberMotorRight.stopMotor();
				ejectSpeed = 0.0;
				RobotData.grabIdle = true;
			} else {
				grabberMotorLeft.set(ejectSpeed);
				grabberMotorRight.set(ejectSpeed);
			}
		}

	}
	double ejectSpeed = 0.0;
	void eject(double time, double speed) {
		RobotData.grabIdle = false;
		objective = EJECT;
		endTime = Timer.getFPGATimestamp() + time;
		ejectSpeed = -speed;
	}
	double intakeSpeed = 0.0;
	void intake(double time, double speed) {
		RobotData.grabIdle = false;
		objective = INTAKE;
		endTime = Timer.getFPGATimestamp() + time;
		intakeSpeed = speed;
	}
}
