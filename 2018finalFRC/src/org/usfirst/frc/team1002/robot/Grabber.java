package org.usfirst.frc.team1002.robot;

import edu.wpi.first.wpilibj.SpeedController;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.Victor;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

public class Grabber {

	SpeedController grabberMotor;
	double maxAmperage = 0;
	public Grabber() {
		grabberMotor = new Victor(5);
	}
static final double GRABBERPOWER = 0.3;
	public void moveGrabber(int angle) {
		SmartDashboard.putNumber("Grabber Amperage", Robot.pdp.getCurrent(0));
		switch (angle) {
		case 1:
			RobotData.grabIdle = false;
			maxAmperage = 0;
			maxAmperage = Math.max(maxAmperage,Robot.pdp.getCurrent(0));
			SmartDashboard.putNumber("Max Amperage", maxAmperage);
			grabberMotor.set(GRABBERPOWER);
			if(maxAmperage <= 5.0) {
			grabberMotor.set(0.0);
			RobotData.grabIdle = true;
			}
			Timer.delay(0.5);
			grabberMotor.set(0.05);
			RobotData.grabIdle = true;
		case -1:
			RobotData.grabIdle = false;
			grabberMotor.set(-GRABBERPOWER);
			Timer.delay(0.5);
			grabberMotor.set(0);
			RobotData.grabIdle = true;
		default:
			RobotData.grabIdle = false;
			grabberMotor.set(0);
			RobotData.grabIdle = true;
		}

	}
}
