package org.usfirst.frc.team1002.robot;

import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.FeedbackDevice;
import com.ctre.phoenix.motorcontrol.StatusFrameEnhanced;
import com.ctre.phoenix.motorcontrol.can.TalonSRX;

import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

public class EightBitElev {
	TalonSRX stageOneTalon;
	TalonSRX stageTwoTalon;

	public EightBitElev() {
		stageOneTalon = new TalonSRX(RobotData.elevTalonPort);
		stageTwoTalon = new TalonSRX(6);

	}

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

	public void talonConfig(TalonSRX thisTalon) {
		/* first choose the sensor */
		thisTalon.configSelectedFeedbackSensor(FeedbackDevice.CTRE_MagEncoder_Relative, kPIDLoopIdx, kTimeoutMs);
		thisTalon.setSensorPhase(true);
		thisTalon.setInverted(false);

		/* Set relevant frame periods to be at least as fast as periodic rate */
		thisTalon.setStatusFramePeriod(StatusFrameEnhanced.Status_13_Base_PIDF0, 10, kTimeoutMs);
		thisTalon.setStatusFramePeriod(StatusFrameEnhanced.Status_10_MotionMagic, 10, kTimeoutMs);

		/* set the peak and nominal outputs */
		thisTalon.configNominalOutputForward(0, kTimeoutMs);
		thisTalon.configNominalOutputReverse(0, kTimeoutMs);
		thisTalon.configPeakOutputForward(1, kTimeoutMs);
		thisTalon.configPeakOutputReverse(-1, kTimeoutMs);

		/* set closed loop gains in slot0 - see documentation */
		thisTalon.selectProfileSlot(kSlotIdx, kPIDLoopIdx);
		thisTalon.config_kF(0, 0.287, kTimeoutMs);
		thisTalon.config_kP(0, 0.4, kTimeoutMs);
		thisTalon.config_kI(0, 0, kTimeoutMs);
		thisTalon.config_kD(0, 0, kTimeoutMs);
		/* set acceleration and vcruise velocity - see documentation */
		thisTalon.configMotionCruiseVelocity(15000, kTimeoutMs);
		thisTalon.configMotionAcceleration(6000, kTimeoutMs);
		/* zero the sensor */
		thisTalon.setSelectedSensorPosition(0, kPIDLoopIdx, kTimeoutMs);

		thisTalon.configMotionCruiseVelocity((int) RobotData.elevCruiseVel, kTimeoutMs);
		thisTalon.configMotionAcceleration((int) RobotData.elevCruiseAccel, kTimeoutMs);

	}

	public void displayTalonParms(TalonSRX thisTalon) {
		boolean InMotionMagic = false;
		/* smart dash plots */
		SmartDashboard.putNumber("Elevator SensorVel", thisTalon.getSelectedSensorVelocity(kPIDLoopIdx));
		SmartDashboard.putNumber("Eevator SensorPos", thisTalon.getSelectedSensorPosition(kPIDLoopIdx));

		SmartDashboard.putNumber("Elevator MotorOutputPercent", thisTalon.getMotorOutputPercent());

		SmartDashboard.putNumber("Elevator ClosedLoopError", thisTalon.getClosedLoopError(kPIDLoopIdx));
		SmartDashboard.putNumber("Elevator ClosedLoopTarget", thisTalon.getClosedLoopError(kPIDLoopIdx));

		SmartDashboard.putNumber("Elevator CruiseVelocity", 300);
		SmartDashboard.putNumber("Elevator CruiseAcceleration", 300);

		SmartDashboard.putNumber("Time", Timer.getFPGATimestamp());

		SmartDashboard.putNumber("Elevator Remaining Distance", mttremainder / clicksPerUnit);
		SmartDashboard.putNumber("Elevator Talon Current", thisTalon.getOutputCurrent());
		/* check if we are motion-magic-ing */
		if (thisTalon.getControlMode() == ControlMode.MotionMagic) {
			SmartDashboard.putString("ControllerStatus", "MotionMagicMode");
			InMotionMagic = true;
		} else {
			SmartDashboard.putString("COntrollerStatus", "Non-MM");
			InMotionMagic = false;
		}
		if (InMotionMagic) {
			/* print the Active Trajectory Point Motion Magic is servoing towards */
			SmartDashboard.putNumber("Elevator ActTrajVelocity", thisTalon.getActiveTrajectoryVelocity());
			SmartDashboard.putNumber("Elevator ActTrajPosition", thisTalon.getActiveTrajectoryPosition());
			SmartDashboard.putNumber("Elevator ActTrajHeading", thisTalon.getActiveTrajectoryHeading());
		}
		currentPosition = thisTalon.getSelectedSensorPosition(kPIDLoopIdx);
		SmartDashboard.putNumber("currentPosition", currentPosition / clicksPerUnit);
		mttremainder = targetPosition - currentPosition;

		talonMax = Math.max(talonMax, thisTalon.getOutputCurrent());
		SmartDashboard.putNumber("Elevator Talon Max Current", talonMax);
		double elevcvMax = Math.max(cvMax, thisTalon.getSelectedSensorVelocity(kPIDLoopIdx));
		SmartDashboard.putNumber("Elevator Max Velocity", elevcvMax);

		SmartDashboard.putNumber("DesiredPosition", RobotData.elevDesiredPosition);
		SmartDashboard.putNumber("targetSensorPosition", targetPosition);

		double Abtn_cvMax = Math.max(cvMax, thisTalon.getSelectedSensorVelocity(kPIDLoopIdx));
		SmartDashboard.putNumber("Max Velocity A Button", Abtn_cvMax);
		Timer.delay(0.2);

	}

	double talonMax = 0.0;
	double cvMax = 0.0;
	double mttremainder = 0;
	double targetPosition;
	double currentPosition = 0;

	public void moveElevatorTo(double position) {
		targetPosition = position * clicksPerUnit;
		double stageOneMax = 30.0 * clicksPerUnit;
		/* Motion Magic - 4096 ticks/rev * 10 Rotations in either direction */
		if (position <= 30) {
			stageOneTalon.set(ControlMode.MotionMagic, targetPosition);
		} else {
			stageOneTalon.set(ControlMode.MotionMagic, stageOneMax);
			stageTwoTalon.set(ControlMode.MotionMagic, (targetPosition - stageOneMax));
		}
	}

	public void Init() {
		talonConfig(stageOneTalon);
		talonConfig(stageTwoTalon);
	}

	public void perform() {

	}

}
