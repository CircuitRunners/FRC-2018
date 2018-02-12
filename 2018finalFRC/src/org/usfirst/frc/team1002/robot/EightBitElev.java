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

	// help

	public void talonConfig(TalonSRX thisTalon) {
		/* first choose the sensor */
		thisTalon.configSelectedFeedbackSensor(FeedbackDevice.CTRE_MagEncoder_Relative, RobotData.elevPIDLoopIdx,
				RobotData.elevTimeoutMs);
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

	public void display() {
		/* smart dash plots */
		SmartDashboard.putNumber("Elevator S1 SensorVel",
				stageOneTalon.getSelectedSensorVelocity(RobotData.elevPIDLoopIdx));
		SmartDashboard.putNumber("Elevator S1 SensorPos",
				stageOneTalon.getSelectedSensorPosition(RobotData.elevPIDLoopIdx));

		SmartDashboard.putNumber("Elevator S1 MotorOutputPercent", stageOneTalon.getMotorOutputPercent());

		SmartDashboard.putNumber("Elevator S1 ClosedLoopError",
				stageOneTalon.getClosedLoopError(RobotData.elevPIDLoopIdx));
		SmartDashboard.putNumber("Elevator S1 ClosedLoopTarget",
				stageOneTalon.getClosedLoopError(RobotData.elevPIDLoopIdx));

		SmartDashboard.putNumber("Elevator CruiseVelocity", RobotData.elevCruiseVel);
		SmartDashboard.putNumber("Elevator CruiseAcceleration", RobotData.elevCruiseAccel);

		SmartDashboard.putNumber("Time", Timer.getFPGATimestamp());

		SmartDashboard.putNumber("Elevator Remaining Distance", mtt1remainder / RobotData.elevClicksPerUnit);
		SmartDashboard.putNumber("Elevator S1 Talon Current", stageOneTalon.getOutputCurrent());

		/* print the Active Trajectory Point Motion Magic is going towards */
		SmartDashboard.putNumber("Elevator S1 ActTrajVelocity", stageOneTalon.getActiveTrajectoryVelocity());
		SmartDashboard.putNumber("Elevator S1 ActTrajPosition", stageOneTalon.getActiveTrajectoryPosition());
		SmartDashboard.putNumber("Elevator S1 ActTrajHeading", stageOneTalon.getActiveTrajectoryHeading());

		RobotData.elevS1Position = stageOneTalon.getSelectedSensorPosition(RobotData.elevPIDLoopIdx);
		SmartDashboard.putNumber("S1 Position", RobotData.elevS1Position / RobotData.elevClicksPerUnit);
		mtt1remainder = RobotData.elevS1PositionTarget - RobotData.elevS1Position;

		RobotData.elevS1OutputMax = Math.max(RobotData.elevS1OutputMax, stageOneTalon.getOutputCurrent());
		SmartDashboard.putNumber("Elevator Talon Max Current", RobotData.elevS1OutputMax);
		double elevcvMax = Math.max(cvMax, stageOneTalon.getSelectedSensorVelocity(RobotData.elevPIDLoopIdx));
		SmartDashboard.putNumber("Elevator Max Velocity", elevcvMax);

		SmartDashboard.putNumber("DesiredPosition", RobotData.elevPositionTarget);
		SmartDashboard.putNumber("targetSensorPosition", RobotData.elevPositionTarget);

		double Abtn_cvMax = Math.max(cvMax, stageOneTalon.getSelectedSensorVelocity(RobotData.elevPIDLoopIdx));
		SmartDashboard.putNumber("Max Velocity A Button", Abtn_cvMax);
		
		
		/* smart dash plots */
		SmartDashboard.putNumber("Elevator SensorVel",
				stageTwoTalon.getSelectedSensorVelocity(RobotData.elevPIDLoopIdx));
		SmartDashboard.putNumber("Elevator SensorPos",
				stageTwoTalon.getSelectedSensorPosition(RobotData.elevPIDLoopIdx));

		SmartDashboard.putNumber("Elevator MotorOutputPercent", stageTwoTalon.getMotorOutputPercent());

		SmartDashboard.putNumber("Elevator Talon Current", stageTwoTalon.getOutputCurrent());
		SmartDashboard.putNumber("Elevator S2 Remaining Distance", mtt2remainder / RobotData.elevClicksPerUnit);
		
		/* print the Active Trajectory Point Motion Magic is going towards */
		SmartDashboard.putNumber("Elevator ActTrajVelocity", stageTwoTalon.getActiveTrajectoryVelocity());
		SmartDashboard.putNumber("Elevator ActTrajPosition", stageTwoTalon.getActiveTrajectoryPosition());
		SmartDashboard.putNumber("Elevator ActTrajHeading", stageTwoTalon.getActiveTrajectoryHeading());

		RobotData.elevS2Position = stageOneTalon.getSelectedSensorPosition(RobotData.elevPIDLoopIdx);
		SmartDashboard.putNumber("currentPosition", RobotData.elevS2Position / RobotData.elevClicksPerUnit);
		mtt2remainder = RobotData.elevS2PositionTarget - RobotData.elevS2Position;

		RobotData.elevS2OutputMax = Math.max(RobotData.elevS2OutputMax, stageOneTalon.getOutputCurrent());
		SmartDashboard.putNumber("Elevator Talon Max Current", RobotData.elevS2OutputMax);
		double elevcvMax = Math.max(cvMax, stageOneTalon.getSelectedSensorVelocity(RobotData.elevPIDLoopIdx));
		SmartDashboard.putNumber("Elevator Max Velocity", elevcvMax);

		SmartDashboard.putNumber("DesiredPosition", RobotData.elevPositionTarget);
		SmartDashboard.putNumber("targetSensorPosition", targetPosition);

		double Abtn_cvMax = Math.max(cvMax, stageOneTalon.getSelectedSensorVelocity(RobotData.elevPIDLoopIdx));
		SmartDashboard.putNumber("Max Velocity A Button", Abtn_cvMax);
	}

	public void moveElevatorTo(double position) {
		RobotData.elevPositionTarget = InchesToS1Clicks(position);
		/* Motion Magic - 4096 ticks/rev * 10 Rotations in either direction */
		if (position <= 30) {
			moveS1To(RobotData.elevPositionTarget);
		} else {
			moveS1To(RobotData.elevStageOneMax);
			moveS2To(RobotData.elevPositionTarget - RobotData.elevStageOneMax);
		}
	}

	public void Init() {
		talonConfig(stageOneTalon);
		talonConfig(stageTwoTalon);
	}

	int InchesToS1Clicks(double pos) {
		return (int) (pos * RobotData.elevClicksPerUnit);
	}

	int InchesToS2Clicks(double pos) {
		return (int) (pos * RobotData.elevClicksPerUnit);
	}

	public void moveS1To(double pos) {
		stageOneTalon.set(ControlMode.MotionMagic, pos);
	}

	public void moveS2To(double pos) {
		stageTwoTalon.set(ControlMode.MotionMagic, pos);
	}

}
