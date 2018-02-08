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

	public void talonConfig() {
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

		elevTalon.configMotionCruiseVelocity((int) RobotData.elevCruiseVel, kTimeoutMs);
		elevTalon.configMotionAcceleration((int) RobotData.elevCruiseAccel, kTimeoutMs);

	}

	public void displayTalonParms() {
		boolean InMotionMagic = false;
		/* smart dash plots */
		SmartDashboard.putNumber("Elevator SensorVel", elevTalon.getSelectedSensorVelocity(kPIDLoopIdx));
		SmartDashboard.putNumber("Eevator SensorPos", elevTalon.getSelectedSensorPosition(kPIDLoopIdx));

		SmartDashboard.putNumber("Elevator MotorOutputPercent", elevTalon.getMotorOutputPercent());

		SmartDashboard.putNumber("Elevator ClosedLoopError", elevTalon.getClosedLoopError(kPIDLoopIdx));
		SmartDashboard.putNumber("Elevator ClosedLoopTarget", elevTalon.getClosedLoopError(kPIDLoopIdx));

		SmartDashboard.putNumber("Elevator CruiseVelocity", 300);
		SmartDashboard.putNumber("Elevator CruiseAcceleration", 300);

		SmartDashboard.putNumber("Time", Timer.getFPGATimestamp());

		SmartDashboard.putNumber("Elevator Remaining Distance", mttremainder / clicksPerUnit);
		SmartDashboard.putNumber("Elevator Talon Current", elevTalon.getOutputCurrent());
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
			SmartDashboard.putNumber("Elevator ActTrajVelocity", elevTalon.getActiveTrajectoryVelocity());
			SmartDashboard.putNumber("Elevator ActTrajPosition", elevTalon.getActiveTrajectoryPosition());
			SmartDashboard.putNumber("Elevator ActTrajHeading", elevTalon.getActiveTrajectoryHeading());
		}
		currentPosition = elevTalon.getSelectedSensorPosition(kPIDLoopIdx);
		SmartDashboard.putNumber("currentPosition", currentPosition / clicksPerUnit);
		mttremainder = targetPosition - currentPosition;

		talonMax = Math.max(talonMax, elevTalon.getOutputCurrent());
		SmartDashboard.putNumber("Elevator Talon Max Current", talonMax);
		double elevcvMax = Math.max(cvMax, elevTalon.getSelectedSensorVelocity(kPIDLoopIdx));
		SmartDashboard.putNumber("Elevator Max Velocity", elevcvMax);

		SmartDashboard.putNumber("DesiredPosition", RobotData.elevDesiredPosition);
		SmartDashboard.putNumber("targetSensorPosition", targetPosition);
		
		double Abtn_cvMax = Math.max(cvMax, elevTalon.getSelectedSensorVelocity(kPIDLoopIdx));
		SmartDashboard.putNumber("Max Velocity A Button", Abtn_cvMax);
		Timer.delay(0.2);

	}

	double talonMax = 0.0;
	double cvMax = 0.0;
	double mttremainder = 0;
	double targetPosition;
	double currentPosition = 0;

	void moveTalonTo(double position) {
		RobotData.elevDesiredPosition = position;
		targetPosition = position * clicksPerUnit;
		/* Motion Magic - 4096 ticks/rev * 10 Rotations in either direction */
		elevTalon.set(ControlMode.MotionMagic, targetPosition);

	}

	public void Init() {
		talonConfig();
	}

	public void perform() {
		double desiredPosition = 0;

		/* if they hold the bumper, put it in motion mode */

		if (Robot.driver.getAButton()) {
			int LOOP = 0;
			double cvMax = 0;

			elevTalon.set(ControlMode.PercentOutput, 0.4);
			displayTalonParms();
			SmartDashboard.putString("ControllerStatus", "PercentageOutput");
			SmartDashboard.putNumber("ControllerPercentage", 0.4);

			while (LOOP++ < 10) {

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

	public void displayElev() {

	}
}
