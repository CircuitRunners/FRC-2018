package org.usfirst.frc.team1002.robot;

import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.FeedbackDevice;
import com.ctre.phoenix.motorcontrol.StatusFrameEnhanced;
import com.ctre.phoenix.motorcontrol.can.TalonSRX;

import edu.wpi.first.wpilibj.GenericHID;
import edu.wpi.first.wpilibj.SampleRobot;
import edu.wpi.first.wpilibj.XboxController;
import edu.wpi.first.wpilibj.Timer;
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
@SuppressWarnings("deprecation")
public class Robot extends SampleRobot {

	final String defaultAuto = "Default";
	final String customAuto = "My Auto";
	XboxController driverCntl = new XboxController(0);
	XboxController operatorCntl = new XboxController(1);
	SendableChooser<String> autoChoice1 = new SendableChooser<String>();

	TalonSRX talonController = new TalonSRX(3);
	StringBuilder _sb = new StringBuilder();

	/**
	 * Which PID slot to pull gains from. Starting 2018, you can choose from 0,1,2
	 * or 3. Only the first two (0,1) are visible in web-based configuration.
	 */
	public static final int kSlotIdx = 0;

	/*
	 * Talon SRX/ Victor SPX will supported multiple (cascaded) PID loops. For now
	 * we just want the primary one.
	 */
	public static final int kPIDLoopIdx = 0;

	/*
	 * set to zero to skip waiting for confirmation, set to nonzero to wait and
	 * report to DS if action fails.
	 */
	public static final int kTimeoutMs = 10;

	public static final double clicksPerUnit = 1024;

	public void talonInit() {
		/* first choose the sensor */
		talonController.configSelectedFeedbackSensor(FeedbackDevice.CTRE_MagEncoder_Relative, kPIDLoopIdx, kTimeoutMs);
		talonController.setSensorPhase(true);
		talonController.setInverted(false);

		/* Set relevant frame periods to be at least as fast as periodic rate */
		talonController.setStatusFramePeriod(StatusFrameEnhanced.Status_13_Base_PIDF0, 10, kTimeoutMs);
		talonController.setStatusFramePeriod(StatusFrameEnhanced.Status_10_MotionMagic, 10, kTimeoutMs);

		/* set the peak and nominal outputs */
		talonController.configNominalOutputForward(0, kTimeoutMs);
		talonController.configNominalOutputReverse(0, kTimeoutMs);
		talonController.configPeakOutputForward(1, kTimeoutMs);
		talonController.configPeakOutputReverse(-1, kTimeoutMs);

		/* set closed loop gains in slot0 - see documentation */
		talonController.selectProfileSlot(kSlotIdx, kPIDLoopIdx);
		talonController.config_kF(0, 0.2, kTimeoutMs);
		talonController.config_kP(0, 0.2, kTimeoutMs);
		talonController.config_kI(0, 0, kTimeoutMs);
		talonController.config_kD(0, 0, kTimeoutMs);
		/* set acceleration and vcruise velocity - see documentation */
		talonController.configMotionCruiseVelocity(15000, kTimeoutMs);
		talonController.configMotionAcceleration(6000, kTimeoutMs);
		/* zero the sensor */
		talonController.setSelectedSensorPosition(0, kPIDLoopIdx, kTimeoutMs);

		SmartDashboard.putNumber("CruiseVelocity", 300);
		SmartDashboard.putNumber("CruiseAcceleration", 300);
	}

	public void displayTalonParms() {
		boolean InMotionMagic = false;
		/* smart dash plots */
		SmartDashboard.putNumber("SensorVel", talonController.getSelectedSensorVelocity(kPIDLoopIdx));
		SmartDashboard.putNumber("SensorPos", talonController.getSelectedSensorPosition(kPIDLoopIdx));
		SmartDashboard.putNumber("MotorOutputPercent", talonController.getMotorOutputPercent());
		SmartDashboard.putNumber("ClosedLoopError", talonController.getClosedLoopError(kPIDLoopIdx));
		SmartDashboard.putNumber("ClosedLoopTarget", talonController.getClosedLoopError(kPIDLoopIdx));

		/* check if we are motion-magic-ing */
		if (talonController.getControlMode() == ControlMode.MotionMagic) {
			SmartDashboard.putString("COntrollerStatus", "MotionMagicMode");
			InMotionMagic = true;
		} else {
			SmartDashboard.putString("COntrollerStatus", "Non-MM");
			InMotionMagic = false;
		}
		if (InMotionMagic) {
			/* print the Active Trajectory Point Motion Magic is servoing towards */
			SmartDashboard.putNumber("ActTrajVelocity", talonController.getActiveTrajectoryVelocity());
			SmartDashboard.putNumber("ActTrajPosition", talonController.getActiveTrajectoryPosition());
			SmartDashboard.putNumber("ActTrajHeading", talonController.getActiveTrajectoryHeading());
		}
	}

	public void talonConfig() {
		double cv = 0, ca = 0;
		cv = SmartDashboard.getNumber("CruiseVelocity", 1000.0);
		ca = SmartDashboard.getNumber("CruiseAcceleration", 100.0);
		/* set acceleration and vcruise velocity - see documentation */
		talonController.configMotionCruiseVelocity((int) cv, kTimeoutMs);
		talonController.configMotionAcceleration((int) ca, kTimeoutMs);

	}

	public void moveTalonTo(double position) {
		double targetPosition = position * clicksPerUnit;
		double currentPosition = 0;
		double remainder = 0;
		talonConfig();
		/* Motion Magic - 4096 ticks/rev * 10 Rotations in either direction */
		talonController.set(ControlMode.MotionMagic, targetPosition);
		SmartDashboard.putNumber("DesiredPosition", position);
		SmartDashboard.putNumber("targetSensorPosition", targetPosition);
		int loop = 0;
		// while (Math.abs(
		// (currentPosition = talonController.getSelectedSensorPosition(kPIDLoopIdx)) -
		// targetPosition) > 100) {
		while (loop++ < 100) {
			currentPosition = talonController.getSelectedSensorPosition(kPIDLoopIdx);
			SmartDashboard.putNumber("currentPosition", currentPosition / clicksPerUnit);
			remainder = targetPosition - currentPosition;
			SmartDashboard.putNumber("Remaining Distance", remainder / clicksPerUnit);
			displayTalonParms();
			if (Math.abs(remainder) < 200) break;
			Timer.delay(0.1); // wait for a motor update time
		}

	}

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
		talonInit();

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
	public void autonomous() {
	}

	/**
	 * Runs the motors with arcade steering.
	 */
	@Override
	public void operatorControl() {
		double desiredPosition = 0;
		/* This code just loops, moving the system to a position */
		while (isOperatorControl() && isEnabled()) {
			/* if they hold the bumper, put it in motion mode */
			if (driverCntl.getBumper(GenericHID.Hand.kLeft)) {
				/* Percent output mode */
				double leftYStick = driverCntl.getY(GenericHID.Hand.kLeft);
				talonController.set(ControlMode.PercentOutput, leftYStick);
				displayTalonParms();
				SmartDashboard.putString("ControllerStatus", "PercentageOutput");
			} else {
				if (driverCntl.getXButton()) {
					desiredPosition = SmartDashboard.getNumber("X Button Position", 0.0);
				} else if (driverCntl.getYButton()) {
					desiredPosition = SmartDashboard.getNumber("Y Button Position", 0.0);
				} else if (driverCntl.getBButton()) {
					desiredPosition = SmartDashboard.getNumber("B Button Position", 0.0);
				}
				moveTalonTo(desiredPosition);
			}
			Timer.delay(0.02); // wait for a bit
		}
	}

	/**
	 * Runs during test mode
	 */
	@Override
	public void test() {
	}
}
