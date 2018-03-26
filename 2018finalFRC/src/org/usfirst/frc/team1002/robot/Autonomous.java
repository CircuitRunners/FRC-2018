package org.usfirst.frc.team1002.robot;

import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

public class Autonomous {

	private static int posIndex;
	private static int targIndex;
	private static String FMSString; // three character string from FMS.
	private static int sideSwitch; // side switch is on.
	private static int sideScale; // side scale is on.
	private static final double SPEED = 0.6; // code.
	private static boolean switchNear;
	private static boolean scaleNear;
	private static boolean debug = false;

	int step = 1;
	int turnDir = 1;

	// private static MarioDrive drive = new MarioDrive();
	// private static EightBitElev elev = new EightBitElev();
	// private static RobotArm arm = new RobotArm();
	// private static Grabber grab = new Grabber();

	static final int LEFT = 1;
	static final int CENTER = 2;
	static final int RIGHT = 3;
	static final int SWITCH = 10;
	static final int SCALE = 11;
	static final int NONE = 12;

	public void init() {
		if (debug)
			return;
		switch (Robot.posSelected) {
		case (LEFT):
			posIndex = LEFT;
			break;
		case (CENTER):
			posIndex = CENTER;
			break;
		case (RIGHT):
			posIndex = RIGHT;
			break;
		}
		switch (Robot.targSelected) {
		case SCALE:
			targIndex = SCALE;
			break;
		case SWITCH:
			targIndex = SWITCH;
			break;
		}
		if (posIndex == LEFT) {
			turnDir = 1;
		} else if (posIndex == RIGHT) {
			turnDir = -1;
		} else {
			turnDir = 0;
		}
		SmartDashboard.putNumber("Robot Position", posIndex);
		SmartDashboard.putNumber("Target Selected: ", targIndex);

	}

	static boolean called = false;

	public void run() {
		switch (posIndex) {
		case RIGHT:

		case LEFT:
			if (targIndex == SCALE) {
				if (scaleNear) {
					sameSideScale();
				} else {
					farSideScale();
				}
			} else if (targIndex == SWITCH) {
				if (switchNear) {
					sameSideSwitch();
				} else {
					farSideSwitch();
				}
			}
			break;
		case CENTER:
			SmartDashboard.putString("Auto Program", "Drive Forward");
			driveForward();
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
		if(FMSString.substring(0,1).equalsIgnoreCase("L"))
			sideSwitch = LEFT;
		else
			sideSwitch = RIGHT;
		if(FMSString.substring(1,2).equalsIgnoreCase("L"))
			sideScale = LEFT;
		else
			sideScale = RIGHT;
		SmartDashboard.putNumber("sideScale",sideScale);
		SmartDashboard.putNumber("sideSwitch",sideSwitch);

		switchNear = (posIndex == sideSwitch);
		scaleNear = (posIndex == sideScale);
		SmartDashboard.putBoolean("switchNear", switchNear);
		SmartDashboard.putBoolean("scaleNear", scaleNear);
		
		return true;

	}


	private void farSideSwitch() {
		SmartDashboard.putString("Auto Program", "farSideSwitch");
		switch (step) {
		case 1:
			Robot.drive.autoDrive(SPEED, 6, 18.0);
			RobotData.elevPositionTarget = Robot.elev.moveTo(20, 100);
			step++;
			break;
		case 2:
			if (!Robot.drive.isIdle() || !Robot.elev.isIdle())
				break;
			Robot.drive.autoTurn(90 * turnDir, 4);
			step++;
			break;
		case 3:
			if (!Robot.drive.isIdle())
				break;
			Robot.drive.autoDrive(SPEED, 6, 18.0);
			RobotData.armPositionTarget = Robot.arm.moveTo(10, 100);
			RobotData.elevPositionTarget = Robot.elev.moveTo(27, 100);
			step++;
			break;
		case 4:
			if (!Robot.drive.isIdle() || !Robot.elev.isIdle() || !Robot.arm.isIdle())
				break;
			Robot.drive.autoTurn(-150 * turnDir, 3);
			step++;
			break;
		case 5:
			if (!Robot.drive.isIdle() || !Robot.elev.isIdle() || !Robot.arm.isIdle())
				break;
			Robot.drive.autoDrive(SPEED, 6, 3.0);
			step++;
			break;
		case 6:
			if (!Robot.drive.isIdle())
				break;
			Robot.grab.autoRelease();
			step++;
			break;
		case 7:
			if (!Robot.grab.isIdle())
				break;
			Robot.drive.autoDrive(SPEED, 4, -6.0);
			step++;
			break;
		case 8:
			if (Robot.drive.isIdle())
				break;
			RobotData.elevPositionTarget = Robot.elev.moveTo(0, 100);
			step++;
			break;
		case 9:
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
			Robot.drive.autoDrive(0.5, 4, 12.5);
			RobotData.elevPositionTarget = Robot.elev.moveTo(20, 100);
			step++;
			break;
		case 2:
			if (!Robot.drive.isIdle() || !Robot.elev.isIdle()) {
				break;
			}
			Robot.drive.autoTurn(90 * turnDir, 4);
			RobotData.elevPositionTarget = Robot.elev.moveTo(26, 100);
			RobotData.armPositionTarget = Robot.arm.moveTo(10, 100);
			step++;
			break;
		case 3:
			if (!Robot.drive.isIdle() || !Robot.elev.isIdle() || !Robot.arm.isIdle())
				break;
			Robot.drive.autoDrive(SPEED, 6, 3.0);
			step++;
			break;
		case 4:
			if (!Robot.drive.isIdle())
				break;
			Robot.grab.autoRelease();
			step++;
			break;
		case 5:
			if (!Robot.grab.isIdle())
				break;
			Robot.drive.autoDrive(SPEED, 4, -4.0);
			step++;
			break;
		case 6:
			if (Robot.drive.isIdle())
				break;
			RobotData.elevPositionTarget = Robot.elev.moveTo(0, 100);
			step++;
			break;
		case 7:
			break;
		}
		
	}

	private void farSideScale() {
				SmartDashboard.putString("Auto Program", "farSideScale");
		switch (step) {
		case 1:
			Robot.drive.autoDrive(SPEED, 6, 18.0);
			RobotData.elevPositionTarget = Robot.elev.moveTo(20, 100);
			step++;
			break;
		case 2:
			if (!Robot.drive.isIdle() || !Robot.elev.isIdle())
				break;
			Robot.drive.autoTurn(90 * turnDir, 4);
			step++;
			break;
		case 3:
			if (!Robot.drive.isIdle())
				break;
			Robot.drive.autoDrive(SPEED, 6, 18.5);
			RobotData.armPositionTarget = Robot.arm.moveTo(55, 100);
			RobotData.elevPositionTarget = Robot.elev.moveTo(RobotData.elevMaxHeightUnits, 100);
			step++;
			break;
		case 4:
			if (!Robot.drive.isIdle() || !Robot.elev.isIdle() || !Robot.arm.isIdle())
				break;
			Robot.drive.autoTurn(-40 * turnDir, 2);
			step++;
			break;
		case 5:
			if (!Robot.drive.isIdle())
				break;
			Robot.drive.autoDrive(0.4, 4, 6.0);
			step++;
			break;
		case 6:
			if (!Robot.drive.isIdle() || !Robot.grab.isIdle())
				break;
			Robot.grab.autoRelease();
			step++;
			break;
		case 7:
			if (!Robot.grab.isIdle())
				break;
			Robot.drive.autoDrive(0.5, 3, -6.0);
			step++;
			break;
		case 8:
			if (!Robot.drive.isIdle() || !Robot.elev.isIdle())
				break;
			RobotData.armPositionTarget = Robot.arm.moveTo(10, 100);
			RobotData.elevPositionTarget = Robot.elev.moveTo(0, 100);
			Robot.drive.autoTurn(-110 * turnDir, 4);
			step++;
			break;
		case 9:
			break;

		}
		// driveForward();
	}

	public void sameSideScale() {
		SmartDashboard.putString("Auto Program", "sameSideScale");
		switch (step) {
		case 1:
			if (!Robot.drive.isIdle())
				break;
			Robot.drive.autoDrive(0.5, 4, 10.0);
			RobotData.elevPositionTarget = Robot.elev.moveTo(20, 100);
			step++;
			break;
		case 2:
			if (!Robot.drive.isIdle() || !Robot.elev.isIdle() || !Robot.arm.isIdle())
				break;
			Robot.drive.autoDrive(0.3, 6, 8.8);
			RobotData.elevPositionTarget = Robot.elev.moveTo(RobotData.elevMaxHeightUnits, 100);
			RobotData.armPositionTarget = Robot.arm.moveTo(30, 100);
			step++;
			break;
		case 3:
			if (!Robot.drive.isIdle() || !Robot.elev.isIdle() || !Robot.arm.isIdle())
				break;
			Robot.drive.autoTurn(35 * turnDir, 2);
			step++;
			break;
		case 4:
			if (!Robot.drive.isIdle())
				break;
			Robot.drive.autoDrive(0.4, 4, 6.0);
			step++;
			break;
		case 5:
			if (!Robot.drive.isIdle() || !Robot.grab.isIdle())
				break;
			Robot.grab.autoRelease();
			step++;
			break;
		case 6:
			if (!Robot.grab.isIdle())
				break;
			Robot.drive.autoDrive(0.5, 3, -6.0);
			step++;
			break;
		case 7:
			if (!Robot.drive.isIdle() || !Robot.elev.isIdle())
				break;
			RobotData.armPositionTarget = Robot.arm.moveTo(10, 100);
			RobotData.elevPositionTarget = Robot.elev.moveTo(0, 100);
			Robot.drive.autoTurn(110 * turnDir, 4);
			step++;
			break;
		case 8:
			break;

		}
	}

	public void driveForward() {
		switch (step) {
		case 1:
			Robot.drive.autoDrive(SPEED, 5, 10.5);
			step++;
			break;
		case 2:
			break;
		}
	}
}
