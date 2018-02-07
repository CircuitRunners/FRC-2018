package org.usfirst.frc.team1002.robot;

public class MyAutonomous {
	private double timeForward;
	private double turnDegrees;
	MarioDrive mDrive;

	public MyAutonomous() {
		timeForward = 0;
		mDrive = new MarioDrive();
		
		
	}

	public void init() {

	}
}