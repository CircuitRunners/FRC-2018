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

	TalonSRX elevTalon = new TalonSRX(3);

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
		elevTalon.configSelectedFeedbackSensor(FeedbackDevice.CTRE_MagEncoder_Relative, kPIDLoopIdx, kTimeoutMs);
		elevTalon.setSensorPhase(true);
		elevTalon.setInverted(false);

		/* Set relevant frame periods to be at least as fast as periodic rate */
		elevTalon.setStatusFramePeriod(StatusFrameEnhanced.Status_13_Base_PIDF0, 10, kTimeoutMs);
		elevTalon.setStatusFramePeriod(StatusFrameEnhanced.Status_10_MotionMagic, 10, kTimeoutMs);

		/* set the peak and nominal outputs */
		elevTalon.configNominalOutputForward(0, kTimeoutMs);
		elevTalon.configNominalOutputReverse(0, kTimeoutMs);
		elevTalon.configPeakOutputForward(1, kTimeoutMs);
		elevTalon.configPeakOutputReverse(-1, kTimeoutMs);

		/* set closed loop gains in slot0 - see documentation */
		elevTalon.selectProfileSlot(kSlotIdx, kPIDLoopIdx);
		elevTalon.config_kF(0, 0.287, kTimeoutMs);
		elevTalon.config_kP(0, 0.4, kTimeoutMs);
		elevTalon.config_kI(0, 0, kTimeoutMs);
		elevTalon.config_kD(0, 0, kTimeoutMs);
		/* set acceleration and vcruise velocity - see documentation */
		elevTalon.configMotionCruiseVelocity(15000, kTimeoutMs);
		elevTalon.configMotionAcceleration(6000, kTimeoutMs);
		/* zero the sensor */
		elevTalon.setSelectedSensorPosition(0, kPIDLoopIdx, kTimeoutMs);

		SmartDashboard.putNumber("CruiseVelocity", 300);
		SmartDashboard.putNumber("CruiseAcceleration", 300);
	}

	public void displayTalonParms() {
		boolean InMotionMagic = false;
		/* smart dash plots */
		SmartDashboard.putNumber("SensorVel", elevTalon.getSelectedSensorVelocity(kPIDLoopIdx));
		SmartDashboard.putNumber("SensorPos", elevTalon.getSelectedSensorPosition(kPIDLoopIdx));
		SmartDashboard.putNumber("MotorOutputPercent", elevTalon.getMotorOutputPercent());
		SmartDashboard.putNumber("ClosedLoopError", elevTalon.getClosedLoopError(kPIDLoopIdx));
		SmartDashboard.putNumber("ClosedLoopTarget", elevTalon.getClosedLoopError(kPIDLoopIdx));

		/* check if we are motion-magic-ing */
		if (elevTalon.getControlMode() == ControlMode.MotionMagic) {
			SmartDashboard.putString("COntrollerStatus", "MotionMagicMode");
			InMotionMagic = true;
		} else {
			SmartDashboard.putString("COntrollerStatus", "Non-MM");
			InMotionMagic = false;
		}
		if (InMotionMagic) {
			/* print the Active Trajectory Point Motion Magic is servoing towards */
			SmartDashboard.putNumber("ActTrajVelocity", elevTalon.getActiveTrajectoryVelocity());
			SmartDashboard.putNumber("ActTrajPosition", elevTalon.getActiveTrajectoryPosition());
			SmartDashboard.putNumber("ActTrajHeading", elevTalon.getActiveTrajectoryHeading());
		}
	}

	public void talonConfig() {
		double cv = 0, ca = 0;
		cv = SmartDashboard.getNumber("CruiseVelocity", 1000.0);
		ca = SmartDashboard.getNumber("CruiseAcceleration", 100.0);
		/* set acceleration and vcruise velocity - see documentation */
		elevTalon.configMotionCruiseVelocity((int) cv, kTimeoutMs);
		elevTalon.configMotionAcceleration((int) ca, kTimeoutMs);

	}

	double talonMax = 0.0;
	double cvMax = 0.0;

	void moveTalonTo(double position) {
		double targetPosition = position * clicksPerUnit;
		double currentPosition = 0;
		double remainder = 0;
		talonConfig();
		/* Motion Magic - 4096 ticks/rev * 10 Rotations in either direction */
		elevTalon.set(ControlMode.MotionMagic, targetPosition);
		SmartDashboard.putNumber("DesiredPosition", position);
		SmartDashboard.putNumber("targetSensorPosition", targetPosition);
		int loop = 0;
		// while (Math.abs(
		// (currentPosition = talonController.getSelectedSensorPosition(kPIDLoopIdx)) -
		// targetPosition) > 100) {

		while (loop++ < 0) {
			currentPosition = elevTalon.getSelectedSensorPosition(kPIDLoopIdx);
			SmartDashboard.putNumber("currentPosition", currentPosition / clicksPerUnit);
			remainder = targetPosition - currentPosition;

			SmartDashboard.putNumber("Remaining Distance", remainder / clicksPerUnit);
			SmartDashboard.putNumber("Talon Current", elevTalon.getOutputCurrent());
			talonMax = Math.max(talonMax, elevTalon.getOutputCurrent());
			SmartDashboard.putNumber("Talon Max Current", talonMax);
			cvMax = Math.max(cvMax, elevTalon.getSelectedSensorVelocity(kPIDLoopIdx));
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

	public void myMethod() {
		double desiredPosition = 0;

		SmartDashboard.putNumber("Time", Timer.getFPGATimestamp());
		/* if they hold the bumper, put it in motion mode */
		if (Robot.driver.getBumper(GenericHID.Hand.kLeft)) {
			/* Percent output mode */
			double leftYStick = Robot.driver.getY(GenericHID.Hand.kLeft);
			elevTalon.set(ControlMode.PercentOutput, leftYStick);
			displayTalonParms();
			SmartDashboard.putString("ControllerStatus", "PercentageOutput");
			SmartDashboard.putNumber("ControllerPercentage", leftYStick);
		} else {
			if (Robot.driver.getAButton()) {
				int LOOP = 0;
				double cvMax = 0;

				elevTalon.set(ControlMode.PercentOutput, 0.4);
				displayTalonParms();
				SmartDashboard.putString("ControllerStatus", "PercentageOutput");
				SmartDashboard.putNumber("ControllerPercentage", 0.4);

				while (LOOP++ < 10) {
					cvMax = Math.max(cvMax, elevTalon.getSelectedSensorVelocity(kPIDLoopIdx));
					SmartDashboard.putNumber("Max Velocity A Button", cvMax);
					Timer.delay(0.2);
				}
				elevTalon.set(ControlMode.PercentOutput, 0);
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
	public void displayElev() {
		
	}
}
