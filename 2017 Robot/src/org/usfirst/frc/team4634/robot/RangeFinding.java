package org.usfirst.frc.team4634.robot;

import org.usfirst.frc.team4634.robot.UltrasonicRangeFinder;

import edu.wpi.first.wpilibj.AnalogInput;

public class RangeFinding extends UltrasonicRangeFinder {
  public static final double voltsPerInch = 5.0/31.5; //800mm = 31.5 inches

  public RangeFinding(AnalogInput device) {
    super(device, voltsPerInch);
  }
}