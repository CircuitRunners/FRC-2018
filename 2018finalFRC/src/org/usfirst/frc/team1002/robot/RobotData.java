package org.usfirst.frc.team1002.robot;

import edu.wpi.first.wpilibj.SpeedController;
import edu.wpi.first.wpilibj.VictorSP;

public class RobotData {
	
	/*
	 * This is where all the constants are. 
	 */
	public static final SpeedController kFrontRight = new VictorSP(7);
	public static final SpeedController kBackRight = new VictorSP(9);  //Motor Controllers for drivebase
	public static final SpeedController kFrontLeft = new VictorSP(8);
	public static final SpeedController kBackLeft = new VictorSP(6);
	
	public static final int driverPort = 0;
	public static final int operatorPort = 1;
	
	public static final int enc1PortA = 2;
	public static final int enc1PortB = 3;
	public static final int enc2PortA = 0;
	public static final int enc2PortB = 1;
	
	public static final double driveDistancePerPulse = (1.0 / 90.0);
	
	/*
	 * This is where all the data are. 
	 */
	
}
