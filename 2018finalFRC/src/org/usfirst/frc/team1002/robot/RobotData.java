package org.usfirst.frc.team1002.robot;

import edu.wpi.first.wpilibj.SpeedController;
import edu.wpi.first.wpilibj.VictorSP;

public class RobotData {
	
	/* 
	 * 
	 * 
	 * This is where all the constants are.
	 * 
	 * 
	 * 
	 */
	
	/*
	 * This is where controller mappings are. 
	 */
	
	//Controller ports - driver controls drivebase, operator controls elevator, arm, and grabber.
	public static final int driverPort = 0;
	public static final int operatorPort = 1;
	
	/* 
	 * Driver button mappings:
	 * Stick Left X: drive left/right
	 * Stick Left Y: drive forward/reverse
	 * Stick Right X: turn left/right
	 * Stick Right Y: none
	 * Button A: none
	 * Button B: none
	 * Button X: none
	 * Button Y: none
	 * Button LB: none
	 * Button RB: none
	 * Button Start: none
	 * Button Select: none
	 * Button LS: none
	 * Button RS: none
	 */
	
	/* 
	 * Operator button mappings:
	 * Stick Left X: none
	 * Stick Left Y: none
	 * Stick Right X: none
	 * Stick Right Y: none
	 * Button A: none
	 * Button B: Set elevator height high
	 * Button X: Set elevator height minimum
	 * Button Y: Set elevator height low
	 * Button LB: none
	 * Button RB: none
	 * Button Start: none
	 * Button Select: none
	 * Button LS: none
	 * Button RS: none
	 */
	
	
	/*
	 * Constants used by the MarioDrive class.
	 */
	
	//Drivebase motors and their ports.
	public static final SpeedController driveFrontRight = new VictorSP(7);
	public static final SpeedController driveBackRight = new VictorSP(9);
	public static final SpeedController driveFrontLeft = new VictorSP(8);
	public static final SpeedController driveBackLeft = new VictorSP(6);
	
	//Drive motor encoder port values. 
	public static final int driveEnc1PortA = 2;
	public static final int driveEnc1PortB = 3;
	public static final int driveEnc2PortA = 0;
	public static final int driveEnc2PortB = 1;
	
	//Distance traveled by wheel per encoder click, in feet. 
	public static final double driveDistancePerPulse = (1.0 / 90.0);
	
	
	/*
	 * Constants used by the EightBitElevator class.
	 */
	
	//Port for stage 1 & 2 Talons on elevator. 
	public static final int elevS1TalonPort = 3;
	public static final int elevS2TalonPort = 6;
	
	//Port for elevator Talon PID. 
	public static final int elevSlotIdx = 0;
	
	//Index of PID loop used by the elevator.
	public static final int elevPIDLoopIdx = 0;
	
	//Amount of time in milliseconds that the Talon waits before getting confirmation that it has acted. 
	public static final int elevTimeoutMs = 10;
	
	//Number of encoder clicks per inch the elevator travels. 
	public static final double elevClicksPerUnit = 895.99; 
	
	//Height target set when operator presses respective button, in inches above drivebase.
	public static final double elevHeightX = 0;
	public static final double elevHeightY = 15;
	public static final double elevHeightB = 30;
	
	//Max speed and acceleration of elevator Talon. 
	public static final double elevCruiseVel = 1000.0;
	public static final double elevCruiseAccel = 100.0;
	
	
	/* 
	 * 
	 * 
	 * This is where all the variables are.
	 * 
	 * 
	 */
	
	/*
	 * Variables used by EightBitElevator.
	 */
	
	//Target height for elevator, in inches above drivebase.
	public static double elevDesiredPosition = 0.0;
	
}
