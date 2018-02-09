package org.usfirst.frc.team1002.robot;

import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.cscore.CvSink;
import edu.wpi.cscore.CvSource;
import edu.wpi.cscore.UsbCamera;
import edu.wpi.first.wpilibj.CameraServer;

import org.opencv.core.Mat;

public class CameraControl {
	public UsbCamera fwdCamSvr;
	public UsbCamera rvsCamSvr;
	public CvSink fwdcvSink;
	public CvSink rvscvSink;
	public CvSink activecvSink;
	//int switchCount = 0;
	public UsbCamera camSvr;
	public CvSink cvSink;

	public void cameraInit() {
		Thread cameraOpThread = new Thread(new Runnable() {
			public void run() {
				cameraOperation();
			}
		});
		cameraOpThread.setName("CamOps Thread");
		cameraOpThread.start();
	}

	public void cameraShutdown() {
	}

	protected void setActiveCamera(int newCamera) {
		if (newCamera == RobotData.camActiveCamera)
			return;
		if (newCamera == 1) { /* Forward */
			activecvSink = fwdcvSink;
		} else if (newCamera == -1) { /* Reverse */
			activecvSink = rvscvSink;
		}
		RobotData.camActiveCamera = newCamera;
	}

	/* CameraThread */
	protected void cameraOperation() {
		RobotData.camFrameNumber = 0;
		
		/* Init the camera sinks */
		fwdCamSvr = CameraServer.getInstance().startAutomaticCapture("fwdCam", 0);
		fwdCamSvr.setResolution(RobotData.camXRes, RobotData.camYRes);
		fwdcvSink = CameraServer.getInstance().getVideo(fwdCamSvr); /* init capture conn */

		rvsCamSvr = CameraServer.getInstance().startAutomaticCapture("rvsCam", 1);
		rvsCamSvr.setResolution(RobotData.camXRes, RobotData.camYRes);
		rvscvSink = CameraServer.getInstance().getVideo(rvsCamSvr); /* init capture conn */

		activecvSink = fwdcvSink;
		RobotData.camActiveCamera = 1;
		/* turn on forward camera */

		setActiveCamera(1);

		/* init dashboard connection */
		CvSource outputStream = CameraServer.getInstance().putVideo("DispWin", RobotData.camXRes, RobotData.camYRes);

		Mat camMat = new Mat();

		while (!Thread.interrupted()) {
			/* Get Frame, return error if req, exit loop */
			if (activecvSink.grabFrame(camMat) == 0) {
				outputStream.notifyError("Line 147" + fwdcvSink.getError());
				continue;
			}
			SmartDashboard.putNumber("frameNumber", RobotData.camFrameNumber++);
			/* Do something so you know the frames are changing */
			// radius = (RobotData.activeCamera == 1) ? switchCount + 40 : 140 - switchCount;
			// Imgproc.circle(camMat, new Point(xRes / 2, yRes / 2), radius,
			// new Scalar(230 - switchCount, 150 + switchCount, 225 - switchCount), 5);
			outputStream.putFrame(camMat); /* post image */

			/* Camera switching */

			if (Robot.driver.getStartButton()) {
				if (RobotData.camActiveCamera == 1)
					setActiveCamera(-1);
				else
					setActiveCamera(1);
				SmartDashboard.putNumber("Camera", RobotData.camActiveCamera);
			}

			Timer.delay(RobotData.camFrameDelay); /* wait for a bit */
		}
		/* Cleanup of camera connections should go here */
	}

}