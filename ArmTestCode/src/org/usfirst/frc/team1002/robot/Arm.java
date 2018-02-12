package org.usfirst.frc.team1002.robot;

import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.Faults;
import com.ctre.phoenix.motorcontrol.FeedbackDevice;
import com.ctre.phoenix.motorcontrol.StatusFrameEnhanced;
import com.ctre.phoenix.motorcontrol.can.TalonSRX;

import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

public class Arm {
	static TalonSRX ArmTalon = new TalonSRX(RobotData.armTalonPort);
	public double currentPosition= 0;
	public double maxVelocity = 0;
	public double maxCurrent = 0;
	public double current = 0;
	
	double currPositionClicks = 0;
	double desiredPosition = 0;
	double targetPosition = 0;
	double remainderClicks = 0;

	int currentArmCmd = 0;
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

		SmartDashboard.putNumber("CruiseVelocity", 1300);
		SmartDashboard.putNumber("CruiseAcceleration", 1300);
	}

	public void init() {
		talonInit(ArmTalon, 1300, 1300);
	}

	public void displayTalonParms(String TalonNM, TalonSRX talonController) {
		boolean InMotionMagic = false;
		/* smart dash plots */
		SmartDashboard.putNumber(TalonNM + "CruiseVelocity", 1300);
		SmartDashboard.putNumber(TalonNM + "CruiseAcceleration", 1300);
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

	public void talonConfig(TalonSRX talonController) {
		double cv = 0, ca = 0;
		cv = RobotData.armCruiseVel;
		ca = RobotData.armCruiseAccel;
		ca = SmartDashboard.getNumber("CruiseAcceleration", 100.0);
		/* set acceleration and vcruise velocity - see documentation */
		talonController.configMotionCruiseVelocity((int) cv, kTimeoutMs);
		talonController.configMotionAcceleration((int) ca, kTimeoutMs);

	}

	double clicksToDegrees(double pos) {
		return pos / RobotData.armClicksPerDegree;
	}

	double DegreesToClicks(double pos) {
		return pos * RobotData.armClicksPerDegree;
	}

	public void setOutput(double PercOut) {
		maxCurrent = maxVelocity =0.0;
		ArmTalon.set(ControlMode.PercentOutput, PercOut);
		SmartDashboard.putString("Arm Controller Status", "PercentageOutput");
		currentArmCmd = PercentOut;
	}

	public void moveTo(double position) {
		maxCurrent = maxVelocity =0.0;
		SmartDashboard.putNumber("Arm DesiredPosition", position);
		targetPosition = DegreesToClicks(position);
		talonConfig(ArmTalon);
		/* Motion Magic - 4096 ticks/rev * 10 Rotations in either direction */
		ArmTalon.set(ControlMode.MotionMagic, targetPosition);
		SmartDashboard.putNumber("targetSensorPosition", targetPosition);
		currentArmCmd = Moving;
	}

	public void checkState() {
		Faults f = new Faults();
		double velocity;
		// while (Math.abs(
		// (currentPosition = talonController.getSelectedSensorPosition(kPIDLoopIdx)) -
		// targetPosition) > 100) {
		String TalonNM = "";
		TalonSRX thisTalon = ArmTalon;
		/* Check the first stage talon */
		TalonNM = "";
		thisTalon = ArmTalon;
		/* Check the first stage talon */
		currPositionClicks = thisTalon.getSelectedSensorPosition(kPIDLoopIdx);
		currentPosition = clicksToDegrees(currPositionClicks);
		remainderClicks = targetPosition - currPositionClicks;
		displayTalonParms(TalonNM, thisTalon);
		maxVelocity = Math.max(maxVelocity, Math.abs(thisTalon.getSelectedSensorVelocity(kPIDLoopIdx)));
		current = thisTalon.getOutputCurrent();
		maxCurrent = Math.max(maxCurrent, Math.abs(current));
		SmartDashboard.putNumber(TalonNM + "MaxVel", maxVelocity);
		SmartDashboard.putNumber(TalonNM + "Remaining Distance", clicksToDegrees(remainderClicks));
		SmartDashboard.putNumber(TalonNM + "currentPosition", currentPosition);
		SmartDashboard.putNumber(TalonNM + "MaxCurrent", maxCurrent);
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
		if (Math.abs(remainderClicks) < 2000) {
			currentArmCmd = Arrived;
		}
	}
}
