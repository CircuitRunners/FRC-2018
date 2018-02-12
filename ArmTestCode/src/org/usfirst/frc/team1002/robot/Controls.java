package org.usfirst.frc.team1002.robot;

import edu.wpi.first.wpilibj.GenericHID.Hand;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

public class Controls {
	/* Keep up with where both the arm and elevator are going */
	double elevatorDesiredPosition;
	double armDesiredPosition;

	double smoothScale(double value) {
		double aValue = Math.abs(value);
		if (aValue < 0.1)
			return 0.0;
		else if (aValue > 0.9)
			return value / aValue;
		/*
		 * sin is in radions, so this will only go to .78, but the statement above gives
		 * the full power
		 */
		else
			return Math.sin(value);
	}

	public void read() {
		/* This code just checks the control, moving the system to a position */
		/* if the hold the bumper, put it in motion mode */
		double elevLastPos = elevatorDesiredPosition;
		double armLastPos = armDesiredPosition;
		if (Robot.driverCntl.getBumper(Hand.kLeft)) {
			/* Percent output mode */
			double leftYStick = smoothScale(Robot.driverCntl.getY(Hand.kLeft));
			Robot.elevator.setS1Output(leftYStick);
			SmartDashboard.putString("ControllerStatus", "PercentageOutput");
		} else {
			/* check to see if they have pressed the increment/decrement button */
			int myPOV = Robot.driverCntl.getPOV();
			if (myPOV != -1) {
				if (myPOV > 90 && myPOV < 270)
					elevatorDesiredPosition -= 0.2;
				else
					elevatorDesiredPosition += 0.2;
				;
			} else if (Robot.driverCntl.getAButton()) {
				elevatorDesiredPosition = SmartDashboard.getNumber("BtnA ElevPos", elevatorDesiredPosition);
				armDesiredPosition = SmartDashboard.getNumber("BtnA Arm", armDesiredPosition);
			} else if (Robot.driverCntl.getBButton()) {
				elevatorDesiredPosition = SmartDashboard.getNumber("BtnB ElevPos", elevatorDesiredPosition);
				armDesiredPosition = SmartDashboard.getNumber("BtnB Arm", armDesiredPosition);
			} else if (Robot.driverCntl.getXButton()) {
				elevatorDesiredPosition = SmartDashboard.getNumber("BtnX ElevPos", elevatorDesiredPosition);
				armDesiredPosition = SmartDashboard.getNumber("BtnX Arm", armDesiredPosition);
			} else if (Robot.driverCntl.getYButton()) {
				elevatorDesiredPosition = SmartDashboard.getNumber("BtnY ElevPos", elevatorDesiredPosition);
				armDesiredPosition = SmartDashboard.getNumber("BtnY Arm", armDesiredPosition);
			}

			if (elevLastPos != elevatorDesiredPosition)
				Robot.elevator.moveTo(elevatorDesiredPosition);
		}
		/* Arm Controls */
		double rightYStick = Robot.driverCntl.getY(Hand.kRight);
		if (rightYStick < -0.5) {
			armDesiredPosition -= 0.1;
		} else if (rightYStick > 0.5)
			armDesiredPosition += 0.1;
		if (armLastPos != armDesiredPosition)
			Robot.arm.moveTo(armDesiredPosition);
	}
}
