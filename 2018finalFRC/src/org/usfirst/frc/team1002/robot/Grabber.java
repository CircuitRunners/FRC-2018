package org.usfirst.frc.team1002.robot;

import edu.wpi.first.wpilibj.VictorSP;

public class Grabber {
	VictorSP grabberMotor;
	public Grabber() {
	 grabberMotor = new VictorSP(0);
	}

	public void moveGrabber(double spd) {
		grabberMotor.set(spd);
	}
}
