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
	
	public static final String posLeft = "Left";
	public static final String posCenter = "Center";
	public static final String posRight = "Right";
	public static final String targSwitch = "Switch";
	public static final String targScale = "Scale";
	public static final String targLine = "None";
	public static final String altFalse = "Normal";
	public static final String altTrue = "Alternate";
	public static String posSelected;
	public static String targSelected;
	public static String altSelected;
	private SendableChooser<String> chooserPos = new SendableChooser<>(); // Choose the starting position of the robot,
																			// with respect to the driver wall.
	private SendableChooser<String> chooserTarg = new SendableChooser<>(); // Choose the target of the robot: switch,
																			// scale, or nothing.
	private SendableChooser<String> chooserAlt = new SendableChooser<>(); // Choose if the robot should use an alternate
																			// path. Intended to be used if we think
																			// another robot will obstruct ours.
	
		public static XboxController driver = new XboxController(RobotData.driverPort);
	public static XboxController operator = new XboxController(RobotData.operatorPort);
	
	static MarioDrive drive = new MarioDrive();
	EightBitElev elev = new EightBitElev();
	Grabber grab = new Grabber();
	RobotArm arm = new RobotArm();
	static PowerDistributionPanel pdp = new PowerDistributionPanel(0);

	/**
	 * This function is run when the robot is first started up and should be used
	 * for any initialization code.
	 */
	@Override
	public void robotInit() {
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
		RobotData.desiredArmAngle = arm.getArmPositionUnits();
		// RobotData.elevPositionTarget = elev.getElevatorPositionUnits();
		posSelected = chooserPos.getSelected();
		targSelected = chooserTarg.getSelected();
		altSelected = chooserAlt.getSelected();
		// autoSelected = SmartDashboard.getString("Auto Selector",
		// defaultAuto);
		System.out.println("Starting Position: " + posSelected);
		System.out.println("Target Selected: " + targSelected);
		System.out.println("Autonomous Mode: " + altSelected);
		Autonomous.init();
		// elev.setElevatorPositionUnits(elev.getElevatorPositionUnits());
	}

	/**
	 * This function is called periodically during autonomous.
	 */
	@Override
	public void autonomousPeriodic() {
		drive.checkStatus();
		arm.checkStatus();
		// elev.checkStatus();
		Autonomous.run();
	}

	public void teleopIinit() {
		RobotData.desiredArmAngle = arm.getArmPositionUnits();
		// RobotData.elevPositionTarget = elev.getElevatorPositionUnits();
	}

	/**
	 * This function is called periodically during operator control.
	 */
	@Override
	public void teleopPeriodic() {
		getControllers();
		drive.teleOp();
		elev.moveTo(RobotData.elevPositionTarget);

		arm.moveArmTo(RobotData.desiredArmAngle);
	}

	/**
	 * This function is called periodically during test mode.
	 */
	@Override
	public void testPeriodic() {
		elev.display();
		if (driver.getXButton()) {
			drive.autoDrive(0.3, 10.0, 6.0);
			drive.autoTurn(-90, 7);
			drive.autoDrive(0.3, 8.0, 2.0);
			drive.autoTurn(0, 5);
			drive.autoDrive(0.3, 10.0, 10);
			elev.moveTo(RobotData.elevStageOneMaxUnits + RobotData.elevStageTwoMaxUnits);
			arm.moveArmTo(20);
			drive.autoTurn(20, 5);
			drive.autoDrive(0.3, 7, 5.5);
		}
	}

	public void disabledPeriodic() {
		SmartDashboard.putNumber("Left Encoder Count", drive.encL.get());
		SmartDashboard.putNumber("Right Encoder Count", drive.encR.get());
		SmartDashboard.putNumber("Left Encoder Distance", drive.encL.getDistance());
		SmartDashboard.putNumber("Right Encoder Distance", drive.encR.getDistance());

		// elev.display();
	}

	boolean lastTimeElevIncrement = false;
	boolean lastTimeArmIncrement = false;

	public void getControllers() {
		/*
		 * The else if's are to ensure only one operation is commanded for each pass of
		 * the loop
		 */
		/*
		 * Elevator Operation Code +++++++++++++++++++++++++++++++++
		 */
		if (operator.getXButton()) {
			RobotData.elevPositionTarget = RobotData.elevHeightX;
		} else if (operator.getYButton()) {
			RobotData.elevPositionTarget = RobotData.elevHeightY;
		} else if (operator.getBButton()) {
			RobotData.elevPositionTarget = RobotData.elevHeightB;
		} else if (operator.getPOV(0) != -1) {
			lastTimeElevIncrement = true;
			if (operator.getPOV(0) > 270 || operator.getPOV(0) < 90) {
				RobotData.elevPositionTarget += 0.5;
			} else {
				RobotData.elevPositionTarget -= 0.5;
			}
		} else if (lastTimeElevIncrement) {
			/*
			 * This says, if the POV was used last time, when the button is released, stop
			 * at that point
			 */
			lastTimeElevIncrement = false;
			SmartDashboard.putNumber("Elevator getPosition", elev.getElevatorPositionUnits());
		//	RobotData.elevPositionTarget = elev.getElevatorPositionUnits();
			
		}

		/*
		 * Arm Operation Code +++++++++++++++++++++++++++++++++++++++++
		 */

		if (operator.getX(GenericHID.Hand.kRight) > 0.1) {
			arm.moveArmTo(RobotData.armPositionDegrees);
		} else if (operator.getX(GenericHID.Hand.kRight) < -0.1) {
			// do something
		} else if (operator.getTriggerAxis(GenericHID.Hand.kLeft) != 0) {
			lastTimeArmIncrement = true;
			RobotData.desiredArmAngle += operator.getTriggerAxis(GenericHID.Hand.kLeft);
		} else if (operator.getTriggerAxis(GenericHID.Hand.kRight) != 0) {
			lastTimeArmIncrement = true;
			RobotData.desiredArmAngle -= operator.getTriggerAxis(GenericHID.Hand.kRight);
		} else if (lastTimeArmIncrement) {
			/*
			 * This says, if the POV was used last time, when the button is released, stop
			 * at that point
			 */
			lastTimeArmIncrement = false;
			RobotData.desiredArmAngle = arm.getArmPositionUnits();
		}
		/*
		 * Grabber Operation Code ++++++++++++++++++++++++++++++++++++++++++
		 */
		if (operator.getBumper(GenericHID.Hand.kLeft)) {
			grab.moveGrabber(1);
		} else if (operator.getBumper(GenericHID.Hand.kRight)) {
			grab.moveGrabber(-1);
		}
	}
}