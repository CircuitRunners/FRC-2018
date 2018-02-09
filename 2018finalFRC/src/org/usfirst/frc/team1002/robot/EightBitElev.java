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
		stageOneTalon = new TalonSRX(RobotData.elevS1TalonPort);
		stageTwoTalon = new TalonSRX(RobotData.elevS2TalonPort);

	}

	//help
	
	public void talonConfig(TalonSRX thisTalon) {
		/* first choose the sensor */
		thisTalon.configSelectedFeedbackSensor(FeedbackDevice.CTRE_MagEncoder_Relative, RobotData.elevPIDLoopIdx, RobotData.elevTimeoutMs);
		thisTalon.setSensorPhase(true);
		thisTalon.setInverted(false);

		/* Set relevant frame periods to be at least as fast as periodic rate */
		thisTalon.setStatusFramePeriod(StatusFrameEnhanced.Status_13_Base_PIDF0, 10, RobotData.elevTimeoutMs);
		thisTalon.setStatusFramePeriod(StatusFrameEnhanced.Status_10_MotionMagic, 10, RobotData.elevTimeoutMs);

		/* set the peak and nominal outputs */
		thisTalon.configNominalOutputForward(0, RobotData.elevTimeoutMs);
		thisTalon.configNominalOutputReverse(0, RobotData.elevTimeoutMs);
		thisTalon.configPeakOutputForward(1, RobotData.elevTimeoutMs);
		thisTalon.configPeakOutputReverse(-1, RobotData.elevTimeoutMs);

		/* set closed loop gains in slot0 - see documentation */
		thisTalon.selectProfileSlot(RobotData.elevSlotIdx, RobotData.elevPIDLoopIdx);
		thisTalon.config_kF(0, 0.287, RobotData.elevTimeoutMs);
		thisTalon.config_kP(0, 0.4, RobotData.elevTimeoutMs);
		thisTalon.config_kI(0, 0, RobotData.elevTimeoutMs);
		thisTalon.config_kD(0, 0, RobotData.elevTimeoutMs);
		/* set acceleration and vcruise velocity - see documentation */
		thisTalon.configMotionCruiseVelocity(15000, RobotData.elevTimeoutMs);
		thisTalon.configMotionAcceleration(6000, RobotData.elevTimeoutMs);
		/* zero the sensor */
		thisTalon.setSelectedSensorPosition(0, RobotData.elevPIDLoopIdx, RobotData.elevTimeoutMs);

		thisTalon.configMotionCruiseVelocity((int) RobotData.elevCruiseVel, RobotData.elevTimeoutMs);
		thisTalon.configMotionAcceleration((int) RobotData.elevCruiseAccel, RobotData.elevTimeoutMs);

	}

	double talonMax = 0.0;
	double cvMax = 0.0;
	double mttremainder = 0;
	double currentPosition = 0;

	public void display() {
		/* smart dash plots */
		SmartDashboard.putNumber("Elevator SensorVel", stageOneTalon.getSelectedSensorVelocity(RobotData.elevPIDLoopIdx));
		SmartDashboard.putNumber("Elevator SensorPos", stageOneTalon.getSelectedSensorPosition(RobotData.elevPIDLoopIdx));

		SmartDashboard.putNumber("Elevator MotorOutputPercent", stageOneTalon.getMotorOutputPercent());

		SmartDashboard.putNumber("Elevator ClosedLoopError", stageOneTalon.getClosedLoopError(RobotData.elevPIDLoopIdx));
		SmartDashboard.putNumber("Elevator ClosedLoopTarget", stageOneTalon.getClosedLoopError(RobotData.elevPIDLoopIdx));

		SmartDashboard.putNumber("Elevator CruiseVelocity", 300);
		SmartDashboard.putNumber("Elevator CruiseAcceleration", 300);

		SmartDashboard.putNumber("Time", Timer.getFPGATimestamp());

		SmartDashboard.putNumber("Elevator Remaining Distance", mttremainder / RobotData.elevClicksPerUnit);
		SmartDashboard.putNumber("Elevator Talon Current", stageOneTalon.getOutputCurrent());

		/* print the Active Trajectory Point Motion Magic is going towards */
		SmartDashboard.putNumber("Elevator ActTrajVelocity", stageOneTalon.getActiveTrajectoryVelocity());
		SmartDashboard.putNumber("Elevator ActTrajPosition", stageOneTalon.getActiveTrajectoryPosition());
		SmartDashboard.putNumber("Elevator ActTrajHeading", stageOneTalon.getActiveTrajectoryHeading());

		currentPosition = stageOneTalon.getSelectedSensorPosition(RobotData.elevPIDLoopIdx);
		SmartDashboard.putNumber("currentPosition", currentPosition / RobotData.elevClicksPerUnit);
		mttremainder = targetPosition - currentPosition;

		talonMax = Math.max(talonMax, stageOneTalon.getOutputCurrent());
		SmartDashboard.putNumber("Elevator Talon Max Current", talonMax);
		double elevcvMax = Math.max(cvMax, stageOneTalon.getSelectedSensorVelocity(RobotData.elevPIDLoopIdx));
		SmartDashboard.putNumber("Elevator Max Velocity", elevcvMax);

		SmartDashboard.putNumber("DesiredPosition", RobotData.elevDesiredPosition);
		SmartDashboard.putNumber("targetSensorPosition", targetPosition);

		double Abtn_cvMax = Math.max(cvMax, stageOneTalon.getSelectedSensorVelocity(RobotData.elevPIDLoopIdx));
		SmartDashboard.putNumber("Max Velocity A Button", Abtn_cvMax);

	}

	double targetPosition;

	public void moveElevatorTo(double position) {
		targetPosition = position * RobotData.elevClicksPerUnit;
		double stageOneMax = 30.0 * RobotData.elevClicksPerUnit;
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

}
