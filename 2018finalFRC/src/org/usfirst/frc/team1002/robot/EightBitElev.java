package org.usfirst.frc.team1002.robot;

import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.FeedbackDevice;
import com.ctre.phoenix.motorcontrol.StatusFrameEnhanced;
import com.ctre.phoenix.motorcontrol.can.TalonSRX;

import edu.wpi.first.wpilibj.GenericHID;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

public class EightBitElev {

	public EightBitElev() {

	}

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

	public static final double clicksPerUnit = 895.99;

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
		talonController.config_kF(0, 0.287, kTimeoutMs);
		talonController.config_kP(0, 0.4, kTimeoutMs);
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

	double talonMax = 0.0;
	double cvMax = 0.0;

	void moveTalonTo(double position) {
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

		while (loop++ < 0) {
			currentPosition = talonController.getSelectedSensorPosition(kPIDLoopIdx);
			SmartDashboard.putNumber("currentPosition", currentPosition / clicksPerUnit);
			remainder = targetPosition - currentPosition;

			SmartDashboard.putNumber("Remaining Distance", remainder / clicksPerUnit);
			SmartDashboard.putNumber("Talon Current", talonController.getOutputCurrent());
			talonMax = Math.max(talonMax, talonController.getOutputCurrent());
			SmartDashboard.putNumber("Talon Max Current", talonMax);
			cvMax = Math.max(cvMax, talonController.getSelectedSensorVelocity(kPIDLoopIdx));
			SmartDashboard.putNumber("Max Velocity", cvMax);

			if (remainder / clicksPerUnit < 0.7)
				break;
			displayTalonParms();
			if (Math.abs(remainder) < 200)
				break;
			Timer.delay(0.1); // wait for a motor update time
		}

	}

	public void Init() {
		SmartDashboard.putNumber("X Button Position", 0.0);
		SmartDashboard.putNumber("Y Button Position", 15.0);
		SmartDashboard.putNumber("B Button Position", 30.0);
		talonInit();

	}

	/**
	 * Runs the motors with arcade steering.
	 */
	public void myMethod() {
		double desiredPosition = 0;
		/* This code just loops, moving the system to a position */
		// while(isOperatorControl() && isEnabled()) {
		// SmartDashboard.putNumber("Time", Timer.getFPGATimestamp());
		/* if they hold the bumper, put it in motion mode */
		if (Robot.driver.getBumper(GenericHID.Hand.kLeft)) {
			/* Percent output mode */
			double leftYStick = Robot.driver.getY(GenericHID.Hand.kLeft);
			talonController.set(ControlMode.PercentOutput, leftYStick);
			displayTalonParms();
			SmartDashboard.putString("ControllerStatus", "PercentageOutput");
			SmartDashboard.putNumber("ControllerPercentage", leftYStick);
		} else {
			if (Robot.driver.getAButton()) {
				int LOOP = 0;
				double cvMax = 0;

				talonController.set(ControlMode.PercentOutput, 0.4);
				displayTalonParms();
				SmartDashboard.putString("ControllerStatus", "PercentageOutput");
				SmartDashboard.putNumber("ControllerPercentage", 0.4);

				while (LOOP++ < 10) {
					cvMax = Math.max(cvMax, talonController.getSelectedSensorVelocity(kPIDLoopIdx));
					SmartDashboard.putNumber("Max Velocity A Button", cvMax);
					Timer.delay(0.2);
				}
				talonController.set(ControlMode.PercentOutput, 0);
			} else {
				SmartDashboard.putString("ControllerStatus", "Button Input");
				if (Robot.driver.getXButton()) {
					desiredPosition = SmartDashboard.getNumber("X Button Position", 0.0);
				} else if (Robot.driver.getYButton()) {
					desiredPosition = SmartDashboard.getNumber("Y Button Position", 0.0);
				} else if (Robot.driver.getBButton()) {
					desiredPosition = SmartDashboard.getNumber("B Button Position", 0.0);
				} else {
					double pov = Robot.driver.getPOV(0);
					if (pov != -1) {

						if (Robot.driver.getPOV(0) > 270 || Robot.driver.getPOV(0) < 90) {
							desiredPosition += 0.5;
						} else {
							desiredPosition -= 0.5;
						}
					}
				}
				moveTalonTo(desiredPosition);
			}
			Timer.delay(0.02); // wait for a bit

		}
	}
}
