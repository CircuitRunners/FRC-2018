package org.usfirst.frc.team1002.robot;

import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.Faults;
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

		SmartDashboard.putNumber("Elevator Remaining Distance",
				RobotData.elevDistRemainder / RobotData.elevClicksPerUnit);
		SmartDashboard.putNumber("Elevator S1 Remaining Distance",
				RobotData.elevS1DistRemainder / RobotData.elevClicksPerUnit);
		SmartDashboard.putNumber("Elevator S1 Talon Current", stageOneTalon.getOutputCurrent());

		/* print the Active Trajectory Point Motion Magic is going towards */
		SmartDashboard.putNumber("Elevator S1 ActTrajVelocity", stageOneTalon.getActiveTrajectoryVelocity());
		SmartDashboard.putNumber("Elevator S1 ActTrajPosition", stageOneTalon.getActiveTrajectoryPosition());
		SmartDashboard.putNumber("Elevator S1 ActTrajHeading", stageOneTalon.getActiveTrajectoryHeading());

		RobotData.elevS1Position = stageOneTalon.getSelectedSensorPosition(RobotData.elevPIDLoopIdx);
		SmartDashboard.putNumber("S1 Position", RobotData.elevS1Position / RobotData.elevClicksPerUnit);
		RobotData.elevDistRemainder = RobotData.elevPositionTarget - RobotData.elevPosition;
		RobotData.elevS1DistRemainder = RobotData.elevS1PositionTarget - RobotData.elevS1Position;

		RobotData.elevS1OutputMax = Math.max(RobotData.elevS1OutputMax, stageOneTalon.getOutputCurrent());
		SmartDashboard.putNumber("Elevator Talon Max Current", RobotData.elevS1OutputMax);
		double elevS1cvMax = Math.max(RobotData.elevS1CVMax,
				stageOneTalon.getSelectedSensorVelocity(RobotData.elevPIDLoopIdx));
		SmartDashboard.putNumber("Elevator Max Velocity", elevS1cvMax);

		SmartDashboard.putNumber("DesiredPosition", RobotData.elevPositionTarget);
		SmartDashboard.putNumber("S1TargetSensorPosition", RobotData.elevS1PositionTarget);

		double AbtnS1cvMax = Math.max(RobotData.elevS1CVMax,
				stageOneTalon.getSelectedSensorVelocity(RobotData.elevPIDLoopIdx));
		SmartDashboard.putNumber("Max Velocity A Button", AbtnS1cvMax);

		/* smart dash plots */
		SmartDashboard.putNumber("Elevator S2 SensorVel",
				stageTwoTalon.getSelectedSensorVelocity(RobotData.elevPIDLoopIdx));
		SmartDashboard.putNumber("Elevator S2 SensorPos",
				stageTwoTalon.getSelectedSensorPosition(RobotData.elevPIDLoopIdx));

		SmartDashboard.putNumber("Elevator S2 MotorOutputPercent", stageTwoTalon.getMotorOutputPercent());

		SmartDashboard.putNumber("Elevator S2 Talon Current", stageTwoTalon.getOutputCurrent());
		SmartDashboard.putNumber("Elevator S2 Remaining Distance",
				RobotData.elevS2DistRemainder / RobotData.elevClicksPerUnit);

		/* print the Active Trajectory Point Motion Magic is going towards */
		SmartDashboard.putNumber("Elevator S2 ActTrajVelocity", stageTwoTalon.getActiveTrajectoryVelocity());
		SmartDashboard.putNumber("Elevator S2 ActTrajPosition", stageTwoTalon.getActiveTrajectoryPosition());
		SmartDashboard.putNumber("Elevator S2 ActTrajHeading", stageTwoTalon.getActiveTrajectoryHeading());

		RobotData.elevS2Position = stageTwoTalon.getSelectedSensorPosition(RobotData.elevPIDLoopIdx);
		SmartDashboard.putNumber("currentPosition", RobotData.elevS2Position / RobotData.elevClicksPerUnit);
		RobotData.elevS2DistRemainder = RobotData.elevS2PositionTarget - RobotData.elevS2Position;

		RobotData.elevS2OutputMax = Math.max(RobotData.elevS2OutputMax, stageTwoTalon.getOutputCurrent());
		SmartDashboard.putNumber("Elevator Talon S2 Max Current", RobotData.elevS2OutputMax);
		double elevS2cvMax = Math.max(RobotData.elevS2CVMax,
				stageTwoTalon.getSelectedSensorVelocity(RobotData.elevPIDLoopIdx));
		SmartDashboard.putNumber("Elevator Max Velocity", elevS2cvMax);

		SmartDashboard.putNumber("S2TargetSensorPosition", RobotData.elevS2PositionTarget);

		double AbtnS2cvMax = Math.max(RobotData.elevS2CVMax,
				stageTwoTalon.getSelectedSensorVelocity(RobotData.elevPIDLoopIdx));
		SmartDashboard.putNumber("Max Velocity A Button", AbtnS2cvMax);
	}

	public void moveElevatorTo(double position) {
		RobotData.elevIdle = false;
		RobotData.elevPositionTarget = inchesToS1Clicks(position);
		/* Motion Magic - 4096 ticks/rev * 10 Rotations in either direction */
		if (Math.abs((stageOneTalon.getSelectedSensorPosition(RobotData.elevPIDLoopIdx)
				+ stageTwoTalon.getSelectedSensorPosition(RobotData.elevPIDLoopIdx))) - position <= 0.02) {
			RobotData.elevIdle = true;
		}
		if (position <= 30) {
			moveS1To(RobotData.elevPositionTarget);
		} else {
			moveS1To(RobotData.elevStageOneMax);
			moveS2To(RobotData.elevPositionTarget - RobotData.elevStageOneMax);
		}
	}

	Faults f;

	public void init() {
		talonConfig(stageOneTalon);
		talonConfig(stageTwoTalon);
		f = new Faults();
	}

	int inchesToS1Clicks(double pos) {
		return (int) (pos * RobotData.elevClicksPerUnit);
	}

	int inchesToS2Clicks(double pos) {
		return (int) (pos * RobotData.elevClicksPerUnit);
	}

	public void moveS1To(double pos) {
		stageOneTalon.getFaults(f);
		if (f.ForwardLimitSwitch) {
			// what do you want to put here
		}
		if (f.ReverseLimitSwitch) {
			// what do you want to put here
		}

		stageOneTalon.set(ControlMode.MotionMagic, pos);
	}

	public void moveS2To(double pos) {
		stageTwoTalon.getFaults(f);
		if (f.ForwardLimitSwitch) {
			// what do you want to put here
		}
		if (f.ReverseLimitSwitch) {
			// what do you want to put here
		}
		stageTwoTalon.set(ControlMode.MotionMagic, pos);
	}

}
