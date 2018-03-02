package org.usfirst.frc.team1002.robot;

import edu.wpi.first.wpilibj.DriverStation;

public class Autonomous {

	private static char posIndex;
	private static int targIndex;
	private static boolean altIndex;
	private static String FMSString; // three character string from FMS.
	private static char sideSwitch; // side switch is on.
	private static char sideScale; // side scale is on.
	private static char handed; // L or R, which direction robot should turn first. Used to reduce repeated
	private static final double SPEED = 0.3;		// code.
	private static boolean switchNear;
	private static boolean scaleNear;

	//private static MarioDrive drive = new MarioDrive();
	//private static EightBitElev elev = new EightBitElev();
	//private static RobotArm arm = new RobotArm();
	//private static Grabber grab = new Grabber();

	public void init() {
		switch (Robot.posSelected) {
		case (Robot.posLeft):
			posIndex = 'L';
			break;
		case (Robot.posCenter):
			posIndex = 'C';
			break;
		case (Robot.posRight):
			posIndex = 'R';
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
		sideSwitch = FMSString.charAt(0);
		sideScale = FMSString.charAt(1);
		switch (targIndex) {
		case (1):
			if (posIndex == sideSwitch) {
				if (posIndex == 'L') {
					handed = 'R';
				} else {
					handed = 'L';
				}
			} else {
				if (posIndex == 'R') {
					handed = 'L';
				} else {
					handed = 'R';
				}
			}
			break;
		case (2):
			if (posIndex == sideScale) {
				if (posIndex == 'L') {
					handed = 'R';
				} else {
					handed = 'L';
				}
			} else {
				if (posIndex == 'R') {
					handed = 'L';
				} else {
					handed = 'R';
				}
			}
			break;
		}
	}

	public static void run() {
		Robot.drive.autoDrive(.5, .5, 6);

	}

	public void getAutoFromSelections() {
		switch (posIndex) {
		case 'L':
			switch (sideSwitch) {
			case 'L':
				switchNear = true;
			case 'R':
				switchNear = false;
			}
		case 'R':
			switch(sideSwitch) {
			case 'L':
				switchNear = false;
			case 'R':
				switchNear = true;
			}
		}
	}
	int step = 1;
	public void sameSideScale() {

		switch(step){
			case 1: 
				Robot.drive.autoDrive(SPEED, 10.0, 6.0);
				step++;
			case 2: 
				if(!Robot.drive.isIdle()) {
					break;
				}
				Robot.drive.autoTurn(-90, 7);
				step++;
			case 3: 
				if(!Robot.drive.isIdle()) {
					break;
				}
				Robot.drive.autoDrive(SPEED, 8.0, 2.0);
				step++;
			case 4:
				if(!Robot.drive.isIdle()) {
					break;
				}
				Robot.drive.autoTurn(0, 5);
				step++;
			case 5: 
				if(!Robot.drive.isIdle()) {
					break;
				}
				Robot.drive.autoDrive(SPEED, 10.0, 10);
				step++;
			case 6:
				if(!Robot.elev.isIdle()) {
					break;
				}
				//Robot.elev.moveTo(RobotData.elevMaxHeightUnits);
				step++;
			case 7:
				if(!Robot.arm.isIdle()) {
					break;
				}
				//Robot.arm.moveTo(20);
				step++;
			case 8:
				if(!Robot.drive.isIdle()) {
					break;
				}
				Robot.drive.autoTurn(20, 5);
				step++;
			case 9:
				if(!Robot.drive.isIdle()) {
					break;
				}
				Robot.drive.autoDrive(SPEED, 7, 5.5);
				step++;
				break;
			case 10:
				break;
				//dodododododododododododododododooooo
		}
	}
}




