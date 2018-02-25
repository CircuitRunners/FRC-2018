package org.usfirst.frc.team1002.robot;

import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.Faults;
import com.ctre.phoenix.motorcontrol.FeedbackDevice;
import com.ctre.phoenix.motorcontrol.StatusFrameEnhanced;
import com.ctre.phoenix.motorcontrol.can.TalonSRX;

import edu.wpi.first.wpilibj.AnalogInput;
import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

public class EightBitElev {
	TalonSRX elevatorTalon;
	//TalonSRX stageTwoTalon;
	DigitalInput upperLim;

	Faults f;

	public EightBitElev() {
		elevatorTalon = new TalonSRX(RobotData.elevS1TalonPort);
		//stageTwoTalon = new TalonSRX(RobotData.elevS2TalonPort);
		//upperLim = new DigitalInput(0);
		f = new Faults();
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
		//SmartDashboard.putNumber("Elevator S2 SensorVel",
		//		stageTwoTalon.getSelectedSensorVelocity(RobotData.elevPIDLoopIdx));
		//SmartDashboard.putNumber("Elevator S2 SensorPos",
		//		stageTwoTalon.getSelectedSensorPosition(RobotData.elevPIDLoopIdx));

		//SmartDashboard.putNumber("Elevator S2 MotorOutputPercent", stageTwoTalon.getMotorOutputPercent());

	//	SmartDashboard.putNumber("Elevator S2 Talon Current", stageTwoTalon.getOutputCurrent());
		SmartDashboard.putNumber("Elevator S2 Remaining Distance",
				RobotData.elevS2DistRemainder / RobotData.elevClicksPerUnitS1);

		/* print the Active Trajectory Point Motion Magic is going towards */
	//	SmartDashboard.putNumber("Elevator S2 ActTrajVelocity", stageTwoTalon.getActiveTrajectoryVelocity());
		//SmartDashboard.putNumber("Elevator S2 ActTrajPosition", stageTwoTalon.getActiveTrajectoryPosition());
		//SmartDashboard.putNumber("Elevator S2 ActTrajHeading", stageTwoTalon.getActiveTrajectoryHeading());

		//RobotData.elevS2Position = stageTwoTalon.getSelectedSensorPosition(RobotData.elevPIDLoopIdx);
		SmartDashboard.putNumber("currentPosition", RobotData.elevS2Position / RobotData.elevClicksPerUnitS1);
		RobotData.elevS2DistRemainder = RobotData.elevS2PositionTarget - RobotData.elevS2Position;

	//	RobotData.elevS2OutputMax = Math.max(RobotData.elevS2OutputMax, stageTwoTalon.getOutputCurrent());
		SmartDashboard.putNumber("Elevator Talon S2 Max Current", RobotData.elevS2OutputMax);
	//	double elevS2cvMax = Math.max(RobotData.elevS2CVMax,
				//stageTwoTalon.getSelectedSensorVelocity(RobotData.elevPIDLoopIdx));
	//	SmartDashboard.putNumber("Elevator Max Velocity", elevS2cvMax);

		SmartDashboard.putNumber("S2TargetSensorPosition", RobotData.elevS2PositionTarget);

		//double AbtnS2cvMax = Math.max(RobotData.elevS2CVMax,
				//stageTwoTalon.getSelectedSensorVelocity(RobotData.elevPIDLoopIdx));
		//SmartDashboard.putNumber("Max Velocity A Button", AbtnS2cvMax);
	}


	public void moveElevatorTo(double position) {
		RobotData.elevIdle = false;
		if (position >= RobotData.elevStageOneMaxUnits + RobotData.elevStageTwoMaxUnits) {
			position = RobotData.elevStageOneMaxUnits + RobotData.elevStageTwoMaxUnits;
			RobotData.elevPositionTarget = RobotData.elevStageOneMaxUnits + RobotData.elevStageTwoMaxUnits;
		}
		// This tells the robot if the elevator is at idle.
	//	if (Math.abs((elevatorTalon.getSelectedSensorPosition(RobotData.elevPIDLoopIdx)
			//	+ stageTwoTalon.getSelectedSensorPosition(RobotData.elevPIDLoopIdx))) - position <= 0.02) {
		//	RobotData.elevIdle = true;
		//}
		elevatorTalon.getFaults(f);
		if (f.ReverseLimitSwitch) {
			RobotData.elevPositionTarget = Math.max(RobotData.elevPositionTarget, 0);
		}
		if (position <= 35) {
			moveS2To(position);
			moveS1To(0);
		} else {
			moveS2To(RobotData.elevStageTwoMaxUnits);
			moveS1To(position - RobotData.elevStageOneMaxUnits);
		}
	}

	public void init() {
		talonConfig(elevatorTalon);
		//talonConfig(stageTwoTalon);
		// f = new Faults();
	}

	int inchesToS1Clicks(double pos) {
		return (int) (pos * RobotData.elevClicksPerUnitS1);
	}

	int inchesToS2Clicks(double pos) {
		return (int) (pos * RobotData.elevClicksPerUnitS2);
	}

	public void moveS1To(double pos) {

		// stageOneTalon.getFaults(f);

		if (pos > RobotData.elevStageOneMaxUnits) {
			pos = RobotData.elevStageOneMaxUnits;
		}

		SmartDashboard.putNumber("S1 Height (units)", pos);
		SmartDashboard.putNumber("S1 Height (clicks)", inchesToS1Clicks(pos));

		elevatorTalon.set(ControlMode.MotionMagic, inchesToS1Clicks(pos));
	}

	public void moveS2To(double pos) {

		// stageTwoTalon.getFaults(f);

		if (pos > RobotData.elevStageTwoMaxUnits) {
			pos = RobotData.elevStageTwoMaxUnits;
		}
		if (pos < 0) {
			pos = 0;
		}
		// if(f.ReverseLimitSwitch) {

		// }
		SmartDashboard.putNumber("S2 Height(units)", pos);
		SmartDashboard.putNumber("S2 Height(clicks)", inchesToS2Clicks(pos));

		//stageTwoTalon.set(ControlMode.MotionMagic, inchesToS2Clicks(pos));
	}

	//public double getElevatorPositionUnits() {
	//	return (elevatorTalon.getSelectedSensorPosition(RobotData.elevPIDLoopIdx) / RobotData.elevClicksPerUnitS1)
	//			+ (stageTwoTalon.getSelectedSensorPosition(RobotData.elevPIDLoopIdx) / RobotData.elevClicksPerUnitS2);
	//}

	public void setElevatorPositionUnits(double pos) {
		moveElevatorTo(pos);
	}

	//public void checkStatus() {
		//if (Math.abs(getElevatorPositionUnits() - RobotData.elevPositionTarget) <= 0.3) {
			//RobotData.elevIdle = true;
	//	} else {
			//RobotData.elevIdle = false;
		//}
	//}

	public void moveElevatorPecrcentageBased(double speed) {
		if (Math.abs(
				RobotData.elevLastEncPos - elevatorTalon.getSelectedSensorPosition(RobotData.elevPIDLoopIdx)) == 0) {

			elevatorTalon.set(ControlMode.PercentOutput, 0);

			System.out.println("ENCODER ERROR");
		} else {
			//if (!upperLim.get()) {
		//		elevatorTalon.set(ControlMode.PercentOutput,Math.min(0, speed));
		//	}else{
				elevatorTalon.set(ControlMode.PercentOutput, speed);
		//	}
		}
	}

	public void moveElevToCurrentPos() {
		elevatorTalon.set(ControlMode.MotionMagic, elevatorTalon.getSelectedSensorPosition(RobotData.elevPIDLoopIdx));
	}

	public double getEncoderPosition(TalonSRX talon) {
		return talon.getSelectedSensorPosition(RobotData.elevPIDLoopIdx);
	}
}
