package org.usfirst.frc.team1002.robot;

import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.Faults;
import com.ctre.phoenix.motorcontrol.FeedbackDevice;
import com.ctre.phoenix.motorcontrol.NeutralMode;
import com.ctre.phoenix.motorcontrol.StatusFrameEnhanced;
import com.ctre.phoenix.motorcontrol.can.TalonSRX;

import edu.wpi.first.wpilibj.AnalogInput;
import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

public class EightBitElev {
	private TalonSRX elevatorTalon;
	private DigitalInput upperLim;
	private String myName = "EightBitElevator";
	boolean isCascade = true;

	public EightBitElev() {
		elevatorTalon = new TalonSRX(RobotData.elevS1TalonPort);
		// upperLim = new DigitalInput(0);
	}

	public double getElevatorPositionUnits() {
		int encoderCount = elevatorTalon.getSelectedSensorPosition(RobotData.elevPIDLoopIdx);
		SmartDashboard.putNumber("Elevator height", encoderCount);
		return clicksToInches(encoderCount);
	}

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
				elevatorTalon.getSelectedSensorVelocity(RobotData.elevPIDLoopIdx));
		SmartDashboard.putNumber("Elevator S1 SensorPos",
				elevatorTalon.getSelectedSensorPosition(RobotData.elevPIDLoopIdx));

		SmartDashboard.putNumber("Elevator S1 MotorOutputPercent", elevatorTalon.getMotorOutputPercent());

		SmartDashboard.putNumber("Elevator S1 ClosedLoopError",
				elevatorTalon.getClosedLoopError(RobotData.elevPIDLoopIdx));
		SmartDashboard.putNumber("Elevator S1 ClosedLoopTarget",
				elevatorTalon.getClosedLoopError(RobotData.elevPIDLoopIdx));

		SmartDashboard.putNumber("Elevator CruiseVelocity", RobotData.elevCruiseVel);
		SmartDashboard.putNumber("Elevator CruiseAcceleration", RobotData.elevCruiseAccel);

		SmartDashboard.putNumber("Time", Timer.getFPGATimestamp());

		SmartDashboard.putNumber("Elevator Remaining Distance",
				RobotData.elevDistRemainder / RobotData.elevClicksPerUnitS1);
		SmartDashboard.putNumber("Elevator S1 Remaining Distance",
				RobotData.elevS1DistRemainder / RobotData.elevClicksPerUnitS1);
		SmartDashboard.putNumber("Elevator S1 Talon Current", elevatorTalon.getOutputCurrent());

		/* print the Active Trajectory Point Motion Magic is going towards */
		SmartDashboard.putNumber("Elevator S1 ActTrajVelocity", elevatorTalon.getActiveTrajectoryVelocity());
		SmartDashboard.putNumber("Elevator S1 ActTrajPosition", elevatorTalon.getActiveTrajectoryPosition());
		SmartDashboard.putNumber("Elevator S1 ActTrajHeading", elevatorTalon.getActiveTrajectoryHeading());

		RobotData.elevS1Position = elevatorTalon.getSelectedSensorPosition(RobotData.elevPIDLoopIdx);
		SmartDashboard.putNumber("S1 Position", RobotData.elevS1Position / RobotData.elevClicksPerUnitS1);
		RobotData.elevDistRemainder = RobotData.elevPositionTarget - RobotData.elevPosition;
		RobotData.elevS1DistRemainder = RobotData.elevS1PositionTarget - RobotData.elevS1Position;

		RobotData.elevS1OutputMax = Math.max(RobotData.elevS1OutputMax, elevatorTalon.getOutputCurrent());
		SmartDashboard.putNumber("Elevator Talon Max Current", RobotData.elevS1OutputMax);
		double elevS1cvMax = Math.max(RobotData.elevS1CVMax,
				elevatorTalon.getSelectedSensorVelocity(RobotData.elevPIDLoopIdx));
		SmartDashboard.putNumber("Elevator Max Velocity", elevS1cvMax);

		SmartDashboard.putNumber("DesiredPosition", RobotData.elevPositionTarget);
		SmartDashboard.putNumber("S1TargetSensorPosition", RobotData.elevS1PositionTarget);

		double AbtnS1cvMax = Math.max(RobotData.elevS1CVMax,
				elevatorTalon.getSelectedSensorVelocity(RobotData.elevPIDLoopIdx));
		SmartDashboard.putNumber("Max Velocity A Button", AbtnS1cvMax);

		/* smart dash plots */
		// SmartDashboard.putNumber("Elevator S2 SensorVel",
		// stageTwoTalon.getSelectedSensorVelocity(RobotData.elevPIDLoopIdx));
		// SmartDashboard.putNumber("Elevator S2 SensorPos",
		// stageTwoTalon.getSelectedSensorPosition(RobotData.elevPIDLoopIdx));

		// SmartDashboard.putNumber("Elevator S2 MotorOutputPercent",
		// stageTwoTalon.getMotorOutputPercent());

		// SmartDashboard.putNumber("Elevator S2 Talon Current",
		// stageTwoTalon.getOutputCurrent());
		SmartDashboard.putNumber("Elevator S2 Remaining Distance",
				RobotData.elevS2DistRemainder / RobotData.elevClicksPerUnitS1);

		/* print the Active Trajectory Point Motion Magic is going towards */
		// SmartDashboard.putNumber("Elevator S2 ActTrajVelocity",
		// stageTwoTalon.getActiveTrajectoryVelocity());
		// SmartDashboard.putNumber("Elevator S2 ActTrajPosition",
		// stageTwoTalon.getActiveTrajectoryPosition());
		// SmartDashboard.putNumber("Elevator S2 ActTrajHeading",
		// stageTwoTalon.getActiveTrajectoryHeading());

		// RobotData.elevS2Position =
		// stageTwoTalon.getSelectedSensorPosition(RobotData.elevPIDLoopIdx);
		SmartDashboard.putNumber("currentPosition", RobotData.elevS2Position / RobotData.elevClicksPerUnitS1);
		RobotData.elevS2DistRemainder = RobotData.elevS2PositionTarget - RobotData.elevS2Position;

		// RobotData.elevS2OutputMax = Math.max(RobotData.elevS2OutputMax,
		// stageTwoTalon.getOutputCurrent());
		SmartDashboard.putNumber("Elevator Talon S2 Max Current", RobotData.elevS2OutputMax);
		// double elevS2cvMax = Math.max(RobotData.elevS2CVMax,
		// stageTwoTalon.getSelectedSensorVelocity(RobotData.elevPIDLoopIdx));
		// SmartDashboard.putNumber("Elevator Max Velocity", elevS2cvMax);

		SmartDashboard.putNumber("S2TargetSensorPosition", RobotData.elevS2PositionTarget);

		// double AbtnS2cvMax = Math.max(RobotData.elevS2CVMax,
		// stageTwoTalon.getSelectedSensorVelocity(RobotData.elevPIDLoopIdx));
		// SmartDashboard.putNumber("Max Velocity A Button", AbtnS2cvMax);
	}

	public void moveTo(double position) {
		RobotData.elevIdle = false;
		// this will keep the elevator from moving past the maximum height.
		if (position >= RobotData.elevMaxHeightUnits) {
			position = RobotData.elevMaxHeightUnits;
			RobotData.elevPositionTarget = RobotData.elevMaxHeightUnits;
		}
		
		position = Math.max(0, position);
		
		SmartDashboard.putNumber("Elevator Target", position);
		SmartDashboard.putNumber("S1 Height (units)", position);
		SmartDashboard.putNumber("S1 Height (clicks)", inchesToClicks(position));

		elevatorTalon.set(ControlMode.MotionMagic, inchesToClicks(position));
		if (isCascade)
			return;
	}

	public void init() {
		talonConfig(elevatorTalon);
	}

	private int inchesToClicks(double pos) {
		return (int) (pos * RobotData.elevClicksPerUnitS1);
	}

	private double clicksToInches(int pos) {
		return pos / RobotData.elevClicksPerUnitS1;
	}

	public double moveElevatorToCurrentPosition() {
		int ePos = elevatorTalon.getSelectedSensorPosition(RobotData.elevPIDLoopIdx);
		elevatorTalon.set(ControlMode.MotionMagic, ePos);
		return clicksToInches(elevatorTalon.getSelectedSensorPosition(RobotData.elevPIDLoopIdx));
	}

	// public void checkStatus() {
	// if (Math.abs(getElevatorPositionUnits() - RobotData.elevPositionTarget) <=
	// 0.3) {
	// RobotData.elevIdle = true;
	// } else {
	// RobotData.elevIdle = false;
	// }
	// }

	public void moveElevToCurrentPos() {
		elevatorTalon.set(ControlMode.MotionMagic, elevatorTalon.getSelectedSensorPosition(RobotData.elevPIDLoopIdx));
	}

	private void safeTalon(TalonSRX thisTalon) {
		thisTalon.setNeutralMode(NeutralMode.Brake);
		//thisTalon.set(ControlMode.PercentOutput, 0);
		
	}

	private void checkEncoder(TalonSRX thisTalon) {
		int pulseWidthUs = thisTalon.getSensorCollection().getPulseWidthRiseToFallUs();
		int periodUs = thisTalon.getSensorCollection().getPulseWidthRiseToRiseUs();
		if (periodUs < 4) {
			/* Sensor Failure */
			safeTalon(thisTalon);
			System.out.println(myName + ":checkEncoder:" + "Pulse Width too small, p=" + periodUs + " :PulseWidth ="
					+ pulseWidthUs); // Always add your name
		}
	}

	public void checkStatus() {
		checkEncoder(elevatorTalon);
		double nowPos = clicksToInches(elevatorTalon.getSelectedSensorPosition(RobotData.armPIDLoopIdx));
		SmartDashboard.putNumber("Elevator Position", nowPos);
		if (Math.abs(nowPos - RobotData.elevStageOneMax) < 1) {
			SmartDashboard.putBoolean("Elevator Upper Limit", true);
		} else {
			SmartDashboard.putBoolean("Elevator Upper Limit", false);
		}
		if (Math.abs(nowPos - 0) < 1) {
			SmartDashboard.putBoolean("Elevator Lower Limit", true);
		} else {
			SmartDashboard.putBoolean("Elevator Lower Limit", false);
		}
		/* Again this should be check velocity, not position */
		// if (Math.abs(getElevatorPositionUnits() - RobotData.elevPositionTarget) <=
		// 0.3) {
		// RobotData.elevIdle = true;
		// } else {
		// RobotData.elevIdle = false;
		// }
	}
	public boolean isIdle() {
		return RobotData.elevIdle;
	}
}
