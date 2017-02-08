package org.usfirst.frc.team4634.robot;
import edu.wpi.first.wpilibj.AnalogInput;
import edu.wpi.first.wpilibj.IterativeRobot;
import java.io.IOException;
import edu.wpi.first.wpilibj.networktables.NetworkTable;
import edu.wpi.first.wpilibj.RobotDrive;
import edu.wpi.first.wpilibj.command.Command;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.command.Scheduler;
import edu.wpi.first.wpilibj.livewindow.LiveWindow;
import org.usfirst.frc.team4634.robot.commands.ExampleCommand;
import org.usfirst.frc.team4634.robot.subsystems.ExampleSubsystem;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
//import edu.wpi.first.wpilibj.AnalogInput;
//import com.ctre.CANTalon;


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
	private final NetworkTable grip = NetworkTable.getTable("grip");

    Command autonomousCommand;
    SendableChooser chooser;
    RobotDrive myRobot;
    Timer timer;
    UltrasonicRangeFinder rangefinder;
    AnalogInput sensor;
    XboxController driveXbox;
    XboxController mechanismXbox;
    /*CANTalon frontLeftMotor, frontRightMotor, rearLeftMotor, rearRightMotor;   */

    @SuppressWarnings("rawtypes")
	/**
     * This function is run when the robot is first started up and should be
     * used for any initialization code.
     */
    @Override
    public void robotInit() {
    	myRobot = new RobotDrive(1, 2, 3, 4);
    	timer = new Timer();
		oi = new OI();
        chooser = new SendableChooser();
        chooser.addDefault("Default Auto", new ExampleCommand());
        rangefinder = new RangeFinding(sensor);
        driveXbox = new XboxController(0);
        mechanismXbox = new XboxController(1);
        
        /*frontLeftMotor = new CANTalon(1);
        frontRightMotor = new CANTalon(2);
        rearLeftMotor = new CANTalon(3);
        rearRightMotor = new CANTalon(4);
        
        frontLeftMotor.setInverted(true);
        rearLeftMotor.setInverted(true);*/
        
        SmartDashboard.putData("Auto mode", chooser);
        /* Run GRIP in a new process */
        try {
            new ProcessBuilder("/home/lvuser/grip").inheritIO().start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }	
	
    public void disabledInit(){

    }	
	public void disabledPeriodic() {
		Scheduler.getInstance().run();
	}

    public void autonomousInit() {
        autonomousCommand = (Command) chooser.getSelected();
    	
    	// schedule the autonomous command (example)
        if (autonomousCommand != null) autonomousCommand.start();
        
        driveForTime(5.0);
    }

    public void autonomousPeriodic() {
        Scheduler.getInstance().run();
        /* Get published values from GRIP using NetworkTables */
        for (double area : grip.getNumberArray("targets/area", new double[0])) {
            System.out.println("Got contour with area=" + area);
        }
        rangefinder.getRange();
    }

    public void teleopInit() {
		// This makes sure that the autonomous stops running when
        // teleop starts running. If you want the autonomous to 
        // continue until interrupted by another command, remove
        // this line or comment it out.
        if (autonomousCommand != null) autonomousCommand.cancel();
    }

    public void teleopPeriodic() {
        Scheduler.getInstance().run();
        double rightX = driveXbox.getX2();
        double leftX = driveXbox.getX1();
        //double rightY = driveXbox.getY2();
        double leftY = driveXbox.getY1();
        
        myRobot.mecanumDrive_Cartesian(leftX, leftY, rightX, 0);
    }

    public void unlock() {
        //unlocks mechanism for gears
    }

    public void lock() {
        //locks mechanism for gears
    }

    //drives forward for a specified amount of time
    public void driveForTime(double time) {
    	if (rangefinder.getRange() < 10.0) {
    		stop();
    	}
    	timer.reset();
        timer.start();
    	while (timer.get() < time) {
    		myRobot.drive(0.75, 0.0);
    	}
    }
    
    public void driveUntilClose(double range) {
    	while (rangefinder.getRange() > 10.0) {
    		myRobot.drive(0.75, 0.0);
    	}
    }
    
    //drives in reverse for a specified amount of time
    public void reverse(double time) {
    	timer.reset();
        timer.start();
    	while (timer.get() < time) {
    		myRobot.drive(-0.75, 0.0);
    	}
    }
    
    public void stop() {
    	myRobot.drive(0.0, 0.0);
    }

    public void testPeriodic() {
        LiveWindow.run();
    }
}
