package org.usfirst.frc.team1002.robot;

import edu.wpi.first.wpilibj.IterativeRobot;
import edu.wpi.first.wpilibj.XboxController;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

/**
 * This is a demo program showing the use of the RobotDrive class. The
 * SampleRobot class is the base of a robot application that will automatically
 * call your Autonomous and OperatorControl methods at the right time as
 * controlled by the switches on the driver station or the field controls.
 *
 * The VM is configured to automatically run this class, and to call the
 * functions corresponding to each mode, as described in the SampleRobot
 * documentation. If you change the name of this class or the package after
 * creating this project, you must also update the manifest file in the resource
 * directory.
 *
 * WARNING: While it may look like a good choice to use for your code if you're
 * inexperienced, don't. Unless you know what you are doing, complex code will
 * be much more difficult under this system. Use IterativeRobot or Command-Based
 * instead if you're new.
 */
public class Robot extends IterativeRobot {

	final String defaultAuto = "Default";
	final String customAuto = "My Auto";

	// static RobotData robotData = new RobotData();
	static Elevator elevator = new Elevator();
	static Arm arm = new Arm();
	static Controls control = new Controls();
	static double elevatorDesiredPostion = 0.0;
	static double armDesiredPostion = 0.0;
	static XboxController driverCntl = new XboxController(0);
	SendableChooser<String> autoChoice1 = new SendableChooser<String>();

	@Override
	public void robotInit() {
		autoChoice1.addDefault("Option 1", "Opt-1");
		autoChoice1.addObject("Option 2", "Opt-2");
		autoChoice1.addObject("Option 3", "Opt-3");
		SmartDashboard.putData("First Choice", autoChoice1);
		SmartDashboard.putData("Auto modes", autoChoice1);
		SmartDashboard.putNumber("X Button Position", 0.0);
		SmartDashboard.putNumber("Y Button Position", 15.0);
		SmartDashboard.putNumber("B Button Position", 30.0);
		elevator.init();
		arm.init();

		/* Preload the defaults in the dashboard */

		SmartDashboard.putNumber("BtnA ElevPos", RobotData.A_ElevPosition);
		SmartDashboard.putNumber("BtnA ArmPos", RobotData.A_ArmPosition);

		SmartDashboard.putNumber("BtnB ElevPos", RobotData.B_ElevPosition);
		SmartDashboard.putNumber("BtnB ArmPos", RobotData.B_ArmPosition);

		SmartDashboard.putNumber("BtnX ElevPos", RobotData.X_ElevPosition);
		SmartDashboard.putNumber("BtnX ArmPos", RobotData.X_ArmPosition);

		SmartDashboard.putNumber("BtnY ElevPos", RobotData.Y_ElevPosition);
		SmartDashboard.putNumber("BtnY ArmPos", RobotData.Y_ArmPosition);

	}

	/**
	 * This autonomous (along with the chooser code above) shows how to select
	 * between different autonomous modes using the dashboard. The sendable chooser
	 * code works with the Java SmartDashboard. If you prefer the LabVIEW Dashboard,
	 * remove all of the chooser code and uncomment the getString line to get the
	 * auto name from the text box below the Gyro
	 *
	 * You can add additional auto modes by adding additional comparisons to the
	 * if-else structure below with additional strings. If using the SendableChooser
	 * make sure to add them to the chooser code above as well.
	 */
	@Override
	public void autonomousPeriodic() {
	}

	/**
	 * Runs the motors with arcade steering.
	 */
	@Override
	public void teleopPeriodic() {
		/* This code just loops, moving the system to a position */
		while (isOperatorControl() && isEnabled()) {
			control.read();
			elevator.performLoop();
			arm.checkState();
		}
	}

	/**
	 * Runs during test mode
	 */
	@Override
	public void testPeriodic() {
	}
}