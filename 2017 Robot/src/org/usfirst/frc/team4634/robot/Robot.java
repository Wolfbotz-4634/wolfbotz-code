package org.usfirst.frc.team4634.robot;
import edu.wpi.first.wpilibj.AnalogInput;
import edu.wpi.first.wpilibj.IterativeRobot;
import java.io.IOException;
import edu.wpi.first.wpilibj.RobotDrive;
import edu.wpi.first.wpilibj.command.Command;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.command.Scheduler;
import edu.wpi.first.wpilibj.livewindow.LiveWindow;
import org.usfirst.frc.team4634.robot.commands.ExampleCommand;
import org.usfirst.frc.team4634.robot.subsystems.ExampleSubsystem;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import java.util.concurrent.TimeUnit;
import org.opencv.core.Rect;
import org.opencv.imgproc.Imgproc;
import edu.wpi.cscore.UsbCamera;
import edu.wpi.first.wpilibj.CameraServer;
//import edu.wpi.first.wpilibj.vision.VisionRunner;
import edu.wpi.first.wpilibj.vision.VisionThread;
import com.ctre.CANTalon;
//import edu.wpi.first.wpilibj.AnalogInput;



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
	private static final int IMG_WIDTH = 320;
	private static final int IMG_HEIGHT = 240;
	
	private VisionThread visionThread;
	private double centerX = 0.0;
	private RobotDrive drive;
	
	private final Object imgLock = new Object();

    Command autonomousCommand;
    Boolean gearPlaced;
    RobotDrive myRobot;
    Timer timer;
    UltrasonicRangeFinder rangefinder;
    AnalogInput sensor;
    XboxController driveXbox;
    XboxController mechanismXbox;
    CANTalon leftMotor, rightMotor, middleMotor;
    boolean brakeYes;
    SendableChooser chooser;
    
    @SuppressWarnings("rawtypes")
    
	/**
     * This function is run when the robot is first started up and should be
     * used for any initialization code.
     */
    @Override
    public void robotInit() {
    	myRobot = new RobotDrive(0,1);
    	timer = new Timer();
		oi = new OI();
        chooser = new SendableChooser();
        gearPlaced = true;
        chooser.addDefault("Default Auto", new ExampleCommand());
        rangefinder = new RangeFinding(sensor);
        driveXbox = new XboxController(0);
        mechanismXbox = new XboxController(1);
        brakeYes = true;
        leftMotor = new CANTalon(1);
        rightMotor = new CANTalon(0);
        middleMotor = new CANTalon(2);
        leftMotor.setInverted(true);
        SmartDashboard.putData("Auto mode", chooser);        
        UsbCamera camera = CameraServer.getInstance().startAutomaticCapture();
        camera.setResolution(IMG_WIDTH, IMG_HEIGHT);
        
        visionThread = new VisionThread(camera, new Pipeline(), pipeline -> {
            if (!pipeline.filterContoursOutput().isEmpty()) {
                Rect r = Imgproc.boundingRect(pipeline.filterContoursOutput().get(0));
                synchronized (imgLock) {
                    centerX = r.x + (r.width / 2);
                }
            }
        });
        visionThread.start();
        /* Run GRIP in a new process */
        try {
            new ProcessBuilder("/home/lvuser/grip").inheritIO().start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void autonomousInit() { //currently configured for far right side start
        autonomousCommand = (Command) chooser.getSelected();    	
    	// schedule the autonomous command (example)
        if (autonomousCommand != null) autonomousCommand.start();        
        driveForTime(5.0);
        /*try {
    		TimeUnit.SECONDS.sleep(5); //use this if autonomousperiodic prevents driveForTime from completing
    	} catch(Exception InterruptedException) {
    		System.out.println("shit wtf");
    	}*/
        
        /* if middle start, remove all code in autonomousperiodic
    	driveUntilClose(); 
    	*/
    }

    public void autonomousPeriodic() { 
        Scheduler.getInstance().run();
        if (! gearPlaced) {
        	double centerX;
        	synchronized (imgLock) {
        		centerX = this.centerX;
        	}
        	double turn = centerX - (IMG_WIDTH / 2);
        	drive.arcadeDrive(0.6, turn * 0.005);
        }
    	if (rangefinder.getRange() < 10.0 && gearPlaced == false) {
    		gearPlaced = true;
    		myRobot.drive(0.0, 0.0);
    		unlock();
    		reverse(2.0);
    	}    	
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
        myRobot.arcadeDrive(throttle(), leftX, true);
        middleMotor.set(rightX);
        if (driveXbox.getRawButton(6)) {
        	leftMotor.enableBrakeMode(! brakeYes);
        	rightMotor.enableBrakeMode(! brakeYes);
        	System.out.println(brakeYes);
        	brakeYes = !brakeYes;
        	try {
        		TimeUnit.MILLISECONDS.sleep(200);
        	} catch(Exception InterruptedException) {
        		System.out.println("shit");
        	}
        }
        /*while (driveXbox.getLeftTrigger() > 0.0) { 
        	middleMotor.set(leftX);
        }
        myRobot.tankDrive(leftY, rightY);*/
    }

    //controls throttle: right trigger to go forward, left trigger to reverse
    public double throttle() {
        if (driveXbox.getRightTrigger() > 0.1) {
            return (driveXbox.getRightTrigger());
        } else if (driveXbox.getLeftTrigger() > 0.1){
            return(-driveXbox.getLeftTrigger());
        } else {
            return 0;
        }
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
    		myRobot.drive(0.0, 0.0);
    	}
    	timer.reset();
        timer.start();
    	while (timer.get() < time) {
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

    //drives forward until the robot is within 10 inches of the object in front of it
    public void driveUntilClose(double range) {
     while (rangefinder.getRange() > 10.0) {
    	 myRobot.drive(0.75, 0.0);
     }
     myRobot.drive(0.0, 0.0);
    }
      
    public void disabledInit(){    	  
    }	
  	public void disabledPeriodic() {
  		Scheduler.getInstance().run();
  	}

    public void testPeriodic() {
        LiveWindow.run();
    }
}
