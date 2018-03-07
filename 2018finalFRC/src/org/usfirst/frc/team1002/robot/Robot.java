/*----------------------------------------------------------------------------*/
/* Copyright (c) 2017-2018 FIRST. All Rights Reserved.                        */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package org.usfirst.frc.team1002.robot;

import edu.wpi.first.wpilibj.GenericHID;
import edu.wpi.first.wpilibj.IterativeRobot;
import edu.wpi.first.wpilibj.PowerDistributionPanel;
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

	public static final String posLeft = "L";
	public static final String posCenter = "C";
	public static final String posRight = "R";
	
	public static final String targSwitch = "Switch";
	public static final String targScale = "Scale";
	public static final String targLine = "None";
	
	public static final String altFalse = "Normal";
	public static final String altTrue = "Alternate";
	
	public static String posSelected;
	public static String targSelected;
	public static String altSelected;
	static SendableChooser<String> chooserPos; // Choose the starting position of the robot,
																			// with respect to the driver wall.
	SendableChooser<String> chooserTarg = new SendableChooser<>(); // Choose the target of the robot: switch,
																			// scale, or nothing.
	SendableChooser<String> chooserAlt = new SendableChooser<>(); // Choose if the robot should use an alternate
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

	/**
	 * This function is run when the robot is first started up and should be used
	 * for any initialization code.
	 */
	@Override
	public void robotInit() {
		
		chooserPos = new SendableChooser<>();
		
		chooserPos.addDefault("Left", posLeft);
		chooserPos.addObject("Center", posCenter);
		chooserPos.addObject("Right", posRight);
		chooserTarg.addDefault("Switch", targSwitch);
		chooserTarg.addObject("Scale", targScale);
		chooserTarg.addObject("Cross Line", targLine);
		chooserAlt.addDefault("Normal Mode", altFalse);
		chooserAlt.addObject("Alternate Mode", altTrue);
		SmartDashboard.putData("Starting Position", chooserPos);
		SmartDashboard.putData("Target", chooserTarg);
		SmartDashboard.putData("Alternate Mode?", chooserAlt);
		
		
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
		RobotData.armPositionTarget = arm.getArmPosition();
		// RobotData.elevPositionTarget = elev.getElevatorPositionUnits();
		posSelected = chooserPos.getSelected();
		targSelected = chooserTarg.getSelected();
		altSelected = chooserAlt.getSelected();
		// autoSelected = SmartDashboard.getString("Auto Selector",
		// defaultAuto);
		System.out.println("Starting Position: " + posSelected);
		System.out.println("Target Selected: " + targSelected);
		System.out.println("Autonomous Mode: " + altSelected);
		auto.init();
		// elev.setElevatorPositionUnits(elev.getElevatorPositionUnits());
	}

	/**
	 * This function is called periodically during autonomous.
	 */
	@Override
	public void autonomousPeriodic() {
		drive.checkStatus();
		arm.checkStatus();
		elev.checkStatus();
		grab.checkStatus();
		// Autonomous.run();
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
		double lastElevPos = RobotData.elevPositionTarget;
		double lastArmPos = RobotData.armPositionTarget;
		getControllers();

		if (lastElevPos != RobotData.elevPositionTarget)
			elev.moveTo(RobotData.elevPositionTarget);
		if (lastArmPos != RobotData.armPositionTarget)
			arm.moveTo(RobotData.armPositionTarget);

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
		if(driver.getAButton()) {
			auto.ScaleAutoV2();
		}
		drive.checkStatus();
		elev.checkStatus();
		arm.checkStatus();
		grab.checkStatus();

	}

	public void disabledPeriodic() {
		SmartDashboard.putNumber("Left Encoder Count", drive.encL.get());
		SmartDashboard.putNumber("Right Encoder Count", drive.encR.get());
		SmartDashboard.putNumber("Left Encoder Distance", drive.encL.getDistance());
		SmartDashboard.putNumber("Right Encoder Distance", drive.encR.getDistance());

		// elev.display();
	}

	double smoothIncrement(double value, double deadBand, double max) {
		double aValue = Math.abs(value);
		if (aValue < deadBand)
			return 0.0;
		return max * value;
	}

	boolean lastTimeElevIncrement = false;
	boolean lastTimeArmIncrement = false;
	static double elevIncrement = 0.2;
	static double armIncrement = 1;

	public void getControllers() {
		/*
		 * The else if's are to ensure only one operation is commanded for each pass of
		 * the loop
		 */
		/*
		 * Elevator Operation Code +++++++++++++++++++++++++++++++++
		 */
		/*
		 * if (operator.getXButton()) { //High elevator height TBC
		 * //RobotData.elevPositionTarget = RobotData.elevHeightX; } else if
		 * (operator.getYButton()) { //Mid elevator height TBC
		 * //RobotData.elevPositionTarget = RobotData.elevHeightY; } else if
		 * (operator.getBButton()) { //Low Elevator height TBC
		 * //RobotData.elevPositionTarget = RobotData.elevHeightB; } else if
		 * (operator.getAButton()) { //Puts the block into a stored position TBC
		 * //RobotData.elevPositionTarget = 20; //RobotData.armPositionClicks = 10; }
		 */ // else if (operator.getPOV(0) != -1) {
			// lastTimeElevIncrement = true;
			// if (operator.getPOV(0) > 270 || operator.getPOV(0) < 90) {
			// RobotData.elevPositionTarget += 0.2;
			// } else {
			// RobotData.elevPositionTarget -= 0.2;
			// }
			// } else if (lastTimeElevIncrement) {
		/*
		 * This says, if the POV was used last time, when the button is released, stop
		 * at that point
		 */
		// lastTimeElevIncrement = false;
		// SmartDashboard.putNumber("Elevator getPosition",
		// elev.getElevatorPositionUnits());
		// RobotData.elevPositionTarget = elev.moveElevatorToCurrentPosition();
		// }

		RobotData.elevPositionTarget -= smoothIncrement(operator.getY(GenericHID.Hand.kRight), 0.2, elevIncrement);
		RobotData.armPositionTarget -= smoothIncrement(operator.getY(GenericHID.Hand.kLeft), 0.2, armIncrement);

		// if (operator.getY(GenericHID.Hand.kLeft) <= -0.4) {
		// Arm Up
		// lastTimeArmIncrement = true;
		// RobotData.armPositionTarget += armIncrement;
		// } else if (operator.getY(GenericHID.Hand.kLeft) >= 0.4) {
		// Arm Down
		// lastTimeArmIncrement = true;
		// RobotData.armPositionTarget -= armIncrement;
		// }
		SmartDashboard.putNumber("ARM TARGET", RobotData.armPositionTarget);
		SmartDashboard.putNumber("ELEV TARGET", RobotData.elevPositionTarget); // else if (lastTimeArmIncrement) {
		/*
		 * This says, if the POV was used last time, when the button is released, stop
		 * at that point
		 */
		// lastTimeArmIncrement = false;

		// }
		/*
		 * Grabber Operation Code ++++++++++++++++++++++++++++++++++++++++++
		 */
		SmartDashboard.putString("GrabberStat", "--");
		if (operator.getBumper(GenericHID.Hand.kLeft) && driver.getBumper(GenericHID.Hand.kRight)) {
			// Operator overrides driver
			grab.moveGrabber(1);
			SmartDashboard.putString("GrabberStat", "1");
		} else if (operator.getBumper(GenericHID.Hand.kRight) && driver.getBumper(GenericHID.Hand.kLeft)) {
			//operator overrides driver, again
			grab.moveGrabber(-1);
			SmartDashboard.putString("GrabberStat", "-1");
		} else if (operator.getBumper(GenericHID.Hand.kLeft) || driver.getBumper(GenericHID.Hand.kLeft)) {
			grab.moveGrabber(1);
			SmartDashboard.putString("GrabberStat", "1");
		} else if (operator.getBumper(GenericHID.Hand.kRight) || driver.getBumper(GenericHID.Hand.kRight)) {
			grab.moveGrabber(-1);
			SmartDashboard.putString("GrabberStat", "-1");
		}
		if(operator.getTriggerAxis(GenericHID.Hand.kRight) >0.5) {
			elev.enableLimitless();
			arm.enableLimitless();
		}
	}
}