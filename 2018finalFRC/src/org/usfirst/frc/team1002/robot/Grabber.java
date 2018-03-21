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
	
	VictorSP grabberMotor;
	public double maxAmperage = 0;

	public Grabber() {
		grabberMotor = new VictorSP(5);
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
				grabberMotor.set(GRABBERPOWER);
			} else {
				grabberMotor.stopMotor();
				RobotData.grabIdle = true;
			}
		} else if (direction == CLOSING) {
			if (currentTime < endTime) {// Later if limit switch added, edit this if statement to check.
				grabberMotor.set(-GRABBERPOWER);
			} else {
				grabberMotor.stopMotor();
				RobotData.grabIdle = true;
			}
		}
	}


	public void moveGrabber(int angle) {

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
			grabberMotor.set(0);
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
}
