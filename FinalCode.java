//FINAL CODE THAT WE RAN ON DEMO DAY

import lejos.robotics.navigation.*;
import lejos.utility.Delay;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.port.MotorPort;
import lejos.robotics.Color;
import lejos.robotics.chassis.Chassis;
import lejos.robotics.chassis.Wheel;
import lejos.robotics.chassis.WheeledChassis;

import java.util.Stack;

import lejos.hardware.Button;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.sensor.EV3IRSensor;
import lejos.hardware.sensor.EV3TouchSensor;
import lejos.hardware.sensor.SensorMode;
import lejos.hardware.port.SensorPort;

public class FinalCode{
    static HSVClass colorSensor = new HSVClass(SensorPort.S3);
    static EV3IRSensor irSensor = new EV3IRSensor(SensorPort.S1);
    static EV3TouchSensor touchSensor = new EV3TouchSensor(SensorPort.S4);
    
    
    //Declare important variables
    final static int PRIMARY_VALUE = colorSensor.getColorID();
    static SensorMode forward_distance, right_distance, left_distance;
    static float[] forward_value, right_value, left_value;
    static int RIGHT_ANGLE = 55;
    static Stack<Integer> navBot = new Stack<Integer>();
    
    //create motors, wheels, and the object that controls them
    static EV3LargeRegulatedMotor  LEFT_MOTOR  =  new  EV3LargeRegulatedMotor(MotorPort.C);
    static EV3LargeRegulatedMotor  RIGHT_MOTOR  =  new  EV3LargeRegulatedMotor(MotorPort.A);
    static Wheel wheel1 = WheeledChassis.modelWheel(LEFT_MOTOR , 2.0).offset(-7.5);
    static Wheel wheel2 = WheeledChassis.modelWheel(RIGHT_MOTOR , 2.0).offset(7.5);
    static Chassis chassis = new WheeledChassis(new  Wheel[] { wheel1, wheel2 }, WheeledChassis.TYPE_DIFFERENTIAL);
    static MovePilot pilot = new MovePilot(chassis);
    static boolean endpoint = false;
   
    public static void setEndpoint(boolean x)
    {
    		endpoint = x;
    }
    
    public static void main(String[] args) {
        Button.waitForAnyPress();
    
        
        do {
            moveRobot();
            if(endpoint==true)
            		break;
            navBot = navigateRobot2(blueResponse());
        } while(endpoint == false);
        
       System.out.println("endpoint reached");
        goBack(navBot);
        System.out.println("back to the starting point");
    }
    
    public static int moveRobot() {
    	//First get the HSV color value of whats the robot is on. if the color is not blue check the ambient light value to see it's on silver. If it's silver stop
        int i = 0;
        System.out.println("MOVING ROBOT");
        pilot.setAngularSpeed(10);
        pilot.setLinearSpeed(15);
        
        while(getHSVColor()<150) { //if its less than 150 its not blue
        		if(getRedColor() == 1.0) //1 is the value for silver so if the color reads silver call the setEndPoint Method
        		{
        			setEndpoint(true);
        			return i;
        		}
            if(getHSVColor()<=40 && getHSVColor()>=20) //if its on wood recenter 
                {
                    recenter();
                }
            pilot.travel(1.00); // if the robot isnt on wood, silver, or blue, travel forward 
            
            float [] samplevalue =  new float [touchSensor.sampleSize()];  
            touchSensor.fetchSample(samplevalue,0);
            //System.out.println("value " +samplevalue[0]);
            if(samplevalue[0]==1)
            {
                pilot.travel(-1);
            		if(getRedColor() == 1.0)
                {
                		setEndpoint(true);
                		return i;
                }	
            		moveBackward(navBot);									
                return i;   
            }
        }
	    //Once a blue intersection is hit travel forward a little bit and the stop and get out of the method
        if(getHSVColor()>150) {
            pilot.travel(1.6);
            pilot.stop();
            return i;
        }
        return i;
    }
    
    public static void moveBackward(Stack<Integer> navBot) { //Allows the robot to handle deadends
        System.out.println("Moving robot backwards");
        pilot.setAngularSpeed(10);
        pilot.setLinearSpeed(5);
       // pilot.travel(-1.3);
        pilot.rotate(-138);
        if(navBot.empty() == false) {
            navBot.pop();
        }
        while(colorSensor.getColorID() != 2) {
            recenter();
            pilot.travel(1);
        }
    }
    
    public static void recenter() { //Recentering the robot method 
        System.out.println("Recentering Robot");
        pilot.setAngularSpeed(10);
        
        double currentAngle = 2.5;
        
        
        while(true) {
            if(getHSVColor()<=60 || getHSVColor()>=90) {    // if the light is not on black
                pilot.rotate(currentAngle);
                currentAngle *= -1.5; //Allows the robot to sweep the ground searching for the black line. The -1.5 factor allows the robot to sweep both directions and and expand its angle
                if(getHSVColor()>=60 && getHSVColor()<=90) { //light is on black
                    break;
                }
            } else {
                break;
            }
        }
        
    }
    
    public static int[] blueResponse() { //Response method that executes when the robot reaches blue intersections
        System.out.println("Blue Response Method");
        pilot.setAngularSpeed(10);
        int[] optionNums = {0, 0, 0};
        
        
        //Figure if an object is in front of the robot
        forward_distance = irSensor.getDistanceMode();
        float [] forward_value =  new float[forward_distance.sampleSize()];
        forward_distance.fetchSample(forward_value, 0);
        System.out.println("Forward value is "+ forward_value[0]);
        if(forward_value[0] > 20) {
            optionNums[0] = 1;
        }
        pilot.rotate(RIGHT_ANGLE);
        pilot.stop();
        //recenter();
        
        //Figure out if an object is to the right of robot
        right_distance = irSensor.getDistanceMode();
        float [] right_value = new float[right_distance.sampleSize()];
        right_distance.fetchSample(right_value, 0);
        System.out.println("right value is"+ right_value[0]);
        if(right_value[0] > 20) {
            optionNums[1] = 1;
            System.out.println("returning [1] = 1");
            return optionNums;
        }
        return optionNums; //return the array containing the avaliable options
        // pilot.rotate(138);
        //pilot.stop();
        
        //Figure out if an object is to the left of the robot
        //          left_distance = irSensor.getDistanceMode();
        //          float [] left_value = new float[left_distance.sampleSize()];
        //          left_distance.fetchSample(left_value, 0);
        //          System.out.println("left value is"+ left_value[0]);
        //          if(left_value[0] > 20) {
        //              optionNums[2] = 1;
        //          }
        
        
        //          pilot.rotate(-138);
        //          pilot.stop();
        //          return optionNums;
    }
    

    
    public static Stack<Integer> navigateRobot2(int[] optionNums) {
        if(optionNums[1] == 1)  { // if the robot can turn right now it should
            System.out.println("turning right");
            pilot.travel(2.00);
            navBot.push(1);
            System.out.println("pushing 1 to the stack");
        } else if(optionNums[0] == 1) { //If the robot can turn go straight this is the second option 
            System.out.println("going straight");
            pilot.rotate(-71);
            pilot.travel(2.00);
            navBot.push(2);
            System.out.println("pushing 2 to the stack");
            //moveRobot();
        } else { //Turn left if it can't do anything else
            pilot.rotate(-71 * 2);
            pilot.travel(2.00);
            navBot.push(3);
            System.out.println("pushing 3 to the stack");
            //moveRobot();
        }
        return navBot;
        
    }
    
    public static Boolean endpointReached() {
        boolean endpoint;
        float [] samplevalue =  new float [touchSensor.sampleSize()];
        touchSensor.fetchSample(samplevalue,0);
        System.out.println("value " +samplevalue[0]);
        if(samplevalue[0]==1) {
        		endpoint = true;
        } else {
        		endpoint = false;
        }
        return endpoint;
    }
    
    public static void goBack(Stack<Integer> navBot) //Method that contains the stack and allows the robot to go back 
    {
    		pilot.travel(-1);
    		pilot.rotate(142);
		int empty = 1;
    		while(empty==1)
    		{
        		if (navBot.empty()==true) //if the stack has no values in it
        		{
        			empty=2;
        			System.out.println("The starting point has been reached");
            		float [] samplevalue =  new float [touchSensor.sampleSize()];
                    touchSensor.fetchSample(samplevalue,0);
                    System.out.println("value " +samplevalue[0]);
                    pilot.travel(1.0);
                    if (samplevalue[0]==1) 
                    {
                    		pilot.stop();
                    }
                    else if (getHSVColor()>150)
                    {
                    		pilot.stop();
                    }
        			Delay.msDelay(1000);
        			System.exit(0);
        		}
    			moveRobot();
    			if (navBot.peek() == 1) //if the stack has a 1 at the top
    			{
    				pilot.travel(2.00);
    				pilot.rotate(-71);
    				System.out.println("Going right on the way back");
    				
    			}
    			else if(navBot.peek() == 3)//if the stack has a 3 at the top
    			{
    				pilot.travel(2.00);
    				pilot.rotate(71);
    				pilot.forward();
    				System.out.println("Going left on the way back");
    			}
    			else if(navBot.peek() == 2)//if the stack has a 2 at the top
    			{
    				pilot.travel(2.00);
    				pilot.forward();
    				System.out.println("Going Straight on the way back");
    			}
    			navBot.pop(); //Get rid of the actiont the stack just did 
    		}
    			
    }
    
	//Method that converts RGB Color value to HSV Values
    public static double[] RGBtoHSV(Color colors){
        double[] hsv = new double[3];
        // read colors
        int r = colors.getRed();
        int b = colors.getBlue();
        int g = colors.getGreen();
        
        double min = Math.min(r, Math.min(b,g));
        double max = Math.max(r, Math.max(b, g));
        double delta = max - min;
        hsv[2] = max/255; //set v to max as a percentage
        if (max != 0){
            hsv[1] = delta/max;
        }
        else{ //r = b = g =0 
            hsv[1] = 0; //s = 0;        // s = 0, v is undefined
            hsv[0] = -1; //h = -1;
            return hsv;
        }
        
        if (r == max){
            hsv[0] = (g-b)/delta; //h 
        }
        else{
            if (g == max)
                hsv[0] = 2 + (b - r)/delta; //h
            else
                hsv[0] = 4 + (r - g)/delta; //h
        }
        
        hsv[0] *=60;    //degrees
        if (hsv[0] < 0)
            hsv[0] +=360;
        
        return hsv;
    }
    
    public static double getHSVColor() //this method allows us to get the HSV colors 
    {
            colorSensor.setRGBMode(); 
            colorSensor.setFloodLight(Color.WHITE);
            Color rgb = colorSensor.getColor();
        double[] hsv = RGBtoHSV(rgb);
       // System.out.println("HSV = " + "[ " + hsv[0] + "," + hsv[1] + "," + hsv[2] + "," +" ]");
        System.out.println(hsv[0]);
        return hsv[0];
        
    }
    public static float getAmbientColor() //this method allows us to get the Ambientcolors 
    {
    		colorSensor.setFloodLight(Color.WHITE);
    		float ambient = colorSensor.getAmbient();
    		return ambient;
    }
    
    public static float getRedColor() { //this method allows us to get the Red color to detect silver 
		colorSensor.setFloodLight(Color.RED);
		colorSensor.setRedMode();
		float red = colorSensor.getRed();
		System.out.println(red);
		return red;
	
    	}
    
}

