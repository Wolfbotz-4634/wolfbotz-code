package org.usfirst.frc.team4634.robot;

import edu.wpi.first.wpilibj.IterativeRobot;
import edu.wpi.first.wpilibj.command.Command;
import edu.wpi.first.wpilibj.command.Scheduler;
import edu.wpi.first.wpilibj.livewindow.LiveWindow;
import org.usfirst.frc.team4634.robot.commands.ExampleCommand;
import org.usfirst.frc.team4634.robot.subsystems.ExampleSubsystem;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj.RobotDrive;
import edu.wpi.first.wpilibj.Victor;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.Talon;


/**
 * The VM is configured to automatically run this class, and to call the
 * functions corresponding to each mode, as described in the IterativeRobot
 * documentation. If you change the name of this class or the package after
 * creating this project, you must also update the manifest file in the resource
 * directory.
 */
public class Robot extends IterativeRobot {

	public static final ExampleSubsystem exampleSubsystem = new ExampleSubsystem();
	public static OI oi;
	public static Joystick xbox;
	public static RobotDrive myRobot;
	public static double leftX, leftY, rightX, rightY;
	Victor driveLeft, driveRight, shootingMech;
	Talon armForward, armBackward;

    Command autonomousCommand;
    SendableChooser chooser;

    /**
     * This function is run when the robot is first started up and should be
     * used for any initialization code.
     */
    public void robotInit() {
		oi = new OI();
        chooser = new SendableChooser();
        xbox = new Joystick(1);
        myRobot = new RobotDrive(driveLeft, driveRight);
        chooser.addDefault("Default Auto", new ExampleCommand());
        driveLeft = new Victor(0);
        driveRight = new Victor(1);
        shootingMech = new Victor(2);
		armForward = new Talon(3);
        armBackward = new Talon(4);

        
//        chooser.addObject("My Auto", new MyAutoCommand());
        SmartDashboard.putData("Auto mode", chooser);
    }
    
	/**
     * This function is called once each time the robot enters Disabled mode.
     * You can use it to reset any subsystem information you want to clear when
	 * the robot is disabled.
     */
    public void disabledInit(){

    }
	
	public void disabledPeriodic() {
		Scheduler.getInstance().run();
	}

	/**
	 * This autonomous (along with the chooser code above) shows how to select between different autonomous modes
	 * using the dashboard. The sendable chooser code works with the Java SmartDashboard. If you prefer the LabVIEW
	 * Dashboard, remove all of the chooser code and uncomment the getString code to get the auto name from the text box
	 * below the Gyro
	 *
	 * You can add additional auto modes by adding additional commands to the chooser code above (like the commented example)
	 * or additional comparisons to the switch structure below with additional strings & commands.
	 */
    public void autonomousInit() {
        autonomousCommand = (Command) chooser.getSelected();
        
		/* String autoSelected = SmartDashboard.getString("Auto Selector", "Default");
		switch(autoSelected) {
		case "My Auto":
			autonomousCommand = new MyAutoCommand();
			break;
		case "Default Auto":
		default:
			autonomousCommand = new ExampleCommand();
			break;
		} */
    	
    	// schedule the autonomous command (example)
        if (autonomousCommand != null) autonomousCommand.start();
    }

    /**
     * This function is called periodically during autonomous
     */
    public void autonomousPeriodic() {
        Scheduler.getInstance().run();
    }

    public void teleopInit() {
		// This makes sure that the autonomous stops running when
        // teleop starts running. If you want the autonomous to 
        // continue until interrupted by another command, remove
        // this line or comment it out.
        if (autonomousCommand != null) autonomousCommand.cancel();
    }

    /**
     * This function is called periodically during operator control
     */
    public void teleopPeriodic() {        
        leftX = xbox.getRawAxis(1);
        leftY = xbox.getRawAxis(2);
        rightX = xbox.getRawAxis(4);
        rightY = xbox.getRawAxis(5);
        myRobot.tankDrive(leftY, rightY);
        armForward();
        armBackward();
        shoot();
    }
    
    public void armForward() {
    	myRobot.tankDrive(leftY, rightY);
		  while(xbox.getRawButton(6)) {
			  armForward.set(0.5);
			  myRobot.tankDrive(leftY, rightY);
		  }
		  armForward.set(0);
	}
    public void armBackward() {
    	myRobot.tankDrive(leftY, rightY);
		  while(xbox.getRawButton(5)) {
			  armBackward.set(-0.5);
			  myRobot.tankDrive(leftY, rightY);
		  }
		  armBackward.set(0);
	}
    public void shoot() {
    	myRobot.tankDrive(leftY, rightY);
		  while(xbox.getRawButton(1)) {
			  myRobot.tankDrive(leftY, rightY);
			  shootingMech.set(1);
			  Timer.delay(1);
			  shootingMech.set(0);
		  }
	}
    
    /**
     * This function is called periodically during test mode
     */
    public void testPeriodic() {
        LiveWindow.run();
    }
}
