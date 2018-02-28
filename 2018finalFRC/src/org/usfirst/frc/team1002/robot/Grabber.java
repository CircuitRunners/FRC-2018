package org.usfirst.frc.team1002.robot;

import edu.wpi.first.wpilibj.SpeedController;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.Victor;
import edu.wpi.first.wpilibj.VictorSP;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

public class Grabber {

	VictorSP grabberMotor;
	public double maxAmperage = 0;

	public Grabber() {
		grabberMotor = new VictorSP(5);
	}

	static final double GRABBERPOWER = 0.3;
public void display() {
	maxAmperage = Math.max(maxAmperage, Robot.pdp.getCurrent(0));
	SmartDashboard.putNumber("Grabber Amperage", Robot.pdp.getCurrent(0));
}
int loop;
	public void moveGrabber(int angle) {

		switch (angle) {
		case 1:
			RobotData.grabIdle = false;
			grabberMotor.set(GRABBERPOWER);
			while (loop++ < 100) {
			maxAmperage = Math.max(maxAmperage, Robot.pdp.getCurrent(0));

			SmartDashboard.putNumber("Max Amperage", maxAmperage);

			Timer.delay(0.01);
			}
		//	if (maxAmperage <= 5.0) {
		//		grabberMotor.set(0.0);
		//		RobotData.grabIdle = true;
		//	}
			
			Timer.delay(0.5);
			grabberMotor.set(0.0);
			RobotData.grabIdle = true;
			break;

		case -1:
			RobotData.grabIdle = false;
			grabberMotor.set(-GRABBERPOWER);
			Timer.delay(0.5);
			grabberMotor.set(0);
			RobotData.grabIdle = true;
			break;
		default:
			RobotData.grabIdle = false;
			grabberMotor.set(0);
			RobotData.grabIdle = true;
			break;
		}

	}
}
