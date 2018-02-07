package org.usfirst.frc.team1002.robot;

import edu.wpi.first.wpilibj.SpeedController;
import edu.wpi.first.wpilibj.VictorSP;

public class RobotMap {
	public static final SpeedController kFrontRight = new VictorSP(7);
	public static final SpeedController kBackRight = new VictorSP(9);  //Motor Controllers for drivebase
	public static final SpeedController kFrontLeft = new VictorSP(8);
	public static final SpeedController kBackLeft = new VictorSP(6);
	
	public static final int driverPort = 0;
	
	public static final double elevP = 1;
	public static final double elevI = 0.1;
	public static final double elevD = 0.0;
	public static final double elevF = 0.0;
}
