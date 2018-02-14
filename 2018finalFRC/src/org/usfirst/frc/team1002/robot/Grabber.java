package org.usfirst.frc.team1002.robot;

import edu.wpi.first.wpilibj.Servo;

public class Grabber {
	Servo grabberMotor;
	public Grabber() {
	 grabberMotor = new Servo(0);
	}

	public void moveGrabber(double angle) {
		grabberMotor.set(angle);
	}
}
