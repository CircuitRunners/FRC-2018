package org.usfirst.frc.team1002.robot;

import edu.wpi.first.wpilibj.SpeedController;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.Victor;

public class Grabber {

	SpeedController grabberMotor;

	public Grabber() {
		grabberMotor = new Victor(0);
	}

	public void moveGrabber(int angle) {
	
		switch (angle) {
		case 1:
			RobotData.grabIdle = false;
			grabberMotor.set(0.85);
			Timer.delay(0.5);
			grabberMotor.set(0.15);
			RobotData.grabIdle = true;
		case -1:
			RobotData.grabIdle = false;
			grabberMotor.set(-0.85);
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
