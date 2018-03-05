package org.usfirst.frc.team1002.robot;

import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.Faults;
import com.ctre.phoenix.motorcontrol.FeedbackDevice;
import com.ctre.phoenix.motorcontrol.StatusFrameEnhanced;
import com.ctre.phoenix.motorcontrol.can.TalonSRX;

import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

public class RobotArm {
	TalonSRX armTalon;
	// Faults f;

	public RobotArm() {
		armTalon = new TalonSRX(RobotData.armTalonPort);
	}

	public void init() {
		talonConfig(armTalon);
		armTalon.setSelectedSensorPosition(-3455, RobotData.armPIDLoopIdx, RobotData.armTimeoutMs);
		// f = new Faults();
	}

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
		/* set acceleration and vcruise velocity - see documentation */
		thisTalon.configMotionCruiseVelocity(10000, RobotData.armTimeoutMs);
		thisTalon.configMotionAcceleration(10000, RobotData.armTimeoutMs);
		/* zero the sensor */
		thisTalon.setSelectedSensorPosition(0, RobotData.armPIDLoopIdx, RobotData.armTimeoutMs);

		thisTalon.configMotionCruiseVelocity((int) RobotData.armCruiseVel, RobotData.armTimeoutMs);
		thisTalon.configMotionAcceleration((int) RobotData.armCruiseAccel, RobotData.armTimeoutMs);

	}

	void displayArmStatus() {
		SmartDashboard.putNumber("Arm Desired Angle", RobotData.armPositionTarget);
		SmartDashboard.putNumber("Arm desired encoder count", RobotData.armPositionClicks);
	}

	/**
	 * 
	 */
	public void checkStatus() {
		int armPos = armTalon.getSelectedSensorPosition(RobotData.armPIDLoopIdx);
		if (Math.abs(armPos - RobotData.armPositionClicks) <= 0.3) {
			RobotData.armIdle = true;
		} else {
			RobotData.armIdle = false;
		}
		/*if(Robot.elev.getElevatorPositionUnits() < 10.0) {
			RobotData.armPositionDegrees = Math.max(0,  RobotData.armPositionDegrees);
		}
		*/
		SmartDashboard.putNumber("ChkStat Arm Pos-Clicks", armPos);
		SmartDashboard.putNumber("ChkStat Arm Pos-Degrees", clicksToDegrees(armPos));
	}

	public void moveTo(double angle) {
		// armTalon.getFaults(f);
		// if (f.ForwardLimitSwitch) {
		// what do you want to put here
		// }
		// if (f.ReverseLimitSwitch) {
		// what do you want to put here
		// }

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
		return RobotData.armIdle;
	}

	public void setPosition(double positionUnits) {
		RobotData.armPositionTarget = positionUnits;
	}

	public void incrementPosition(double increment) {
		RobotData.armPositionTarget += increment;
	}
}
