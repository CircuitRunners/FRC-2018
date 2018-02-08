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
	public int activeCamera = 0; /* 0 is none, -1 is rev, 1 is fwd */
	int switchCount = 0;
	double frameDelay = 0.05;
	int frameNumber = 0;
	public UsbCamera camSvr;
	public CvSink cvSink;

	public int xRes = 640;
	public int yRes = 480;
	static int simpleMode = 1;

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
		if (newCamera == activeCamera)
			return;
		if (newCamera == 1) { /* Forward */
			activecvSink = fwdcvSink;
		} else if (newCamera == -1) { /* Reverse */
			activecvSink = rvscvSink;
		}
		activeCamera = newCamera;
	}

	/* CameraThread */
	protected void cameraOperation() {
		int frameNumber = 0;
		
		/* Init the camera sinks */
		fwdCamSvr = CameraServer.getInstance().startAutomaticCapture("fwdCam", 0);
		fwdCamSvr.setResolution(xRes, yRes);
		fwdcvSink = CameraServer.getInstance().getVideo(fwdCamSvr); /* init capture conn */

		rvsCamSvr = CameraServer.getInstance().startAutomaticCapture("rvsCam", 1);
		rvsCamSvr.setResolution(xRes, yRes);
		rvscvSink = CameraServer.getInstance().getVideo(rvsCamSvr); /* init capture conn */

		activecvSink = fwdcvSink;
		activeCamera = 1;
		/* turn on forward camera */

		setActiveCamera(1);

		/* init dashboard connection */
		CvSource outputStream = CameraServer.getInstance().putVideo("DispWin", xRes, yRes);

		Mat camMat = new Mat();

		while (!Thread.interrupted()) {
			/* Get Frame, return error if req, exit loop */
			if (activecvSink.grabFrame(camMat) == 0) {
				outputStream.notifyError("Line 147" + fwdcvSink.getError());
				continue;
			}
			SmartDashboard.putNumber("frameNumber", frameNumber++);
			/* Do something so you know the frames are changing */
			// radius = (activeCamera == 1) ? switchCount + 40 : 140 - switchCount;
			// Imgproc.circle(camMat, new Point(xRes / 2, yRes / 2), radius,
			// new Scalar(230 - switchCount, 150 + switchCount, 225 - switchCount), 5);
			outputStream.putFrame(camMat); /* post image */

			/* Camera switching */

			if (Robot.driver.getStartButton()) {
				if (activeCamera == 1)
					setActiveCamera(-1);
				else
					setActiveCamera(1);
				SmartDashboard.putNumber("Camera", activeCamera);
			}

			Timer.delay(frameDelay); /* wait for a bit */
		}
		/* Cleanup of camera connections should go here */
	}

}