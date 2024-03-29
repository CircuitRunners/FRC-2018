/*----------------------------------------------------------------------------*/
/* Copyright (c) 2017-2018 FIRST. All Rights Reserved.                        */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package org.usfirst.frc.team1002.robot;

import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.GenericHID;
import edu.wpi.first.wpilibj.IterativeRobot;
import edu.wpi.first.wpilibj.PowerDistributionPanel;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.XboxController;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

/**
 * The VM is configured to automatically run this class, and to call the
 * functions corresponding to each mode, as described in the IterativeRobot
 * documentation. If you change the name of this class or the package after
 * creating this project, you must also update the build.properties file in the
 * project.
 */

public class Robot extends IterativeRobot {

	static final int NONE = 0;
	static final int LEFT = 1;
	static final int CENTER = 2;
	static final int RIGHT = 3;
	static final int SWITCH = 4;
	static final int SCALE = 5;
	static final int SWITCHORSCALESWITCHPREF = 6;
	static final int SWITCHORSCALESCALEPREF = 7;
	static final int SWITCHANDSCALE = 8;
	static final int FARSWITCHANDSCALE = 9;
	static final int NEAREST = 10;
	static final int FURTHEST = 11;
	static final int ONEBLOCK = 12;
	static final int TWOBLOCK = 13;
	static final int THREEBLOCK = 14;
	static final int ALLPREFSCALE = 15;
	static final int ALLPREFSWITCH = 16;

	public static int posSelected = -1;
	public static int targSelected = -1;
	public static int prefSelected = -1;
	public static int blockSelected = -1;
	public static boolean wasDisabled = false;
	// public static String altSelected;
	static SendableChooser<Integer> chooserPos = new SendableChooser<>(); // Choose the starting position of the robot,
																			// with respect to the driver wall.
	static SendableChooser<Integer> chooserTarg = new SendableChooser<>();

	static SendableChooser<Integer> chooserPreference = new SendableChooser<>();

	static SendableChooser<Integer> chooserBlock = new SendableChooser<>();

	public static XboxController driver = new XboxController(RobotData.driverPort);
	public static XboxController operator = new XboxController(RobotData.operatorPort);

	static MarioDrive drive = new MarioDrive();
	static EightBitElev elev = new EightBitElev();
	static Grabber grab = new Grabber();
	static RobotArm arm = new RobotArm();
	static PowerDistributionPanel pdp = new PowerDistributionPanel(0);
	static Autonomous auto = new Autonomous();
	static CameraControl cam = new CameraControl();
	static DigitalInput resetGyro = new DigitalInput(9);

	/**
	 * This function is run when the robot is first started up and should be used
	 * for any initialization code.
	 */
	@Override
	public void robotInit() {

		chooserPos.addDefault("Left", LEFT);
		chooserPos.addObject("Center", CENTER);
		chooserPos.addObject("Right", RIGHT);
		SmartDashboard.putData("Robot Position", chooserPos);
		// chooserTarg.addDefault("Switch", SWITCH);
		// chooserTarg.addObject("Scale", SCALE);
		// chooserTarg.addObject("Far Switch and Scale", FARSWITCHANDSCALE);

		chooserTarg.addObject("switch or Scale(switch Pref)", SWITCHORSCALESWITCHPREF);// done
		chooserTarg.addObject("Switch or Scale(Scale Pref)", SWITCHORSCALESCALEPREF);// done
		chooserTarg.addObject("Switch and Scale", SWITCHANDSCALE);// done
		chooserTarg.addDefault("Cross Line", NONE);
		SmartDashboard.putData("Chooser Target", chooserTarg);
		chooserPreference.addObject("Nearest", NEAREST);// done
		chooserPreference.addObject("Furthest", FURTHEST);// done
		chooserPreference.addObject("Switch", SWITCH);// done
		chooserPreference.addDefault("Scale", SCALE);// done
		SmartDashboard.putData("Chooser Preference", chooserPreference);
		chooserBlock.addDefault("One Block", ONEBLOCK);// done
		chooserBlock.addObject("Two Block", TWOBLOCK);// done
		chooserBlock.addObject("Three Block", THREEBLOCK);// done
		SmartDashboard.putData("Chooser Block", chooserBlock);
		
		cam.cameraInit();
		elev.init();
		arm.init();

	}

	/**
	 * This autonomous (along with the chooser code above) shows how to select
	 * between different autonomous modes using the dashboard. The sendable chooser
	 * code works with the Java SmartDashboard. If you prefer the LabVIEW Dashboard,
	 * remove all of the chooser code and uncomment the getString line to get the
	 * auto name from the text box below the Gyro
	 *
	 * <p>
	 * You can add additional auto modes by adding additional comparisons to the
	 * switch structure below with additional strings. If using the SendableChooser
	 * make sure to add them to the chooser code above as well.
	 * </p>
	 */
	@Override
	public void autonomousInit() {
		drive.gyro.reset();
		RobotData.armPositionTarget = arm.getArmPosition();

		posSelected = chooserPos.getSelected();
		targSelected = chooserTarg.getSelected();
		prefSelected = chooserPreference.getSelected();
		blockSelected = chooserBlock.getSelected();

		SmartDashboard.putNumber("Pos Selected", posSelected);
		SmartDashboard.putNumber("Target Selected", targSelected);
		System.out.println("Starting Position: " + posSelected);
		System.out.println("Target Selected: " + targSelected);
		// System.out.println("Autonomous Mode: " + altSelected);
		auto.init();

	}

	boolean hasFMS = false;

	/**
	 * This function is called periodically during autonomous.
	 */
	@Override
	public void autonomousPeriodic() {
		while (!hasFMS) {
			hasFMS = auto.getAutoRoutine();
			Timer.delay(0.01);
		}
		drive.checkStatus();
		arm.checkStatus();
		elev.checkStatus();
		grab.autoCheckStatus();

		auto.run();
		SmartDashboard.putNumber("Auto Step", auto.step);
	}

	// @Override
	// public void teleopInit() {
	// RobotData.armIdle = true;
	// RobotData.elevIdle = true;
	// RobotData.grabIdle = true;
	// drive.currentJob = 0;// IDLE
	// RobotData.armPositionTarget = arm.getArmPosition();
	// RobotData.elevPositionTarget = elev.getElevatorPositionUnits();
	// RobotData.elevPositionTarget = elev.getElevatorPositionUnits();
	// }

	/**
	 * This function is called periodically during operator control.
	 */
	@Override
	public void teleopPeriodic() {
		if (wasDisabled) {
			RobotData.elevPositionTarget = elev.getElevatorPositionUnits();
			RobotData.armPositionTarget = arm.getArmPosition();
			wasDisabled = false;
		}
		double lastElevPos = RobotData.elevPositionTarget;
		double lastArmPos = RobotData.armPositionTarget;
		getControllers();

		if (lastElevPos != RobotData.elevPositionTarget)
			RobotData.elevPositionTarget = elev.moveTo(RobotData.elevPositionTarget, 80, 5);
		if (lastArmPos != RobotData.armPositionTarget)
			RobotData.armPositionTarget = arm.moveTo(RobotData.armPositionTarget, 10, 5);

		drive.teleOp();
		arm.checkStatus();
		elev.checkStatus();

		arm.displayArmStatus();
		elev.displayElevStatus();

	}

	/**
	 * This function is called periodically during test mode.
	 */
	@Override
	public void testPeriodic() {
		elev.display();
		if (driver.getXButton()) {

			drive.autoTurn(90, 0.5, 4);

		}
		if (driver.getAButton()) {
			grab.eject(5, 0.5);
		}
		drive.checkStatus();
		elev.checkStatus();
		arm.checkStatus();
		grab.autoCheckStatus();

	}

	public void disabledPeriodic() {
		if (!resetGyro.get()) {

			Timer.delay(0.5);
			drive.gyro.calibrate();
			drive.gyro.reset();
		}

		SmartDashboard.putNumber("Left Encoder Count", drive.encL.get());
		SmartDashboard.putNumber("Right Encoder Count", drive.encR.get());
		SmartDashboard.putNumber("Left Encoder Distance", drive.encL.getDistance());
		SmartDashboard.putNumber("Right Encoder Distance", drive.encR.getDistance());
		SmartDashboard.putNumber("Gyro Count Degrees", drive.gyro.getAngle());

		// elev.display();
		wasDisabled = true;
	}

	double smoothIncrement(double value, double deadBand, double max) {
		double aValue = Math.abs(value);
		if (aValue < deadBand)
			return 0.0;
		return max * value;
	}

	boolean lastTimeElevIncrement = false;
	boolean lastTimeArmIncrement = false;
	static double elevIncrement = ((elev.maxElevCV * elev.speedFactor / 100.0) / RobotData.elevClicksPerUnitS1) / 50;
	static double armIncrement = ((arm.maxArmCV * arm.speedFactor / 100.0) / RobotData.armClicksPerUnit) / 50;

	public void getControllers() {
		/*
		 * The else if's are to ensure only one operation is commanded for each pass of
		 * the loop
		 */

		RobotData.elevPositionTarget -= smoothIncrement(operator.getY(GenericHID.Hand.kLeft), 0.2, elevIncrement);
		RobotData.armPositionTarget -= smoothIncrement(operator.getY(GenericHID.Hand.kRight), 0.2, armIncrement);

		SmartDashboard.putNumber("ARM TARGET", RobotData.armPositionTarget);
		SmartDashboard.putNumber("ELEV TARGET", RobotData.elevPositionTarget); // else if (lastTimeArmIncrement) {

		drive.opScale = 1;

		if (driver.getBumper(GenericHID.Hand.kLeft)) {// std eject
			grab.grabberMotorLeft.set(-0.45);
			grab.grabberMotorRight.set(-0.45);

		} else if (operator.getBumper(GenericHID.Hand.kLeft)) {
			grab.grabberMotorLeft.set(0.75);
			grab.grabberMotorRight.set(-0.75);

		} else if (operator.getBumper(GenericHID.Hand.kRight)) {
			grab.grabberMotorLeft.set(-0.75);
			grab.grabberMotorRight.set(0.75);
		} else if (driver.getTriggerAxis(GenericHID.Hand.kLeft) > 0.1) {// variable eject
			grab.grabberMotorLeft.set(-driver.getTriggerAxis(GenericHID.Hand.kLeft));
			grab.grabberMotorRight.set(-driver.getTriggerAxis(GenericHID.Hand.kLeft));
		} else if (operator.getTriggerAxis(GenericHID.Hand.kLeft) > 0.1
				|| operator.getTriggerAxis(GenericHID.Hand.kRight) > 0.1) {// variable intake
			grab.grabberMotorLeft.set(operator.getTriggerAxis(GenericHID.Hand.kLeft));
			grab.grabberMotorRight.set(operator.getTriggerAxis(GenericHID.Hand.kRight));
		} else if(!driver.getBumper(GenericHID.Hand.kRight)){
			grab.grabberMotorLeft.set(0.25);
			grab.grabberMotorRight.set(0.25);
		} else { 
			grab.grabberMotorLeft.set(0.0);
			grab.grabberMotorRight.set(0.0);
		}

		if (operator.getYButton()) {
			RobotData.elevPositionTarget = elev.moveTo(RobotData.elevMaxHeightUnits - 2, 100, 5);
			RobotData.armPositionTarget = arm.moveTo(75, 100, 5);
		} else if (operator.getAButton()) {
			RobotData.elevPositionTarget = elev.moveTo(0, 100, 5);
			arm.enableLimitless();
			RobotData.armPositionTarget = arm.moveTo(-30, 100, 5);
		} else if (operator.getXButton()) {
			RobotData.elevPositionTarget = elev.moveTo(15, 100, 5);
			RobotData.armPositionTarget = arm.moveTo(0, 100, 5);
		}
		if (driver.getTriggerAxis(GenericHID.Hand.kRight) > 0.2) {
			drive.opScale /= 2;
		}
		if (operator.getBButton()) {
			elev.enableLimitless();
			arm.enableLimitless();

		}

	}

}