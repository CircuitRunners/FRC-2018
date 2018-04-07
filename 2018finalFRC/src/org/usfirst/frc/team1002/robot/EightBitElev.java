package org.usfirst.frc.team1002.robot;

import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.FeedbackDevice;
import com.ctre.phoenix.motorcontrol.NeutralMode;
import com.ctre.phoenix.motorcontrol.StatusFrameEnhanced;
import com.ctre.phoenix.motorcontrol.can.TalonSRX;

//import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

public class EightBitElev {
	private TalonSRX elevatorTalon;
	// private DigitalInput upperLim;
	private String myName = "EightBitElevator";
	boolean isCascade = true;
	int speedFactor = 100;
	int elevCV = 15000;
	int maxElevCV = 18000;

	public EightBitElev() {
		elevatorTalon = new TalonSRX(RobotData.elevS1TalonPort);
		// upperLim = new DigitalInput(5);

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
		thisTalon.setStatusFramePeriod(StatusFrameEnhanced.Status_2_Feedback0, 10, RobotData.elevTimeoutMs);
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
		thisTalon.configMotionCruiseVelocity(elevCV, RobotData.elevTimeoutMs);
		thisTalon.configMotionAcceleration(elevCV, RobotData.elevTimeoutMs);
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
		RobotData.elevDistRemainder = RobotData.elevPositionTarget - RobotData.elevPositionClicks;
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
	}

	boolean limitless = true;
	double maxTime = 0;
	public double moveTo(double position, int speedfactor, double time) {
		boolean insideLimits = true;
		maxTime = Timer.getFPGATimestamp() + time;
		int sf = Math.min(speedFactor, 100);
		sf = Math.max(0, sf);

		double safePosition = Math.min(RobotData.elevMaxHeightUnits, position);
		safePosition = Math.max(0, safePosition);

		if (position != safePosition) {
			insideLimits = false;
		}
		if (limitless) {
			if (insideLimits)
				limitless = false;
			internalMoveTo(position, sf);
			return position;
		} else {
			internalMoveTo(safePosition, sf);
			return safePosition;
		}

	}

	public void internalMoveTo(double position, int sf) {
		RobotData.elevIdle = false;
		if (sf != speedFactor) {
			speedFactor = sf;
			elevCV = (int) (maxElevCV * (sf / 100.0));
			elevatorTalon.configMotionCruiseVelocity(elevCV, RobotData.armTimeoutMs);
			elevatorTalon.configMotionAcceleration(elevCV, RobotData.armTimeoutMs);
		}

		RobotData.elevPositionClicks = inchesToClicks(position);
		elevatorTalon.set(ControlMode.MotionMagic, inchesToClicks(position));

		SmartDashboard.putNumber("Elevator Target", position);
		SmartDashboard.putNumber("Elevator Height (units)", position);
		SmartDashboard.putNumber("Elevator Height (clicks)", inchesToClicks(position));

	}

	public void init() {
		int startPos = 13413 - 1816;
		talonConfig(elevatorTalon);
		elevatorTalon.setSelectedSensorPosition(startPos, RobotData.elevPIDLoopIdx, RobotData.elevTimeoutMs);
		RobotData.elevPositionClicks = startPos;
	}

	private int inchesToClicks(double pos) {
		return (int) (pos * RobotData.elevClicksPerUnitS1);
	}

	private double clicksToInches(int pos) {
		return pos / RobotData.elevClicksPerUnitS1;
	}

	public double moveToCurrentPosition() {
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

	private void safeTalon(TalonSRX thisTalon) {
		thisTalon.setNeutralMode(NeutralMode.Brake);
		// thisTalon.set(ControlMode.PercentOutput, 0);

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
	double currentTime = 0;
	public void checkStatus() {
		checkEncoder(elevatorTalon);
		currentTime = Timer.getFPGATimestamp();-
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

		if (Math.abs(elevatorTalon.getSelectedSensorPosition(RobotData.armPIDLoopIdx)
				- RobotData.elevPositionClicks) <= 1000 || currentTime > maxTime) {
			RobotData.elevIdle = true;
		}
		// if (upperLim.get()) {
		// RobotData.elevPositionTarget -= 0.2;

		// }
		/* Again this should be check velocity, not position */
		// if (Math.abs(getElevatorPositionUnits() - RobotData.elevPositionTarget) <=
		// 0.3) {
		// RobotData.elevIdle = true;
		// } else {
		// RobotData.elevIdle = false;
		// }
		int elevPos = elevatorTalon.getSelectedSensorPosition(RobotData.elevPIDLoopIdx);

		SmartDashboard.putNumber("ChkStat Elev Pos-Clicks", elevPos);
		SmartDashboard.putNumber("ChkStat Elev Pos-Inches", clicksToInches(elevPos));
	}

	public boolean isIdle() {
		SmartDashboard.putBoolean("Elev Idle", RobotData.elevIdle);
		return RobotData.elevIdle;
	}

	public void setPosition(double positionUnits) {
		RobotData.elevPositionTarget = positionUnits;
	}

	public void enableLimitless() {
		limitless = true;
	}

	public void displayElevStatus() {
		SmartDashboard.putNumber("Elevator Amperage Pull", elevatorTalon.getOutputCurrent());
		SmartDashboard.putNumber("Elevator Voltage Draw", elevatorTalon.getMotorOutputVoltage());
		SmartDashboard.putNumber("Elevator MotorController Temperature", elevatorTalon.getTemperature());
	}
}
