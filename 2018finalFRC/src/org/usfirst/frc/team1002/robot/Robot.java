/*----------------------------------------------------------------------------*/
/* Copyright (c) 2017-2018 FIRST. All Rights Reserved.                        */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package org.usfirst.frc.team1002.robot;

import edu.wpi.first.wpilibj.ADXRS450_Gyro;
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

	static final int LEFT = 1;
	static final int CENTER = 2;
	static final int RIGHT = 3;
	static final int SWITCH = 10;
	static final int SCALE = 11;
	static final int NONE = 12;

	public static final String posLeft = "L";
	public static final String posCenter = "C";
	public static final String posRight = "R";

	public static final String targSwitch = "Switch";
	public static final String targScale = "Scale";
	public static final String targLine = "None";

	public static final String altFalse = "Normal";
	public static final String altTrue = "Alternate";

	public static int posSelected = -1;
	public static int targSelected = -1;
	public static boolean wasDisabled = false;
	// public static String altSelected;
	static SendableChooser<Integer> chooserPos = new SendableChooser<>(); // Choose the starting position of the robot,
																			// with respect to the driver wall.
	SendableChooser<Integer> chooserTarg = new SendableChooser<>(); // Choose the target of the robot: switch,
																	// scale, or nothing.
	// SendableChooser<String> chooserAlt = new SendableChooser<>(); // Choose if
	// the robot should use an alternate
	// path. Intended to be used if we think
	// another robot will obstruct ours.

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
		chooserTarg.addDefault("Switch", SWITCH);
		chooserTarg.addObject("Scale", SCALE);
		chooserTarg.addObject("Cross Line", NONE);
		// chooserAlt.addDefault("Normal Mode", altFalse);
		// chooserAlt.addObject("Alternate Mode", altTrue);
		SmartDashboard.putData("Starting Position", chooserPos);
		SmartDashboard.putData("Target", chooserTarg);
		// SmartDashboard.putData("Alternate Mode?", chooserAlt);

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
		// RobotData.elevPositionTarget = elev.getElevatorPositionUnits();
		posSelected = chooserPos.getSelected();
		targSelected = chooserTarg.getSelected();
		// altSelected = chooserAlt.getSelected();
		SmartDashboard.putNumber("Pos Selected", posSelected);
		SmartDashboard.putNumber("Target Selected", targSelected);
		// autoSelected = SmartDashboard.getString("Auto Selector",
		// defaultAuto);
		System.out.println("Starting Position: " + posSelected);
		System.out.println("Target Selected: " + targSelected);
		// System.out.println("Autonomous Mode: " + altSelected);
		auto.init();

		// elev.setElevatorPositionUnits(elev.getElevatorPositionUnits());
	}

	/**
	 * This function is called periodically during autonomous.
	 */
	boolean hasFMS = false;

	@Override
	public void autonomousPeriodic() {
		while (!hasFMS) {
			hasFMS = auto.getAutoRoutine();
			Timer.delay(0.01);
		}
		drive.checkStatus();
		arm.checkStatus();
		elev.checkStatus();
		grab.checkStatus();

		auto.run();

	}

	@Override
	public void teleopInit() {
		RobotData.armPositionTarget = arm.getArmPosition();
		RobotData.elevPositionTarget = elev.getElevatorPositionUnits();
		// RobotData.elevPositionTarget = elev.getElevatorPositionUnits();
	}

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
			RobotData.elevPositionTarget = elev.moveTo(RobotData.elevPositionTarget, 70);
		if (lastArmPos != RobotData.armPositionTarget)
			RobotData.armPositionTarget = arm.moveTo(RobotData.armPositionTarget, 70);

		drive.teleOp();

		grab.checkStatus();
		arm.checkStatus();
		elev.checkStatus();

	}

	/**
	 * This function is called periodically during test mode.
	 */
	@Override
	public void testPeriodic() {
		elev.display();
		if (driver.getXButton()) {

			drive.autoDrive(0.25, 15.0, 10.0);

		}
		if (driver.getAButton()) {
			auto.sameSideScale();
		}
		drive.checkStatus();
		elev.checkStatus();
		arm.checkStatus();
		grab.checkStatus();

	}

	public void disabledPeriodic() {
		if(!resetGyro.get()) {

			Timer.delay(0.5);
			drive.gyro = new ADXRS450_Gyro();
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
	static double elevIncrement = ((elev.maxElevCV * elev.speedFactor/100.0) / RobotData.elevClicksPerUnitS1) / 50;
	static double armIncrement = ((arm.maxArmCV * arm.speedFactor/100.0) / RobotData.armClicksPerUnit) / 50;

	public void getControllers() {
		/*
		 * The else if's are to ensure only one operation is commanded for each pass of
		 * the loop
		 */

		RobotData.elevPositionTarget -= smoothIncrement(operator.getY(GenericHID.Hand.kRight), 0.2, elevIncrement);
		RobotData.armPositionTarget -= smoothIncrement(operator.getY(GenericHID.Hand.kLeft), 0.2, armIncrement);

		SmartDashboard.putNumber("ARM TARGET", RobotData.armPositionTarget);
		SmartDashboard.putNumber("ELEV TARGET", RobotData.elevPositionTarget); // else if (lastTimeArmIncrement) {

		/*
		 * Grabber Operation Code ++++++++++++++++++++++++++++++++++++++++++
		 */
		SmartDashboard.putString("GrabberStat", "--");
		if (operator.getBumper(GenericHID.Hand.kLeft)) {
			grab.moveGrabber(1);
			SmartDashboard.putString("GrabberStat", "1");
		} else if (operator.getBumper(GenericHID.Hand.kRight)) {
			grab.moveGrabber(-1);
			SmartDashboard.putString("GrabberStat", "-1");
		} else if (driver.getBumper(GenericHID.Hand.kLeft)) {
			grab.moveGrabber(1);
			SmartDashboard.putString("GrabberStat", "1");
		} else if (driver.getBumper(GenericHID.Hand.kRight)) {
			grab.moveGrabber(-1);
			SmartDashboard.putString("GrabberStat", "-1");
		}
		if (operator.getTriggerAxis(GenericHID.Hand.kRight) > 0.5) {
			elev.enableLimitless();
			arm.enableLimitless();
		}
		drive.opScale = 1;
		if (driver.getTriggerAxis(GenericHID.Hand.kRight) > 0.5)
			drive.opScale /= 2;
	}
}