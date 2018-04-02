package org.usfirst.frc.team1002.robot;

import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.FeedbackDevice;
import com.ctre.phoenix.motorcontrol.StatusFrameEnhanced;
import com.ctre.phoenix.motorcontrol.can.TalonSRX;

import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

public class RobotArm {
	TalonSRX armTalon;
	int speedFactor = 100;
    // Faults f;

	public RobotArm() {
		armTalon = new TalonSRX(RobotData.armTalonPort);
	}

	public void init() {
		talonConfig(armTalon);
		armTalon.setSelectedSensorPosition(-11300, RobotData.armPIDLoopIdx, RobotData.armTimeoutMs);
		RobotData.armPositionClicks = -11300 + 1950;
		// f = new Faults();
	}
 public int armCV = 10000;
 int maxArmCV = 10000;
	public void talonConfig(TalonSRX thisTalon) {
		/* first choose the sensor */
		thisTalon.configSelectedFeedbackSensor(FeedbackDevice.CTRE_MagEncoder_Relative, RobotData.elevPIDLoopIdx,
				RobotData.armTimeoutMs);
		thisTalon.setSensorPhase(true);
		thisTalon.setInverted(false);

		/* Set relevant frame periods to be at least as fast as periodic rate */
		thisTalon.setStatusFramePeriod(StatusFrameEnhanced.Status_13_Base_PIDF0, 10, RobotData.armTimeoutMs);
		thisTalon.setStatusFramePeriod(StatusFrameEnhanced.Status_10_MotionMagic, 10, RobotData.armTimeoutMs);
		thisTalon.setStatusFramePeriod(StatusFrameEnhanced.Status_2_Feedback0, 10, RobotData.armTimeoutMs);
		/* set the peak and nominal outputs */
		thisTalon.configNominalOutputForward(0, RobotData.armTimeoutMs);
		thisTalon.configNominalOutputReverse(0, RobotData.armTimeoutMs);
		thisTalon.configPeakOutputForward(1, RobotData.armTimeoutMs);
		thisTalon.configPeakOutputReverse(-1, RobotData.armTimeoutMs);

		/* set closed loop gains in slot0 - see documentation */
		thisTalon.selectProfileSlot(RobotData.armSlotIdx, RobotData.armPIDLoopIdx);
		thisTalon.config_kF(0, 0.287, RobotData.armTimeoutMs);
		thisTalon.config_kP(0, 0.4, RobotData.armTimeoutMs);
		thisTalon.config_kI(0, 0, RobotData.armTimeoutMs);
		thisTalon.config_kD(0, 0, RobotData.armTimeoutMs);
		/* set acceleration and cruise velocity - see documentation */
		thisTalon.configMotionCruiseVelocity(armCV, RobotData.armTimeoutMs);
		thisTalon.configMotionAcceleration(armCV, RobotData.armTimeoutMs);
		/* zero the sensor */
		thisTalon.setSelectedSensorPosition(0, RobotData.armPIDLoopIdx, RobotData.armTimeoutMs);

	}

	void displayArmStatus() {
		SmartDashboard.putNumber("Arm Desired Angle", RobotData.armPositionTarget);
		SmartDashboard.putNumber("Arm desired encoder count", RobotData.armPositionClicks);
		SmartDashboard.putNumber("Arm Amperage Pull",armTalon.getOutputCurrent());
		SmartDashboard.putNumber("Arm Voltage Draw", armTalon.getMotorOutputVoltage());
		SmartDashboard.putNumber("Arm MotorController Temperature", armTalon.getTemperature());
	}

	
	public void checkStatus() {
		int armPos = armTalon.getSelectedSensorPosition(RobotData.armPIDLoopIdx);
		if (Math.abs(armPos - RobotData.armPositionClicks) <= 300) {
			RobotData.armIdle = true;
		} else {
			RobotData.armIdle = false;
		}
		/*
		 * if(Robot.elev.getElevatorPositionUnits() < 10.0) {
		 * RobotData.armPositionDegrees = Math.max(0, RobotData.armPositionDegrees); }
		 */
		SmartDashboard.putNumber("ChkStat Arm Pos-Clicks", armPos);
		SmartDashboard.putNumber("ChkStat Arm Pos-Degrees", clicksToDegrees(armPos));
	}

	boolean limitless = true;

	public double moveTo(double angle, int speedfactor) {
		boolean insideLimits = true;

		int sf = Math.min(100,  speedFactor);
		sf = Math.max(0, sf);
		
		double safeAngle = Math.max(angle, RobotData.armMinAngle);
		safeAngle = Math.min(safeAngle, RobotData.armMaxAngle);

		if (safeAngle != angle)
			insideLimits = false;
		if (limitless) {
			if (insideLimits)
				limitless = false;
			internalMoveTo(angle, sf);
			return angle;
		} else {
			internalMoveTo(safeAngle, sf);
			return safeAngle;
		}

	}

	private void internalMoveTo(double angle, int sf) {
		RobotData.armIdle = false;
		
		if(speedFactor != sf) {
			speedFactor = sf;
			armCV =(int) (maxArmCV * (sf/100.0));
			armTalon.configMotionCruiseVelocity(armCV, RobotData.armTimeoutMs);
			armTalon.configMotionAcceleration(armCV, RobotData.armTimeoutMs);
		}
		RobotData.armPositionClicks = degreesToClicks(angle);
		SmartDashboard.putNumber("MoveTo-Arm Pos-Degrees)", angle);
		SmartDashboard.putNumber("MoveTo-Arm Pos-Clicks)", RobotData.armPositionClicks);
		armTalon.set(ControlMode.MotionMagic, RobotData.armPositionClicks);
	}

	int degreesToClicks(double angle) {
		return (int) (angle * RobotData.armClicksPerUnit);
	}

	int clicksToDegrees(int pos) {
		return (int) (pos / RobotData.armClicksPerUnit);
	}

	public double getArmPosition() {
		return (armTalon.getSelectedSensorPosition(RobotData.armPIDLoopIdx) / RobotData.armClicksPerUnit);
	}

	public void resetArmPositon() {
		RobotData.armPositionTarget = 0;
	}

	public boolean isIdle() {
		SmartDashboard.putBoolean("Arm Idle", RobotData.armIdle);
		return RobotData.armIdle;
	}

	public void setPosition(double positionUnits) {
		RobotData.armPositionTarget = positionUnits;
	}

	public void incrementPosition(double increment) {
		RobotData.armPositionTarget += increment;
	}
	public void enableLimitless() {
		limitless = true;
	}
}
