package org.usfirst.frc.team1002.robot;

import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.Faults;
import com.ctre.phoenix.motorcontrol.FeedbackDevice;
import com.ctre.phoenix.motorcontrol.StatusFrameEnhanced;
import com.ctre.phoenix.motorcontrol.can.TalonSRX;

import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

public class Elevator {

	public double currentPosition = 0;
	public double maxCurrent = 0;
	public double current = 0;

	public double currentS1Position = 0;
	public double maxS1Velocity = 0;
	public double S1current = 0;

	public double currentS2Position = 0;
	public double maxS2Current = 0;
	public double S2current = 0;

	static TalonSRX S1Talon = new TalonSRX(RobotData.elevS1TalonPort);
	static TalonSRX S2Talon = new TalonSRX(RobotData.elevS2TalonPort);

	double desiredPosition = 0;
	double targetPosition = 0;

	int S1CurrentPosClicks = 0;
	int S1Remainder = 0;
	int S1TargetPos = 0;
	int S1MaxVel = 0;
	double S1MaxCurrent = 0;

	int S2CurrentPosClicks = 0;
	int S2Remainder = 0;
	int S2TargetPos = 0;
	int S2MaxVel = 0;
	double S2MaxCurrent = 0;

	int currentS1Cmd = 0;
	int currentS2Cmd = 0;

	public static int IDLE = 0;
	public static int PercentOut = 1;
	public static int Moving = 2;
	public static int Arrived = 3;

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

	public static final double clicksPerUnit = 1024;

	public void talonInit(TalonSRX talonController, int cruise, int accel) {
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
		talonController.config_kF(0, 0.2, kTimeoutMs);
		talonController.config_kP(0, 0.2, kTimeoutMs);
		talonController.config_kI(0, 0, kTimeoutMs);
		talonController.config_kD(0, 0, kTimeoutMs);
		/* set acceleration and vcruise velocity - see documentation */
		talonController.configMotionCruiseVelocity(cruise, kTimeoutMs);
		talonController.configMotionAcceleration(accel, kTimeoutMs);
		/* zero the sensor */
		talonController.setSelectedSensorPosition(0, kPIDLoopIdx, kTimeoutMs);
	}

	public void init() {
		talonInit(S1Talon, 1300, 1300);
		talonInit(S2Talon, 1300, 1300);
	}

	public void displayTalonParms(String TalonNM, TalonSRX talonController) {
		boolean InMotionMagic = false;
		/* smart dash plots */
		SmartDashboard.putNumber(TalonNM + "SensorVel", talonController.getSelectedSensorVelocity(kPIDLoopIdx));
		SmartDashboard.putNumber(TalonNM + "SensorPos", talonController.getSelectedSensorPosition(kPIDLoopIdx));
		SmartDashboard.putNumber(TalonNM + "MotorOutputPercent", talonController.getMotorOutputPercent());
		SmartDashboard.putNumber(TalonNM + "ClosedLoopError", talonController.getClosedLoopError(kPIDLoopIdx));
		SmartDashboard.putNumber(TalonNM + "ClosedLoopTarget", talonController.getClosedLoopError(kPIDLoopIdx));

		/* check if we are motion-magic-ing */
		if (talonController.getControlMode() == ControlMode.MotionMagic) {
			SmartDashboard.putString(TalonNM + "COntrollerStatus", "MotionMagicMode");
			InMotionMagic = true;
		} else {
			SmartDashboard.putString(TalonNM + "COntrollerStatus", "Non-MM");
			InMotionMagic = false;
		}
		if (InMotionMagic) {
			/* print the Active Trajectory Point Motion Magic is servoing towards */
			SmartDashboard.putNumber(TalonNM + "ActTrajVelocity", talonController.getActiveTrajectoryVelocity());
			SmartDashboard.putNumber(TalonNM + "ActTrajPosition", talonController.getActiveTrajectoryPosition());
			SmartDashboard.putNumber(TalonNM + "ActTrajHeading", talonController.getActiveTrajectoryHeading());
		}
	}

	public void talonConfig(TalonSRX talonController, String elName) {
		double cv = 0, ca = 0;
		if (elName == "S1") {
			cv = RobotData.elevS1CruiseVel;
			ca = RobotData.elevS1CruiseAccel;
		}
		else {
			cv = RobotData.elevS2CruiseVel;
			ca = RobotData.elevS2CruiseAccel;
		}

		/* set acceleration and vcruise velocity - see documentation */
		talonController.configMotionCruiseVelocity((int) cv, kTimeoutMs);
		talonController.configMotionAcceleration((int) ca, kTimeoutMs);

	}

	double clicksToS1Inches(double pos) {
		return pos / RobotData.elevClicksPerInch;
	}

	double clicksToS2Inches(double pos) {
		return pos / RobotData.elevClicksPerInch;
	}

	int InchesToS1Clicks(double pos) {
		return (int) (pos * RobotData.elevClicksPerInch);
	}

	int InchesToS2Clicks(double pos) {
		return (int) (pos * RobotData.elevClicksPerInch);
	}

	public void setS1Output(double PercOut) {
		S1MaxCurrent = S1MaxVel = 0;
		S1Talon.set(ControlMode.PercentOutput, PercOut);
		SmartDashboard.putString("ControllerStatus", "PercentageOutput");
		currentS1Cmd = PercentOut;
	}

	void moveS1To(double position) {
		S1MaxCurrent = S1MaxVel = 0;
		S1TargetPos = InchesToS1Clicks(position);
		talonConfig(S1Talon, "S1");
		/* Motion Magic - 4096 ticks/rev * 10 Rotations in either direction */
		S1Talon.set(ControlMode.MotionMagic, S1TargetPos);
		SmartDashboard.putNumber("Stage1 targetSensorPosition", S1TargetPos);
		currentS1Cmd = Moving;
	}

	void moveS2To(double position) {
		S2MaxCurrent = S2MaxVel = 0;
		S2TargetPos = InchesToS1Clicks(position);
		talonConfig(S2Talon, "S2");
		/* Motion Magic - 4096 ticks/rev * 10 Rotations in either direction */
		S2Talon.set(ControlMode.MotionMagic, S2TargetPos);
		SmartDashboard.putNumber("Stage2 targetSensorPosition", S2TargetPos);
		currentS2Cmd = Moving;
	}

	double safetyCheck(double pos) {
		double protectedSection = 0;
		if (Robot.arm.currentPosition < 0) {
			protectedSection = RobotData.armLenght * Math.sin(-Robot.arm.currentPosition * Math.PI);
			if (pos < protectedSection)
				return protectedSection;
			else
				return pos;
		} else
			return pos;
	}

	public void moveTo(double position) {
		SmartDashboard.putNumber("Elevator DesiredPosition", position);
		desiredPosition = position;
		targetPosition = safetyCheck(desiredPosition);
		moveS1To(targetPosition / 2);
		moveS2To(targetPosition / 2);
	}

	public void performLoop() {
		Faults f = new Faults();
		double velocity;
		String TalonNM = "";
		TalonSRX thisTalon;
		// while (Math.abs(
		// (currentPosition = talonController.getSelectedSensorPosition(kPIDLoopIdx)) -
		// targetPosition) > 100) {
		TalonNM = "Stage1 ";
		thisTalon = S1Talon;
		/* Check the first stage talon */
		S1CurrentPosClicks = thisTalon.getSelectedSensorPosition(kPIDLoopIdx);
		currentS1Position = clicksToS1Inches(S1CurrentPosClicks);
		S1Remainder = S1TargetPos - S1CurrentPosClicks;
		displayTalonParms(TalonNM, thisTalon);
		S1MaxVel = Math.max(S1MaxVel, Math.abs(thisTalon.getSelectedSensorVelocity(kPIDLoopIdx)));
		S1current = thisTalon.getOutputCurrent();
		S1MaxCurrent = Math.max(S1MaxCurrent, Math.abs(S1current));
		SmartDashboard.putNumber(TalonNM + "MaxVel", S1MaxVel);
		SmartDashboard.putNumber("Remaining Distance", clicksToS1Inches(S1Remainder));
		SmartDashboard.putNumber("currentPosition", currentS1Position);
		SmartDashboard.putNumber(TalonNM + "MaxCurrent", S1MaxCurrent);
		velocity = thisTalon.getSelectedSensorVelocity(kPIDLoopIdx);
		if (velocity < 100) {
			/* Check to see if it has hit a limit switch * */
			thisTalon.getFaults(f);
			if (f.ForwardLimitSwitch) {
				// limit switch is closed
			}
			if (f.ReverseLimitSwitch) {
				// limit switch is closed
			}
		}
		if (Math.abs(S1Remainder) < 2000) {
			currentS1Cmd = Arrived;
			SmartDashboard.putNumber(TalonNM + "SensorVel", thisTalon.getSelectedSensorVelocity(kPIDLoopIdx));
			SmartDashboard.putNumber(TalonNM + "SensorPos", thisTalon.getSelectedSensorPosition(kPIDLoopIdx));
		}

		TalonNM = "Stage2 ";
		thisTalon = S2Talon;
		/* Check the first stage talon */
		S2CurrentPosClicks = thisTalon.getSelectedSensorPosition(kPIDLoopIdx);
		currentS2Position = clicksToS2Inches(S2CurrentPosClicks);
		S2Remainder = S2TargetPos - S2CurrentPosClicks;
		displayTalonParms(TalonNM, thisTalon);
		S2MaxVel = Math.max(S2MaxVel, Math.abs(thisTalon.getSelectedSensorVelocity(kPIDLoopIdx)));
		S2current = thisTalon.getOutputCurrent();
		S2MaxCurrent = Math.max(S2MaxCurrent, Math.abs(S2current));
		SmartDashboard.putNumber(TalonNM + "MaxVel", S2MaxVel);
		SmartDashboard.putNumber("Remaining Distance", clicksToS2Inches(S2Remainder));
		SmartDashboard.putNumber("currentPosition", currentS2Position);
		SmartDashboard.putNumber(TalonNM + "MaxCurrent", S2MaxCurrent);
		velocity = thisTalon.getSelectedSensorVelocity(kPIDLoopIdx);
		if (velocity < 100) {
			/* Check to see if it has hit a limit switch * */
			thisTalon.getFaults(f);
			if (f.ForwardLimitSwitch) {
				// limit switch is closed
			}
			if (f.ReverseLimitSwitch) {
				// limit switch is closed
			}
		}
		if (Math.abs(S2Remainder) < 2000) {
			currentS2Cmd = Arrived;
			SmartDashboard.putNumber(TalonNM + "SensorVel", thisTalon.getSelectedSensorVelocity(kPIDLoopIdx));
			SmartDashboard.putNumber(TalonNM + "SensorPos", thisTalon.getSelectedSensorPosition(kPIDLoopIdx));
		}

		// Compute the published totals
		currentPosition = currentS1Position + currentS2Position + RobotData.elevOffset;
		maxCurrent = maxS2Current + maxS2Current;
		current = S1current + S2current;

		/* Check to see if we were safety limited before */
		if (desiredPosition != targetPosition) {
			moveTo(desiredPosition);
		}
	}
}
