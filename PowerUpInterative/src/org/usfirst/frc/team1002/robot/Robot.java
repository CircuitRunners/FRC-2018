/*----------------------------------------------------------------------------*/
/* Copyright (c) 2017-2018 FIRST. All Rights Reserved.                        */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package org.usfirst.frc.team1002.robot;

import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.GenericHID;
import edu.wpi.first.wpilibj.IterativeRobot;
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
	
	public static XboxController driver = new XboxController(RobotMap.driverPort);
	static MarioDrive drive;
	//MyAutonomous auto;
	gcbCameraSample cam;
	String gameData;
	/**
	 * This function is run when the robot is first started up and should be
	 * used for any initialization code.
	 */

	@Override
	public void robotInit() {
		drive = new MarioDrive();
		cam = new gcbCameraSample();
		//auto = new MyAutonomous();
		drive.gyro.reset();
		cam.cameraInit();
		
	}
	
	

	/**
	 * This autonomous (along with the chooser code above) shows how to select
	 * between different autonomous modes using the dashboard. The sendable
	 * chooser code works with the Java SmartDashboard. If you prefer the
	 * LabVIEW Dashboard, remove all of the chooser code and uncomment the
	 * getString line to get the auto name from the text box below the Gyro
	 *
	 * <p>You can add additional auto modes by adding additional comparisons to
	 * the switch structure below with additional strings. If using the
	 * SendableChooser make sure to add them to the chooser code above as well.
	 */
	
	@Override
	public void autonomousInit() {
		// autoSelected = SmartDashboard.getString("Auto Selector",
		// defaultAuto);
		gameData = DriverStation.getInstance().getGameSpecificMessage();
		SmartDashboard.putString("Game Data",gameData);
		
	}

	/**
	 * This function is called periodically during autonomous.
	 */
	@Override
	public void autonomousPeriodic() {
		if(gameData.charAt(0) == 'L') {
			SmartDashboard.putString("Data Recieved", "Left");
		}
	}

	/**
	 * This function is called periodically during operator control.
	 */
	@Override
	public void teleopPeriodic() {

		if(driver.getAButton()) {
			drive.autoDrive(0.2, 3.0, 3.0);
		}else if(driver.getBumper(GenericHID.Hand.kRight)) {
			drive.autoDrive(0.2, 3.0, -3.0);
		}else if(driver.getBumper(GenericHID.Hand.kLeft)) {
			drive.autoTurn(90, 4.0);
		}else if(driver.getXButton()) {
			drive.autoTurn(0.0, 4.0);
		}else if(driver.getYButton()) {
			drive.autoDrive(0.3, 10.0, 10.0);
			drive.autoTurn(90, 5.0);
			drive.autoDrive(0.2, 6.0, 4.0);
		}
		else {
		drive.teleOp();
		}
	}

	/**
	 * This function is called periodically during test mode.
	 */
	@Override
	public void testPeriodic() {
		
	}
	@Override
	public void disabledPeriodic() {
		SmartDashboard.putNumber("Left Encoder Count", drive.encL.get());
		SmartDashboard.putNumber("Left Encoder Distance", drive.encL.getDistance());
		SmartDashboard.putNumber("Right Encoder Count", drive.encR.get());
		SmartDashboard.putNumber("Right Encoder Distance", drive.encR.getDistance());	
	}
	
		
}
