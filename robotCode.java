package org.usfirst.frc.team4634.robot;
import com.ni.vision.NIVision;
import com.ni.vision.NIVision.DrawMode;
import com.ni.vision.NIVision.Image;
import com.ni.vision.NIVision.ShapeMode;
import edu.wpi.first.wpilibj.Victor;
import edu.wpi.first.wpilibj.CANTalon;
import edu.wpi.first.wpilibj.CameraServer;
import edu.wpi.first.wpilibj.IterativeRobot;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.RobotDrive;
import edu.wpi.first.wpilibj.livewindow.LiveWindow;
import edu.wpi.first.wpilibj.vision.USBCamera;
import edu.wpi.first.wpilibj.Talon;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.VictorSP;


/**
 * The VM is configured to automatically run this class, and to call the
 * functions corresponding to each mode, as described in the IterativeRobot
 * documentation. If you change the name of this class or the package after
 * creating this project, you must also update the manifest file in the resource
 * directory.
 */
public class Robot extends IterativeRobot {
	
	RobotDrive myRobot;
	Joystick stick, stick2;
	CANTalon armLeft, armRight;
	VictorSP intakeFront;
	//Victor armLeft, armRight;
    CameraServer server;

	
	int autoLoopCounter, session;
	//Image frame;
    /**
     * This function is run when the robot is first started up and should be
     * used for any initialization code.
     */
    public void robotInit() { //BLACK WIRE ON INSIDE OF MOTOR CONTROLLERS
    	myRobot = new RobotDrive(0,1,2,3);
    	stick = new Joystick(0);
    	intakeFront = new VictorSP(4);
    	//armLeft = new Victor(5);
    	//armRight = new Victor(6);
    	armLeft = new CANTalon(3);
    	armRight = new CANTalon(2);
    	stick2 = new Joystick(1);
    }
    
   
/*    public void operatorControl() { 
    	
    //	myRobot.setSafetyEnabled(true);       
        while (isOperatorControl() && isEnabled()) {

       //      robot code here! 
            Timer.delay(0.005);		// wait for a motor update time
        }
    }
    
    
    /**
     * This function is run once each time the robot enters autonomous mode
     */
    public void autonomousInit() {
    	autoLoopCounter = 0;
    	
    }

    /**
     * This function is called periodically during autonomous
     */
    public void autonomousPeriodic() {
    	//myRobot.setSafetyEnabled(true);
    	
    	if(autoLoopCounter < 100) //Check if we've completed 100 loops (approximately 1 seconds)
		{
			myRobot.drive(.75, 0.0); 	// drive forward 1 seconds
			autoLoopCounter++;
		}
    	/**if (autoLoopCounter > 100 && autoLoopCounter < 200) // reverse for 1 seconds
    	{
    		myRobot.drive(-.75, 0.0);
    		autoLoopCounter++;
    	}
    
    	if (autoLoopCounter > 200 &&  autoLoopCounter < 300) //move forward second time 1
		{
			myRobot.drive(.75, 0.0);
			autoLoopCounter++; 	// drive forwards .75,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,, speed
		}
    	*/
    	else
    	{
    		myRobot.drive(0.0, 0.0); 	// stop robot
		}
    	
    }

    /**
     * This function is called once each time the robot enters tele-operated mode
     */
    public void teleopInit(){
    	server = CameraServer.getInstance();
    	server.setQuality(50);
        //the camera name (ex "cam0") can be found through the roborio web interface
        server.startAutomaticCapture("cam0");   
    }

    /**
     * This function is called periodically during operator control
     */
    public void teleopPeriodic() {
    	
	///////////////////////////////////////////////////////////////////////////////////
	//////////////////////////Put any robot commands in here///////////////////////////
	///////////////////////////////////////////////////////////////////////////////////    	
        myRobot.setSafetyEnabled(true);
        while(isOperatorControl()&&isEnabled()){
    	myRobot.arcadeDrive(-stick.getY(),-stick.getX());
        System.out.println("drive");
        intake();      
        armRight();
        armLeft();
     //   operatorControl(); // if this is enabled, we cant drive but we can use camera
        }
    }
    
    /**
     * This function is called periodically during test mode
     
    public void testPeriodic() {
    	LiveWindow.run(); //keep this so webcam view will show up
    }
    */
    
    /////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////Robot controller commands///////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////
  
    public void intake()//front wheel
{
		  myRobot.arcadeDrive(-stick.getY(),-stick.getX());
		  while(stick.getRawButton(4) || stick2.getRawButton(1))// 1, shoot ball
	    	{
	    	intakeFront.set(1);
			  
	  	  myRobot.arcadeDrive(-stick.getY(),-stick.getX());
	  	 
	    	}
		  while(stick.getRawButton(1) || stick2.getRawButton(2))// 4, bring in ball
	    	{
	    	intakeFront.set(-1);
	  	  myRobot.arcadeDrive(-stick.getY(),-stick.getX());
	  	
	    	}
		  	  
	        intakeFront.set(0.00);
    System.out.println("intake");
    
}

	public void armLeft(){ //Needs to be the same except "set()" which would be inverted for armRight
	
		myRobot.arcadeDrive(-stick.getY(), -stick.getX());
		if(stick.getRawButton(5) || stick2.getRawButton(6))//bring arm up (using left trigger)
		{ 
			armLeft.set(-0.25);
		}
		else if(stick.getRawButton(6)|| stick2.getRawButton(7))//bring arm down (using right trigger)
		{
			armLeft.set(0.75);
		}
		else
		{
			armRight.set(0);
			armLeft.set(0);
		}
	//	armLeft.set(0);
	}

	public void armRight(){ //Needs to be the same except "set()" which would be inverted for armLeft
		
		myRobot.arcadeDrive(-stick.getY(), -stick.getX());
		if(stick.getRawButton(5)|| stick2.getRawButton(6))//bring arm up (using left trigger)
		{ 
			armRight.set(-0.25);
		}		
		else if(stick.getRawButton(6)|| stick2.getRawButton(7))//bring arm down (using right trigger)
		{
			armRight.set(0.75);
		}
		else
		{
			armRight.set(0);
			armLeft.set(0);
		}
		}
//	armRight.set(0);
	}