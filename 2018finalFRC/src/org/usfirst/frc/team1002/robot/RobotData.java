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
	
	public static final int driveEnc1PortA = 2;
	public static final int driveEnc1PortB = 3;
	public static final int driveEnc2PortA = 0;
	public static final int driveEnc2PortB = 1;
	
	public static final double driveDistancePerPulse = (1.0 / 90.0);
	
	//Constants used by the Elevator class. 
	public static final int elevTalonPort = 3;
	public static final int elevSlotIdx = 0;
	public static final int elevPIDLoopIdx = 0;
	public static final int elevTimeoutMs = 10;
	public static final double elevClicksPerUnit = 895.99; 
	public static final double elevHeightX = 0;
	public static final double elevHeightY = 15;
	public static final double elevHeightB = 30;
	public static final double elevCruiseVel = 1000.0;
	public static final double elevCruiseAccel = 100.0;
	public static double elevDesiredPosition = 0.0;
	/*
	 * This is where all the data are. 
	 */
	
	
}
