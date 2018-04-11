package org.usfirst.frc.team1002.robot;

import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

public class Autonomous {

	private static int posIndex;
	private static int targIndex;
	private static int prefIndex;
	private static int blockIndex;
	private static String FMSString; // three character string from FMS.
	private static int sideSwitch; // side switch is on.
	private static int sideScale; // side scale is on.
	private static final double SCALESPEED = 0.8; // code.
	private static final double SWITCHSPEED = 0.6;
	private static boolean switchNear;
	private static boolean scaleNear;
	//private static boolean debug = false;

	int step = 1;
	int turnDir = 1;

	public void init() {

		posIndex = Robot.posSelected;
		targIndex = Robot.targSelected;
		prefIndex = Robot.prefSelected;
		blockIndex = Robot.blockSelected;

		if (posIndex == Robot.LEFT) {
			turnDir = 1;
		} else {
			turnDir = -1;
		}
		SmartDashboard.putNumber("Robot Position:", posIndex);
		SmartDashboard.putNumber("Target Selected: ", targIndex);
		SmartDashboard.putNumber("Preference:", prefIndex);
	}

	static boolean called = false;

	public void run() {
		switch (posIndex) {
		case Robot.RIGHT:
			if(prefIndex == Robot.SWITCH) {
				if(targIndex == Robot.SWITCH) {
					
				}
					
			}
		case Robot.LEFT:
			if (targIndex == Robot.SCALE) {
				if (scaleNear) {
					sameSideScale2Block();
				} else {
					farSideScale2Block();
				}
			} else if (targIndex == Robot.SWITCH) {
				if (switchNear) {
					sameSideSwitch();
				} else {
					farSideSwitch();
				}
			} else if (targIndex == Robot.SWITCHORSCALE) {
				if (switchNear)
					sameSideSwitch();
				else if (scaleNear) {
					sameSideScale2Block();
				} else {
					farSideScale2Block();
				}

			} else if (targIndex == Robot.SWITCHANDSCALE) {
				if (switchNear) {
					if (scaleNear) {
						scaleAndSwitch();
					}
				} else if (scaleNear) {
					sameSideScale2Block();
				} else {
					farSideScale2Block();
				}
			} else if (targIndex == Robot.NONE) {
				driveForward();
			} else if (targIndex == Robot.FARSWITCHANDSCALE) {
				if(!switchNear && !scaleNear) {
					farSideSwitchAndScale();
				} else if (scaleNear) {
					sameSideScale2Block();
				} else if (switchNear) {
					sameSideSwitch();
				} else {
					farSideScale2Block();
				}
			}
			break;
		case Robot.CENTER:
			if (targIndex == Robot.SWITCH) {
				if (sideScale == Robot.RIGHT) {
					turnDir = -1;
					centerSwitch();
				} else {
					turnDir = 1;
					centerSwitch();
				}

			} else {
				driveForward();
			}
			break;

		}
	}

	public boolean getAutoRoutine() {

		if (DriverStation.getInstance().getGameSpecificMessage().length() != 3)
			return false;

		// The field will take about a second to send the fms string so you cant grab
		// it in init; instead, keep looping this until you get the string, then go
		// through the algorithm.
		FMSString = DriverStation.getInstance().getGameSpecificMessage();
		SmartDashboard.putString("FMS String", FMSString);
		if (FMSString.substring(0, 1).equalsIgnoreCase("L"))
			sideSwitch = Robot.LEFT;
		else
			sideSwitch = Robot.RIGHT;
		if (FMSString.substring(1, 2).equalsIgnoreCase("L"))
			sideScale = Robot.LEFT;
		else
			sideScale = Robot.RIGHT;
		SmartDashboard.putNumber("sideScale", sideScale);
		SmartDashboard.putNumber("sideSwitch", sideSwitch);

		switchNear = (posIndex == sideSwitch);
		scaleNear = (posIndex == sideScale);
		SmartDashboard.putBoolean("switchNear", switchNear);
		SmartDashboard.putBoolean("scaleNear", scaleNear);

		return true;

	}

	double startingTime;

	private void farSideSwitch() {
		SmartDashboard.putString("Auto Program", "farSideSwitch");
		switch (step) {
		case 1:
			beginStep();
			startingTime = Timer.getFPGATimestamp();
			SmartDashboard.putNumber("Time Elapsed", 0);
			Robot.drive.autoDrive(17.6, SWITCHSPEED, 6);
			RobotData.elevPositionTarget = Robot.elev.moveTo(20, 100, 5);
			step++;
			break;
		case 2:
			if (!Robot.drive.isIdle())
				break;
			beginStep();
			Robot.drive.autoTurn(90 * turnDir, 0.5, 4);
			step++;
			break;
		case 3:
			if (!Robot.drive.isIdle())
				break;
			beginStep();
			Robot.drive.autoDrive(11, SWITCHSPEED, 6);
			RobotData.armPositionTarget = Robot.arm.moveTo(0, 100, 5);
			RobotData.elevPositionTarget = Robot.elev.moveTo(15, 100, 5);
			step++;
			break;
		case 4:
			if (!Robot.drive.isIdle())
				break;
			beginStep();
			Robot.drive.autoTurn(180 * turnDir, 0.5, 3);
			step++;
			break;
		case 5:
			if (!Robot.drive.isIdle() || !Robot.elev.isIdle() || !Robot.arm.isIdle())
				break;
			beginStep();
			Robot.drive.autoDrive(.5, SWITCHSPEED / 2, 6);
			step++;
			break;
		case 6:
			if (!Robot.drive.isIdle())
				break;
			beginStep();
			Robot.grab.eject(0.4, 1);
			step++;
			break;
		case 7:
			if (!Robot.grab.isIdle())
				break;
			beginStep();
			Robot.drive.autoDrive(-2.0, SWITCHSPEED, 4);
			step++;
			break;
		case 8:
			if (!Robot.drive.isIdle())
				break;
			beginStep();
			RobotData.elevPositionTarget = Robot.elev.moveTo(0, 100, 5);
			RobotData.armPositionTarget = Robot.arm.moveTo(-25, 100, 5);
			step++;
			break;
		case 9:
			SmartDashboard.putNumber("Time Elapsed", Timer.getFPGATimestamp() - startingTime);
			step++;
			break;
		case 10:
			break;

		}
		// driveForward();
	}

	private void sameSideSwitch() {
		SmartDashboard.putString("Auto Program", "sameSideSwitch");

		switch (step) {
		case 1:
			if (!Robot.drive.isIdle())
				break;
			beginStep();
			startingTime = Timer.getFPGATimestamp();
			SmartDashboard.putNumber("Time Elapsed", 0);
			Robot.drive.autoDrive(11, 0.5, 4);
			RobotData.elevPositionTarget = Robot.elev.moveTo(20, 100, 5);
			step++;
			break;
		case 2:
			if (!Robot.drive.isIdle())
				break;
			beginStep();
			Robot.drive.autoTurn(90 * turnDir, 0.5, 4);
			RobotData.elevPositionTarget = Robot.elev.moveTo(15, 100, 3);
			RobotData.armPositionTarget = Robot.arm.moveTo(0, 100, 3);
			step++;
			break;
		case 3:
			if (!Robot.drive.isIdle() || !Robot.elev.isIdle() || !Robot.arm.isIdle())
				break;
			beginStep();
			Robot.drive.autoDrive(0.5, SWITCHSPEED / 2, 6);
			step++;
			break;
		case 4:
			if (!Robot.drive.isIdle())
				break;
			beginStep();
			Robot.grab.eject(0.6, 0.6);
			step++;
			break;
		case 5:
			if (!Robot.grab.isIdle())
				break;
			beginStep();
			Robot.drive.autoDrive(-1.5, SWITCHSPEED / 2, 4);
			step++;
			break;
		case 6:
			if (Robot.drive.isIdle())
				break;
			beginStep();
			RobotData.elevPositionTarget = Robot.elev.moveTo(0, 100, 5);
			RobotData.armPositionTarget = Robot.arm.moveTo(-25, 100, 5);
			step++;
			break;// maybe add some code to get the next block ready
		case 7:
			SmartDashboard.putNumber("Time Elapsed", Timer.getFPGATimestamp() - startingTime);
			step++;
			break;
		case 8:
			break;
		}

	}

	private void farSideScale2Block() {
		SmartDashboard.putString("Auto Program", "farSideScale");
		switch (step) {
		case 1:
			startingTime = Timer.getFPGATimestamp();
			SmartDashboard.putNumber("Time Elapsed", 0);
			Robot.drive.autoDrive(16.3, SCALESPEED, 6);
			RobotData.elevPositionTarget = Robot.elev.moveTo(20, 100, 5);
			step++;
			break;
		case 2:
			if (!Robot.drive.isIdle())
				break;
			beginStep();
			Robot.drive.autoTurn(90 * turnDir, 0.5, 6);
			step++;
			break;
		case 3:
			if (!Robot.drive.isIdle())
				break;
			beginStep();
			Robot.drive.autoDrive(14.25, SCALESPEED, 6);
			RobotData.armPositionTarget = Robot.arm.moveTo(60, 100, 5);
			RobotData.elevPositionTarget = Robot.elev.moveTo(RobotData.elevMaxHeightUnits, 100, 5);
			step++;
			break;
		case 4:
			if (!Robot.drive.isIdle())
				break;
			beginStep();
			Robot.drive.autoTurn(0 * turnDir, 0.5, 6);
			step++;
			break;
		case 5:
			if (!Robot.drive.isIdle() || !Robot.elev.isIdle() || !Robot.arm.isIdle())
				break;
			beginStep();
			Robot.drive.autoDrive(1, SCALESPEED / 2, 4);// drive forward to deliver 1st block
			step++;
			break;
		case 6:
			if (!Robot.drive.isIdle())
				break;
			beginStep();
			Robot.grab.eject(0.6, 0.4);
			step++;
			break;
		case 7:
			if (!Robot.grab.isIdle())
				break;
			beginStep();
			Robot.drive.autoDrive(-1.75, SCALESPEED / 2, 3);
			step++;
			break;
		case 8:
			if (!Robot.drive.isIdle())
				break;
			beginStep();
			RobotData.armPositionTarget = Robot.arm.moveTo(-25, 100, 5);
			RobotData.elevPositionTarget = Robot.elev.moveTo(0, 100, 5);
			Robot.drive.autoTurn(-165 * turnDir, 0.5, 4);
			step++;
			break;
		case 9:
			if (!Robot.drive.isIdle())
				break;
			beginStep();

			Robot.drive.autoDrive(1, SCALESPEED / 2, 5);// drive forward to get block
			Robot.grab.intake(1.6, 0.4);
			step++;
			break;
		case 10:
			if (!Robot.drive.isIdle())
				break;
			beginStep();
			Robot.drive.autoTurn(-10 * turnDir, 0.5, 5);
			RobotData.elevPositionTarget = Robot.elev.moveTo(RobotData.elevMaxHeightUnits, 100, 5);
			RobotData.armPositionTarget = Robot.arm.moveTo(60, 100, 5);
			step++;
			break;
		case 11:
			if (!Robot.drive.isIdle())
				break;
			beginStep();
			Robot.drive.autoDrive(0.3, SCALESPEED / 3, 3);
			step++;
			break;
		case 12:
			if (!Robot.drive.isIdle() || !Robot.elev.isIdle())
				break;
			beginStep();
			Robot.grab.eject(0.4, 0.6);
			step++;
			break;
		case 13:
			if (!Robot.grab.isIdle())
				break;
			beginStep();
			Robot.drive.autoDrive(-1.75, SCALESPEED, 3);
			step++;
			break;
		case 14:
			if (!Robot.drive.isIdle())
				break;
			Robot.drive.autoTurn(140 * turnDir, 0.5, 3);// prepare for block 3 ;)
			RobotData.armPositionTarget = Robot.arm.moveTo(-25, 100, 5);
			RobotData.elevPositionTarget = Robot.elev.moveTo(0, 100, 5);
			step++;
			break;
		case 15:
			SmartDashboard.putNumber("Time Elapsed", Timer.getFPGATimestamp() - startingTime);
			step++;
			break;
		case 16:
			break;
		}
		// driveForward();
	}

	public void sameSideScale2Block() {
		SmartDashboard.putString("Auto Program", "sameSideScale");
		switch (step) {
		case 1:
			if (!Robot.drive.isIdle())
				break;
			beginStep();
			startingTime = Timer.getFPGATimestamp();
			SmartDashboard.putNumber("Time Elapsed", 0);
			Robot.drive.autoDrive(6.0, SCALESPEED, 4);
			RobotData.elevPositionTarget = Robot.elev.moveTo(20, 100, 5);
			step++;
			break;
		case 2:
			if (!Robot.drive.isIdle() || !Robot.elev.isIdle())
				break;
			beginStep();
			Robot.drive.autoDrive(8, SCALESPEED, 6);
			RobotData.elevPositionTarget = Robot.elev.moveTo(RobotData.elevMaxHeightUnits, 100, 5);
			RobotData.armPositionTarget = Robot.arm.moveTo(60, 100, 5);
			step++;
			break;
		case 3:
			if (!Robot.drive.isIdle() || !Robot.elev.isIdle() || !Robot.arm.isIdle())
				break;
			beginStep();
			Robot.drive.autoTurn(35 * turnDir, 0.5, 3);
			step++;
			break;
		case 4:
			if (!Robot.drive.isIdle())
				break;
			beginStep();
			Robot.drive.autoDrive(0.75, SCALESPEED / 2, 4);
			step++;
			break;
		case 5:
			if (!Robot.drive.isIdle())
				break;
			beginStep();
			Robot.grab.eject(0.6, 0.4);
			step++;
			break;
		case 6:
			if (!Robot.grab.isIdle())
				break;
			beginStep();
			Robot.drive.autoDrive(-2, SCALESPEED / 2, 3);
			step++;
			break;
		case 7:
			if (!Robot.drive.isIdle())
				break;
			beginStep();
			RobotData.armPositionTarget = Robot.arm.moveTo(-25, 100, 5);
			RobotData.elevPositionTarget = Robot.elev.moveTo(0, 100, 5);
			Robot.drive.autoTurn(142 * turnDir, 0.5, 4);
			step++;
			break;
		case 8:
			if (!Robot.drive.isIdle())
				break;
			beginStep();
			Robot.drive.autoTurn(142 * turnDir, 0.4, 3);
			step++;
			break;
		case 9:
			if (!Robot.drive.isIdle() || !Robot.elev.isIdle() || !Robot.arm.isIdle())
				break;
			beginStep();
			Robot.drive.autoDrive(2.5, SCALESPEED / 2, 5);// intakes block 2
			Robot.grab.intake(1.5, 0.75);
			step++;
			break;
		case 10:
			if (!Robot.drive.isIdle())
				break;
			beginStep();
			Robot.drive.autoDrive(-1.5, SCALESPEED / 2, 3);
			RobotData.elevPositionTarget = Robot.elev.moveTo(RobotData.elevMaxHeightUnits, 100, 5);
			step++;
			break;
		case 11:
			if (!Robot.drive.isIdle())
				break;
			beginStep();
			Robot.drive.autoTurn(35 * turnDir, 0.5, 4);
			RobotData.armPositionTarget = Robot.arm.moveTo(60, 100, 5);
			step++;
			break;
		case 12:
			if (!Robot.drive.isIdle() || !Robot.arm.isIdle())
				break;
			beginStep();
			Robot.drive.autoDrive(1, SCALESPEED / 2, 6);
			step++;
			break;
		case 13:
			if (!Robot.drive.isIdle())
				break;
			beginStep();
			Robot.grab.eject(0.6, 0.8);
			step++;
			break;
		case 14:
			if (!Robot.grab.isIdle())
				break;
			beginStep();
			Robot.drive.autoDrive(-3.5, SCALESPEED / 2, 4);
			step++;
			break;
		case 15:
			if (!Robot.drive.isIdle())
				break;
			beginStep();
			Robot.drive.autoTurn(140 * turnDir, 0.5, 3);// prepare for block 3 ;)
			RobotData.elevPositionTarget = Robot.elev.moveTo(0, 100, 5);
			RobotData.armPositionTarget = Robot.arm.moveTo(-25, 100, 5);
			step++;
			break;
		case 16:
			SmartDashboard.putNumber("Time Elapsed", Timer.getFPGATimestamp() - startingTime);
			step++;
			break;
		case 17:
			break;

		}
	}

	public void driveForward() {
		switch (step) {
		case 1:
			startingTime = Timer.getFPGATimestamp();
			SmartDashboard.putNumber("Time Elapsed", 0);
			Robot.drive.autoDrive(7, SWITCHSPEED, 5);
			RobotData.elevPositionTarget = Robot.elev.moveTo(15, 100, 5);
			step++;
			break;
		case 2:
			SmartDashboard.putNumber("Time Elapsed", Timer.getFPGATimestamp() - startingTime);
			step++;
			break;
		case 3:
			break;
		}
	}

	public void centerSwitch() {
		switch (step) {
		case 1:
			if (!Robot.drive.isIdle())
				break;
			beginStep();
			Robot.drive.autoDrive(2, SWITCHSPEED, 4);
			RobotData.elevPositionTarget = Robot.elev.moveTo(15, 100, 5);
			RobotData.armPositionTarget = Robot.arm.moveTo(0, 100, 5);
			step++;
			break;
		case 2:
			if (!Robot.drive.isIdle() || !Robot.elev.isIdle() || !Robot.arm.isIdle())
				break;
			beginStep();
			Robot.drive.autoTurn(45 * turnDir, 0.5, 4);
			step++;
			break;
		case 3:
			if (!Robot.drive.isIdle())
				break;
			beginStep();
			Robot.drive.autoDrive(6, SWITCHSPEED, 7);
			step++;
			break;
		case 4:
			if (!Robot.drive.isIdle())
				break;
			beginStep();
			Robot.drive.autoTurn(0, 0.5, 4);
			step++;
			break;
		case 5:
			if (!Robot.drive.isIdle())
				break;
			beginStep();
			Robot.drive.autoDrive(2, SWITCHSPEED / 2, 4);
			step++;
			break;
		case 6:
			if (!Robot.drive.isIdle())
				break;
			beginStep();
			Robot.grab.eject(0.6, 0.6);
			step++;
			break;
		case 7:
			if (!Robot.grab.isIdle())
				break;
			beginStep();
			Robot.drive.autoDrive(-2, SWITCHSPEED, 3);
			RobotData.elevPositionTarget = Robot.elev.moveTo(0, 100, 5);
			RobotData.armPositionTarget = Robot.arm.moveTo(-25, 100, 5);
			step++;
			break;
		case 8:
			SmartDashboard.putNumber("Time Elapsed", Timer.getFPGATimestamp() - startingTime);
			step++;
			break;
		case 9:
			break;

		}
	}

	public void scaleAndSwitch() {
		SmartDashboard.putString("Auto Program", "switchAndScale");
		switch (step) {
		case 1:
			beginStep();
			startingTime = Timer.getFPGATimestamp();
			SmartDashboard.putNumber("Time Elapsed", 0);
			Robot.drive.autoDrive(11, 0.5, 4);
			RobotData.elevPositionTarget = Robot.elev.moveTo(20, 100, 5);
			step++;
			break;
		case 2:
			if (!Robot.drive.isIdle())
				break;
			beginStep();
			Robot.drive.autoTurn(90 * turnDir, 0.5, 4);
			RobotData.elevPositionTarget = Robot.elev.moveTo(15, 100, 3);
			RobotData.armPositionTarget = Robot.arm.moveTo(0, 100, 2);
			step++;
			break;
		case 3:
			if (!Robot.drive.isIdle() || !Robot.arm.isIdle())
				break;
			beginStep();
			Robot.drive.autoDrive(0.5, SWITCHSPEED / 2, 6);
			step++;
			break;
		case 4:
			if (!Robot.drive.isIdle())
				break;
			beginStep();
			Robot.grab.eject(0.3, 1);
			step++;
			break;
		case 5:
			if (!Robot.grab.isIdle())
				break;
			beginStep();
			Robot.drive.autoDrive(-1, SWITCHSPEED / 2, 4);
			step++;
			break;
		case 6:
			if (!Robot.drive.isIdle())
				break;
			beginStep();
			Robot.drive.autoTurn(0, 0.5, 4);
			RobotData.armPositionTarget = Robot.arm.moveTo(-25, 100, 2);
			RobotData.elevPositionTarget = Robot.elev.moveTo(0, 100, 3);
			step++;
			break;
		case 7:
			if (!Robot.drive.isIdle())
				break;
			beginStep();
			Robot.drive.autoDrive(3, SCALESPEED, 3);
			step++;
			break;
		case 8:
			if (!Robot.drive.isIdle() || !Robot.elev.isIdle() || !Robot.arm.isIdle())
				break;
			beginStep();
			Robot.drive.autoTurn(110 * turnDir, 0.5, 3);
			step++;
			break;
		case 9:
			if (!Robot.drive.isIdle())
				break;
			beginStep();
			Robot.drive.autoDrive(1.8, SCALESPEED / 2, 3);
			Robot.grab.intake(0.8, 0.5);
			step++;
			break;
		case 10:
			if (!Robot.drive.isIdle())
				break;
			beginStep();
			Robot.drive.autoDrive(-1.5, SCALESPEED / 2, 3);
			// Robot.grab.intake(0.8, 0.5);
			step++;
			break;
		case 11:
			if (!Robot.drive.isIdle())
				break;
			beginStep();
			Robot.drive.autoTurn(20 * turnDir, 0.5, 3);
			RobotData.elevPositionTarget = Robot.elev.moveTo(RobotData.elevMaxHeightUnits, 100, 4);
			RobotData.armPositionTarget = Robot.arm.moveTo(70, 100, 4);
			step++;
			break;
		case 12:
			if (!Robot.drive.isIdle() || !Robot.elev.isIdle() || !Robot.arm.isIdle())
				break;
			beginStep();
			Robot.drive.autoDrive(7.5, SCALESPEED, 3);
			step++;
			break;
		case 13:
			if (!Robot.drive.isIdle())
				break;
			beginStep();
			Robot.grab.eject(0.5, 0.5);
			step++;
			break;
		case 14:
			if (!Robot.grab.isIdle())
				break;
			beginStep();
			Robot.drive.autoDrive(-2, SCALESPEED, 3);
			step++;
			break;
		case 15:
			if (!Robot.drive.isIdle())
				break;
			beginStep();
			Robot.drive.autoDrive(-2, SCALESPEED, 3);
			RobotData.elevPositionTarget = Robot.elev.moveTo(0, 100, 4);
			RobotData.armPositionTarget = Robot.arm.moveTo(-25, 100, 4);
			step++;
			break;
		case 16:
			if (!Robot.drive.isIdle() || !Robot.elev.isIdle() || !Robot.arm.isIdle())
				break;
			SmartDashboard.putNumber("Time Elapsed", Timer.getFPGATimestamp() - startingTime);
			step++;
			break;
		case 17:
			break;
		}

	}

	public void sameSideScale3Block() {// heh heh maybe
		SmartDashboard.putString("Auto Program", "sameSideScale");
		switch (step) {
		case 1:
			if (!Robot.drive.isIdle())
				break;
			beginStep();
			startingTime = Timer.getFPGATimestamp();
			SmartDashboard.putNumber("Time Elapsed", 0);
			Robot.drive.autoDrive(6.0, SCALESPEED, 4);
			RobotData.elevPositionTarget = Robot.elev.moveTo(20, 100, 5);
			step++;
			break;
		case 2:
			if (!Robot.drive.isIdle() || !Robot.elev.isIdle())
				break;
			beginStep();
			Robot.drive.autoDrive(8, SCALESPEED, 6);
			RobotData.elevPositionTarget = Robot.elev.moveTo(RobotData.elevMaxHeightUnits, 100, 5);
			RobotData.armPositionTarget = Robot.arm.moveTo(60, 100, 5);
			step++;
			break;
		case 3:
			if (!Robot.drive.isIdle() || !Robot.elev.isIdle() || !Robot.arm.isIdle())
				break;
			beginStep();
			Robot.drive.autoTurn(35 * turnDir, 0.5, 3);
			step++;
			break;
		case 4:
			if (!Robot.drive.isIdle())
				break;
			beginStep();
			Robot.drive.autoDrive(0.75, SCALESPEED / 2, 4);
			step++;
			break;
		case 5:
			if (!Robot.drive.isIdle())
				break;
			beginStep();
			Robot.grab.eject(0.6, 0.4);
			step++;
			break;
		case 6:
			if (!Robot.grab.isIdle())
				break;
			beginStep();
			Robot.drive.autoDrive(-2, SCALESPEED / 2, 3);
			step++;
			break;
		case 7:
			if (!Robot.drive.isIdle())
				break;
			beginStep();
			RobotData.armPositionTarget = Robot.arm.moveTo(-25, 100, 5);
			RobotData.elevPositionTarget = Robot.elev.moveTo(0, 100, 5);
			Robot.drive.autoTurn(142 * turnDir, 0.5, 4);
			step++;
			break;
		case 8:
			if (!Robot.drive.isIdle())
				break;
			beginStep();
			Robot.drive.autoTurn(142 * turnDir, 0.4, 3);
			step++;
			break;
		case 9:
			if (!Robot.drive.isIdle() || !Robot.elev.isIdle() || !Robot.arm.isIdle())
				break;
			beginStep();
			Robot.drive.autoDrive(2.5, SCALESPEED / 2, 5);// intakes block 2
			Robot.grab.intake(1.5, 0.75);
			step++;
			break;
		case 10:
			if (!Robot.drive.isIdle())
				break;
			beginStep();
			Robot.drive.autoDrive(-1.5, SCALESPEED / 2, 3);
			RobotData.elevPositionTarget = Robot.elev.moveTo(RobotData.elevMaxHeightUnits, 100, 5);
			step++;
			break;
		case 11:
			if (!Robot.drive.isIdle())
				break;
			beginStep();
			Robot.drive.autoTurn(35 * turnDir, 0.5, 4);
			RobotData.armPositionTarget = Robot.arm.moveTo(60, 100, 5);
			step++;
			break;
		case 12:
			if (!Robot.drive.isIdle() || !Robot.arm.isIdle())
				break;
			beginStep();
			Robot.drive.autoDrive(1, SCALESPEED / 2, 6);
			step++;
			break;
		case 13:
			if (!Robot.drive.isIdle())
				break;
			beginStep();
			Robot.grab.eject(0.6, 0.8);
			step++;
			break;
		case 14:
			if (!Robot.grab.isIdle())
				break;
			beginStep();
			Robot.drive.autoDrive(-3.5, SCALESPEED / 2, 4);
			step++;
			break;
		case 15:
			if (!Robot.drive.isIdle())
				break;
			beginStep();
			Robot.drive.autoTurn(140 * turnDir, 0.5, 3);// prepare for block 3 ;)
			RobotData.elevPositionTarget = Robot.elev.moveTo(0, 100, 5);
			RobotData.armPositionTarget = Robot.arm.moveTo(-25, 100, 5);
			step++;
			break;
		case 16:
			SmartDashboard.putNumber("Time Elapsed", Timer.getFPGATimestamp() - startingTime);
			step++;
			break;
		case 17:
			break;

		}
	}

	public void farSideSwitchAndScale() {
		switch (step) {
		case 1:
			startingTime = Timer.getFPGATimestamp();
			SmartDashboard.putNumber("Time Elapsed", 0);
			Robot.drive.autoDrive(16.3, SCALESPEED, 6);
			RobotData.elevPositionTarget = Robot.elev.moveTo(20, 100, 5);
			step++;
			break;
		case 2:
			if (!Robot.drive.isIdle())
				break;
			beginStep();
			Robot.drive.autoTurn(90 * turnDir, 0.5, 6);
			step++;
			break;
		case 3:
			if (!Robot.drive.isIdle())
				break;
			beginStep();
			Robot.drive.autoDrive(14.25, SCALESPEED, 6);
			RobotData.armPositionTarget = Robot.arm.moveTo(60, 100, 5);
			RobotData.elevPositionTarget = Robot.elev.moveTo(RobotData.elevMaxHeightUnits, 100, 5);
			step++;
			break;
		case 4:
			if (!Robot.drive.isIdle())
				break;
			beginStep();
			Robot.drive.autoTurn(0 * turnDir, 0.5, 6);
			step++;
			break;
		case 5:
			if (!Robot.drive.isIdle() || !Robot.elev.isIdle() || !Robot.arm.isIdle())
				break;
			beginStep();
			Robot.drive.autoDrive(1, SCALESPEED / 2, 4);// drive forward to deliver 1st block
			step++;
			break;
		case 6:
			if (!Robot.drive.isIdle())
				break;
			beginStep();
			Robot.grab.eject(0.6, 0.4);
			step++;
			break;
		case 7:
			if (!Robot.grab.isIdle())
				break;
			beginStep();
			Robot.drive.autoDrive(-1.75, SCALESPEED / 2, 3);
			step++;
			break;
		case 8:
			if (!Robot.drive.isIdle())
				break;
			beginStep();
			RobotData.armPositionTarget = Robot.arm.moveTo(-25, 100, 5);
			RobotData.elevPositionTarget = Robot.elev.moveTo(0, 100, 5);
			Robot.drive.autoTurn(-165 * turnDir, 0.5, 4);
			step++;
			break;
		case 9:
			if (!Robot.drive.isIdle())
				break;
			beginStep();
			Robot.drive.autoDrive(1, SCALESPEED / 2, 5);// drive forward to get block
			Robot.grab.intake(1.6, 0.4);
			step++;
			break;
		case 10:
			if (!Robot.drive.isIdle())
				break;
			beginStep();
			Robot.drive.autoDrive(-0.25, SCALESPEED / 2, 2);
			RobotData.elevPositionTarget = Robot.elev.moveTo(15, 100, 3);
			RobotData.armPositionTarget = Robot.arm.moveTo(0, 100, 3);
			step++;
			break;
		case 11:
			if (!Robot.arm.isIdle())
				break;
			beginStep();
			Robot.grab.eject(0.5, 0.9);
			step++;
			break;
		case 12:
			beginStep();
			step++;
			break;
		case 13:
			break;
		}
	}

	boolean delayed = false;

	void beginStep() {

		SmartDashboard.putNumber("Time Elapsed", Timer.getFPGATimestamp() - startingTime);
		if (delayed) {
			Timer.delay(1);
		}
	}
}
