package org.usfirst.frc.team1002.robot;

import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

public class Autonomous {

	private static String posIndex;
	private static int targIndex;
	private static boolean altIndex;
	private static String FMSString; // three character string from FMS.
	private static String sideSwitch; // side switch is on.
	private static String sideScale; // side scale is on.
	private static char handed; // L or R, which direction robot should turn first. Used to reduce repeated
	private static final double SPEED = 0.3; // code.
	private static boolean switchNear;
	private static boolean scaleNear;
	private static boolean debug = true;

	// private static MarioDrive drive = new MarioDrive();
	// private static EightBitElev elev = new EightBitElev();
	// private static RobotArm arm = new RobotArm();
	// private static Grabber grab = new Grabber();

	public void init() {
		if (debug)
			return;
		switch (Robot.posSelected) {
		case (Robot.posLeft):
			posIndex = "L";
			break;
		case (Robot.posCenter):
			posIndex = "C";
			break;
		case (Robot.posRight):
			posIndex = "R";
			break;
		}
		switch (Robot.targSelected) {
		case (Robot.targSwitch):
			targIndex = 1;
			break;
		case (Robot.targScale):
			targIndex = 2;
			break;
		case (Robot.targLine):
			targIndex = 3;
			break;
		}
		switch (Robot.altSelected) {
		case (Robot.altFalse):
			altIndex = false;
			break;
		case (Robot.altTrue):
			altIndex = true;
			break;
		}
		FMSString = DriverStation.getInstance().getGameSpecificMessage();
		sideSwitch = FMSString.substring(0, 1);
		sideScale = FMSString.substring(1, 2);

		switch (targIndex) {
		case (1):
			if (posIndex.equalsIgnoreCase(sideSwitch)) {
				if (posIndex == "L") {
					handed = 'R';
				} else {
					handed = 'L';
				}
			} else {
				if (posIndex == "R") {
					handed = 'L';
				} else {
					handed = 'R';
				}
			}
			break;
		case (2):
			if (posIndex == sideScale) {
				if (posIndex == "L") {
					handed = 'R';
				} else {
					handed = 'L';
				}
			} else {
				if (posIndex == "R") {
					handed = 'L';
				} else {
					handed = 'R';
				}
			}
			break;
		}
		if (Robot.chooserPos.getSelected() == Robot.posCenter) {

		}

	}

	static boolean called = false;

	public void run() {
		if (called)
			return;
		Robot.drive.autoDrive(.4, 12.5, 8);
		called = true;
	}

	public void getAutoRoutine() {

		if (DriverStation.getInstance().getGameSpecificMessage().length() == 3) {
			// The field will take about a second to send the fms string so you cant grab
			// it in init; instead, keep looping this until you get mthe string, then go
			// through the algorithm.
			FMSString = DriverStation.getInstance().getGameSpecificMessage();
			sideSwitch = FMSString.substring(0, 1);
			sideScale = FMSString.substring(1, 2);

			switch (posIndex) {
			case "L":
				switch (sideSwitch) {
				case "L":
					switchNear = true;
				case "R":
					switchNear = false;
				}
			case "'R":
				switch (sideSwitch) {
				case "L":
					switchNear = false;
				case "R":
					switchNear = true;
				}
			}
		}
	}

	int step = 1;

	public void sameSideScale() {
		SmartDashboard.putNumber("SameSideScale:Step #", step);
		switch (step) {
		case 1:
			Robot.drive.autoDrive(SPEED, 10.0, 6.0);
			step++;
		case 2:
			if (!Robot.drive.isIdle()) {
				break;
			}
			Robot.drive.autoTurn(-90, 7);
			step++;
		case 3:
			if (!Robot.drive.isIdle()) {
				break;
			}
			Robot.drive.autoDrive(SPEED, 8.0, 2.0);
			step++;
		case 4:
			if (!Robot.drive.isIdle()) {
				break;
			}
			Robot.drive.autoTurn(0, 5);
			step++;
		case 5:
			if (!Robot.drive.isIdle()) {
				break;
			}
			Robot.drive.autoDrive(SPEED, 10.0, 10);
			step++;
		case 6:
			if (!Robot.elev.isIdle()) {
				break;
			}
			Robot.elev.moveTo(20);
			step++;
		case 7:
			if (!Robot.arm.isIdle() || Robot.elev.isIdle()) {
				break;
			}
			Robot.elev.moveTo(RobotData.elevMaxHeightUnits);
			 Robot.arm.moveTo(30);
			step++;
		case 8:
			if (!Robot.drive.isIdle() || !Robot.arm.isIdle() || !Robot.elev.isIdle()) {
				break;
			}
			Robot.drive.autoTurn(20, 5);
			step++;
		case 9:
			if (!Robot.drive.isIdle()) {
				break;
			}
			Robot.drive.autoDrive(SPEED, 7, 5.5);
			step++;
			break;
		case 10:
			if(!Robot.drive.isIdle())
				break;
			Robot.grab.autoRelease();
			step++;
			break;
		case 11:
			if(!Robot.grab.isIdle())
				break;
			Robot.drive.autoDrive(SPEED, 4,-4.0);
			step++;
			break;
		case 12:
			if(!Robot.drive.isIdle())
				break;
			Robot.arm.moveTo(10);
			Robot.elev.moveTo(0);
			step++;
			break;
		// dodododododododododododododododooooo
		}
	}

	int switchStep = 1;

	public void switchAutoTester() {
		switch (switchStep) {
		case 1:
			if (!Robot.drive.isIdle())
				break;
			Robot.drive.autoDrive(0.3, 3, 1.5);
			switchStep++;
			break;
		case 2:
			int turnSign = (sideSwitch.equalsIgnoreCase("L")) ? -1 : 1;
			if (!Robot.drive.isIdle())
				break;
			Robot.drive.autoTurn(35 * turnSign, 2);
			switchStep++;
			break;
		case 3:
			if (!Robot.drive.isIdle())
				break;
			Robot.drive.autoDrive(0.5, 4, 6);
			switchStep++;
			break;
		case 4:
			double turnSign2 = (sideSwitch.equalsIgnoreCase("L")) ? 1 : -1;
			if (!Robot.drive.isIdle())
				break;
			Robot.drive.autoTurn(35 * turnSign2, 2);
			switchStep++;
			break;
		case 6:
			if (!Robot.drive.isIdle())
				break;
			Robot.elev.moveTo(10);
			RobotData.elevPositionTarget = 10;
			Robot.arm.moveTo(0);
			RobotData.armPositionTarget = 0;
			Robot.drive.autoDrive(0.4, 3, 4);
			switchStep++;
			break;
		case 7:
			if (!Robot.elev.isIdle() || !Robot.arm.isIdle() || !Robot.drive.isIdle())
				break;
			Robot.grab.moveGrabber(1);
			switchStep++;
			break;
		case 8:
			break;
		}

	}

	int scaleStep = 1;

	public void ScaleAutoV2() {
		switch (scaleStep) {
		case 1:
			if (!Robot.drive.isIdle())
				break;
			Robot.drive.autoDrive(0.5, 4, 10.0);
			scaleStep++;
			break;
		case 2:
			if (!Robot.drive.isIdle() || !Robot.elev.isIdle() || !Robot.arm.isIdle())
				break;
			Robot.drive.autoDrive(0.3, 6, 17.75);
			Robot.elev.moveTo(30);
			Robot.arm.moveTo(5);
			scaleStep++;
			break;
		case 3:
			if (!Robot.drive.isIdle() || !Robot.elev.isIdle() || !Robot.arm.isIdle())
				break;
			Robot.drive.autoTurn(-20, 2);
			scaleStep++;
			break;
		case 4:
			if (!Robot.drive.isIdle())
				break;
			Robot.drive.autoDrive(0.4, 4, 6.0);
			scaleStep++;
			break;
		case 5:
			if (!Robot.drive.isIdle() || !Robot.grab.isIdle())
				break;
			Robot.grab.autoRelease();
			scaleStep++;
			break;
		case 6:
			if (!Robot.grab.isIdle())
				break;
			Robot.drive.autoDrive(0.5, 3, -6.0);
			scaleStep++;
			break;
		case 7:
			if (!Robot.drive.isIdle() || !Robot.elev.isIdle())
				break;
			Robot.elev.moveTo(0);
			Robot.drive.autoTurn(-110, 4);
			scaleStep++;
			break;
		case 8:
			break;

		}
	}

	int switchStep2 = 1;

	public void sideSwitch() {
		switch(switchStep2) {
		
		case 1:
			if(!Robot.drive.isIdle())
				break;
			Robot.drive.autoDrive(0.5, 3, 13.0);
			switchStep2++;
			break;
		case 2:
			int turnDir = (Robot.chooserPos.getSelected() == Robot.posRight) ? -1 : 1;
			if(!Robot.drive.isIdle())
				break;
			Robot.drive.autoTurn(90 * turnDir, 3);
			Robot.arm.moveTo(0);
			switchStep2++;
			break;
		case 3:
			if(!Robot.drive.isIdle() || !Robot.arm.isIdle())
				break;
			Robot.drive.autoDrive(0.4, 2, 3.8);
			switchStep2++;
			break;
		case 4:
			if(!Robot.drive.isIdle())
				break;
			Robot.grab.autoRelease();
			switchStep2++;
			break;
		case 5:
			break;
			}
		}
}
