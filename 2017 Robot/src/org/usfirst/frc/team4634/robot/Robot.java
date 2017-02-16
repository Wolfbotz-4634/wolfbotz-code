package org.usfirst.frc.team4634.robot;
import edu.wpi.first.wpilibj.AnalogInput;
import edu.wpi.first.wpilibj.IterativeRobot;
import java.io.IOException;
import edu.wpi.first.wpilibj.RobotDrive;
import edu.wpi.first.wpilibj.command.Command;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.VictorSP;
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
//import edu.wpi.first.wpilibj.Solenoid;
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
	public static OI oi;
	public static final ExampleSubsystem exampleSubsystem = new ExampleSubsystem();
	
	//vars for vision processing
	private static final int IMG_WIDTH = 320;
	private static final int IMG_HEIGHT = 240;	
	private VisionThread visionThread;
	private double centerX = 0.0;	
	private final Object imgLock = new Object();
	Boolean gearPlaced;
	
	//Solenoid gearSolenoid;
    Command autonomousCommand;
    RobotDrive myRobot; //the robot's driving functionality
    Timer timer; //a timer that counts in seconds
    UltrasonicRangeFinder rangefinder; //the ultrasonic rangefinder, tells you how far the nearest object in front is
    AnalogInput sensor; //the analog input of the rangefinder
    XboxController driveXbox; //driver's controller
    XboxController mechanismXbox; //mechanism control person's controller
    VictorSP leftRear, leftFront, rightRear, rightFront;
    CANTalon middleMotor, intakeMotor; //middle strafing motor
    boolean brakeYes;
    SendableChooser chooser;
    
    @SuppressWarnings("rawtypes")
    
	/**
     * This function is run when the robot is first started up and should be
     * used for any initialization code.
     */
    @Override
    public void robotInit() {
    	chooser.addDefault("Default Auto", new ExampleCommand());
    	chooser = new SendableChooser();
    	oi = new OI();
    	
    	myRobot = new RobotDrive(0,1,2,3);
    	timer = new Timer();        
        gearPlaced = true;
        rangefinder = new RangeFinding(sensor);
        driveXbox = new XboxController(0);
        mechanismXbox = new XboxController(1);
        brakeYes = true;
        middleMotor = new CANTalon(4);
        //gearSolenoid = new Solenoid(1);
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
        driveForTime(5.0, 0.75);
        Timer turningTimer = new Timer();
        turningTimer.reset();
        turningTimer.start();
        while (turningTimer.get() < 2.0) {
        	myRobot.arcadeDrive(0.0, -1.0);
        }
        
        /* if middle start, remove all code in autonomousperiodic pertaining to alignment, just use:
    	while (rangefinder.getRange() > 10.0) {
    		myRobot.drive(0.75, 0.0);
     	}
     	myRobot.drive(0.0, 0.0);
    	*/
    }

    public void autonomousPeriodic() { 
        Scheduler.getInstance().run();
        /*this gets the centerX pixel value of the target and subtracts half the image width to change
         *  it to a value that is zero when the rectangle is centered in the image and positive or negative when
         *   the target center is on the left or right side of the frame. That value is used to steer the robot 
         *   towards the target.*/
        if (! gearPlaced) {
        	double centerX;
        	synchronized (imgLock) {
        		centerX = this.centerX;
        	}
        	double turn = centerX - (IMG_WIDTH / 2);
        	myRobot.arcadeDrive(0.6, turn * 0.005);
        }
    	if (rangefinder.getRange() < 10.0 && gearPlaced == false) {
    		gearPlaced = true;
    		driveForTime(2.0, 0.0);
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
        intake();
        if (driveXbox.getRawButton(6)) {
        	System.out.println(brakeYes);
        	brakeYes = !brakeYes;
        	try {
        		TimeUnit.MILLISECONDS.sleep(200);
        	} catch(Exception InterruptedException) {
        		System.out.println("shit");
        	}
        }
        SmartDashboard.putNumber("Distance", rangefinder.getRange());
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
    
    public void intake() {
    	if (driveXbox.getRawButton(1)) {
    		intakeMotor.set(1.0);
        }
    }

    //drives forward for a specified amount of time
    public void driveForTime(double time, double speed) {
    	if (rangefinder.getRange() < 10.0) {
    		myRobot.drive(0.0, 0.0);
    	}
    	timer.reset();
        timer.start();
    	while (timer.get() < time) {
    		myRobot.drive(speed, 0.0);
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
      
    public void disabledInit(){    	  
    }	
  	public void disabledPeriodic() {
  		Scheduler.getInstance().run();
  	}

    public void testPeriodic() {
        LiveWindow.run();
    }
}
