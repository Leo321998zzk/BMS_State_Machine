


// Important, time below represent the time of certain function's lasting time.
public class StateMachine {


    //overVoltage threshold in V
    static double overVoltage = 4.2;

    //under Voltage threshold in V
    static double underVoltage = 2.5;

    //sleep current threshold in A
    static double sleepCurrent = 10;

    //Charging over temperature threshold
    static double overChargingTemperature = 45.0;

    //Charging under temperature threshold
    static double underChargingTemperature = 0.0;

    //Discharging over temperature threshold Celsius;
    static double overDischargingTemperature = 60.0;

    //Discharging over temperature threshold in degree Celsius
    static double underDischargingTemperature = -20.0;

    //Optimal SOC for discharging and deep sleep state;
    static double optimalCapacity = 40;

    /*
    Deep Sleep State for the battery
     */
    public static String deepSleepState(boolean pressedButton) {
        if (pressedButton) {
            System.out.println("Turn on BMS, transit to self test state");
            return "Self Test";
        }
        return "Deep Sleep";
    }

    /*
    Self Test state before initialization
    Checking if all units in BMS are connected
    maxVolt: highest volt between cells
    minVolt: lowest vot between cells
    maxTemp: highest temperature
    minTemp: lowest Temperature
     */
    public static String selfTest(boolean connected, double maxVolt, double minVolt, double maxTemp, double minTemp, double current, boolean power, boolean charger) {
        if(!power) {
            System.out.println("No power supply, battery transit to fault state!");
            return "Fault State";
        }
        if (!connected) {
            System.out.println("Units are not fully connected, state transit to fault state!");
            return "Fault State";
        }

        if (charger) {
            System.out.println("Transit to fault state for not removing the charger during the self test state");
            return "Fault State";
        }
        if (!voltTest(maxVolt, minVolt)) {
            if (maxVolt >= overVoltage) {
                System.out.println("Transit to fault state for the risk of OV");
                return "Fault State";
            } else {
                System.out.println("Transit to fault state for the risk of UV");
                return "Fault State";
            }
        }

        if(!tempTest(maxTemp, minTemp, current)) {
            if (maxTemp >= overDischargingTemperature) {
                System.out.println("Transit to fault state for the risk of over temperature");
                return "Fault State";
            } else {
                System.out.println("Changing to fault state for the risk of under temperature");
                return "Fault State";
            }
        }

        System.out.println("Self test success, going to initialization state");

        return "Init State";


    }

    // All fault will be detected during the self test State
    public static String initState(boolean initializationSuccess) {
        if (initializationSuccess) {
            System.out.println("Initialization of the BMS success, automatically transit to Normal State");
            return "Normal State";
        } else {
            System.out.println("Initialization of the BMS failed, transit to fault state");
            return "Fault State";
        }
    }
    /*
    Normal State
     */
    public static String normalState(boolean connected, double maxVolt, double minVolt, double maxTemp,
                              double minTemp, double current, boolean power, double sleepTime,
                                     boolean sleepButton, boolean airCooling, double dischargingTime, boolean charger, boolean SOCFull) {

        //Shut down circuit included in power, since shut down circuit will shut down the whole race car circuit
        if(!faultDetection(connected, maxVolt, minVolt, maxTemp, minTemp, current, power)) {
            return "Fault State";
        }

        if(!dischargingFaultDetection(current, dischargingTime, airCooling)) {
            System.out.println("Improper discharging");
            return "Fault State";
        }

        if (charger) {
            if (current > sleepCurrent) {
                System.out.println("Discharging sensed, stay in normal state");
                return "Normal State";
            }
            if (SOCFull) {
                System.out.println("Battery Full");
                return "Normal State";
            }
            if (maxTemp < overChargingTemperature && minTemp > underChargingTemperature) {
                System.out.println("Transit to charging state");
                return "Charging State";
            } else {
                System.out.println("Incorrect temperature zone for charging, stay at normal state");
                return "Normal State";
            }
        }

        if(!warningStateDetection(maxVolt, minVolt, maxTemp, minTemp, current)) {
            return "Warning State";
        }
        if (current <= sleepCurrent && current >= 0) {
            if (sleepTime >= 120) {
                System.out.println("Automatically transit to sleep state");
                return "Sleep State";
            }
        } else {
            sleepTime = 0;
        }
        if (sleepButton && current <= sleepCurrent) {
            System.out.println("Button clicked, transit to discharging state");
            return "Discharging State";
        }

        return "Normal State";
    }

    public static String warningState(boolean returnButton, double time, boolean connected, double maxVolt, double minVolt, double maxTemp,
                                      double minTemp, double current, boolean power, String previousState) {
        if (!faultDetection(connected, maxVolt, minVolt, maxTemp, minTemp, current, power)) {
            return "Fault State";
        }

        if (warningStateDetection(maxVolt, minVolt, maxTemp, minTemp, current)) {
            System.out.println("Return to " + previousState);
            return previousState;
        }

        if (returnButton) {
            return previousState;
        }

        if (time >= 10) {
            System.out.println("Automatically transit to Fault State.");
            return "Fault State";
        }

        return "Warning State";
    }

    public static String chargingState(boolean connected, double maxVolt, double minVolt, double maxTemp,
                                       double minTemp, double current, boolean power, double time,
                                       boolean sleepButton, boolean socFull, boolean airCooling, boolean charger) {

        //All current flow to the Accumulator must stop immediately
        if (!faultDetection(connected, maxVolt, minVolt, maxTemp, minTemp, current, power)) {
            System.out.println("Transit to fault state");
            return "Fault State";
        }

        if(!warningStateDetection(maxVolt, minVolt, maxTemp, minTemp, current)) {
            return "Warning State";
        }

        if (!chargingFaultDetection(current, time, airCooling)) {
            System.out.println("Improper charging, transit to fault state");
            return "Fault State";
        }

        if (time == 10 && current >= -120 && current <= -20) {
            System.out.println("Automatically transit to sleep state");
            return "Sleep State";
        }

        if (current > sleepCurrent && time > 1) {
            System.out.println("Discharging sensed, transit to normal state");
            return "Normal State";
        }

        if (!charger) {
            System.out.println("Charger Removed, transit to sleep state");
            return "Sleep State";
        }

        if (socFull) {
            System.out.println("Fully charged, transit to sleep state");
            return "Sleep State";
        }

        if (sleepButton) {
            System.out.println("Transit to discharging state");
            return "Discharging State";
        }

        return "Charging State";
    }

    public static String sleepState(boolean connected, double maxVolt, double minVolt, double maxTemp,
                                    double minTemp, double current, boolean power, double time,
                                    boolean sleepButton, boolean charger, boolean socFull) {
        if (!faultDetection(connected, maxVolt, minVolt, maxTemp, minTemp, current, power)){
            return "Fault State";
        }

        if (!warningStateDetection(maxVolt, minVolt, maxTemp, minTemp, current)) {
            return "Warning State";
        }



        if (current <= sleepCurrent && time > 15 * 60 && sleepCurrent > 0) {
            System.out.println("Automatically transit to discharging state");
            return "Discharging State";
        }

        if (current > sleepCurrent && time > 1) {
            System.out.println("Discharging sensed, transit to normal state");
            return "Normal State";
        }

        if (charger) {
            if (maxTemp > 45 || minTemp < 0) {
                System.out.println("Improper charging temperature zone");
                return "Sleep State";
            }
            if (socFull) {
                System.out.println("SOC Full");
                return "Sleep State";
            }
            System.out.println("Charger sensed, transit to charging state");
            return "Charging State";
        }

        if(sleepButton) {
            System.out.println("Deep Sleep button pressed, transit to discharging state");
            return "Discharging State";
        }

        return "Sleep State";



    }

    //Only can transit back to normal state when button got pressed
    //If charging or discharging sensed without button pressed then go to fault state

    public static String dischargingState(boolean connected, double maxVolt, double minVolt, double maxTemp,
                                          double minTemp, double current, boolean power,
                                          boolean wakeButton, double SOC, boolean charger) {

        //Maximum discharging current in discharging state
        double standardDischargingCurrent = 20;
        if (!faultDetection(connected, maxVolt, minVolt, maxTemp, minTemp, current, power)) {
            System.out.println("Transit to fault state");
            return "Fault State";
        }

        if (current > standardDischargingCurrent) {
            System.out.println("Discharging current sensed, transit to fault state.");
            return "Fault State";
        }

        if (charger && current > sleepCurrent) {
            System.out.println("Charging current sensed, transit to fault state.");
            return "Fault State";

        }

        if (SOC <= optimalCapacity) {
            System.out.println("Automatically transit to deep sleep when optimal SOC");
            return "Deep Sleep";
        }

        if (wakeButton) {
            System.out.println("Manually pressed the button, transit to Init State");
            return "Init State";
        }

        return "Discharging State";


    }

    public static String faultState(boolean buttonPressed) {
        if (buttonPressed) {
            System.out.println("Button pressed, transit to deep sleep state");
            return "Deep Sleep";
        }
        return "Fault State";

    }

    

    /*
    General test for volt and temp
     */
    private static boolean voltTest(double maxVolt, double minVolt) {
        return maxVolt < overVoltage && minVolt > underVoltage;
    }

    /*
    Current test if charging or discharging
    true: charging, false: discharging
    Incoming current is negative
     */
    private static boolean isCharging(double current) {
        return current < 0;
    }
    /*
    Measure charging or discharging first, then check if the temperature is in the interval
     */
    private static boolean tempTest(double maxTemp, double minTemp, double current) {
        if (isCharging(current)) {
            return maxTemp < overChargingTemperature && minTemp > underChargingTemperature;
        } else {
            return maxTemp < overDischargingTemperature && minTemp > underDischargingTemperature;
        }
    }

    /*
    When one fault detected, change to fault state directly, based on the rule of SAE
    Connected: Including sensors also units in BMS.
     */
    private static boolean faultDetection(boolean connected, double maxVolt, double minVolt, double maxTemp, double minTemp, double current, boolean power) {

        if (!power) {
            System.out.println("Power disconnected, transit to fault state!!");
            return false;
        }

        if (!connected) {
            System.out.println("Missing part of the BMS connection, transit to fault state!!");
            return false;
        }



        if (maxVolt >  overVoltage) {
            System.out.println("Over voltage, transit to fault state!!");
            return false;
        }

        if (minVolt < underVoltage) {
            System.out.println("Under voltage, transit to fault state!!");
            return false;
        }

        if (isCharging(current)) {
            if (minTemp < underChargingTemperature) {
                System.out.println("Under minimum charging temperature, transit to fault state!!");
                return false;
            } else if(maxTemp > overChargingTemperature) {
                System.out.println("Over maximum charging temperature, transit to fault state!!");
                return false;
            }
        } else {
            if (minTemp < underDischargingTemperature) {
                System.out.println("Under minimum discharging temperature, transit to fault state!!");
                return false;
            } else if(maxTemp > overDischargingTemperature) {
                System.out.println("over discharging maximum temperature, transit to fault state");
                return false;
            }
        }

        return true;


    }

    private static boolean warningStateDetection(double maxVolt, double minVolt, double maxTemp, double minTemp, double current) {
        if (maxVolt > overVoltage * 0.95) {
            System.out.println("Risk of over voltage, transit to Warning State");
            return false;
        }

        if (minVolt < underVoltage * 1.05) {
            System.out.println("Risk of under voltage, transit to Warning State");
            return false;
        }

        if (isCharging(current)) {
            if (maxTemp > overChargingTemperature * 0.95) {
                System.out.println("Risk of over temperature, transit to Warning State");
                return false;
            }

            //It is 0
            if (minTemp < underChargingTemperature + 2) {
                System.out.println("Risk of under temperature, transit to Warning State");
                return false;
            }
        } else {
            if (maxTemp > overDischargingTemperature * 0.95) {
                System.out.println("Risk of over temperature, transit to warning state");
                return false;

            }
            //Negative value
            if (minTemp < underDischargingTemperature * 0.9) {
                System.out.println("Risk of under temperature, transit to warning state");
                return false;
            }
        }
        return true;
    }

    private static boolean chargingFaultDetection(double current, double time, boolean airCooling) {
        if (current >= -20 && airCooling) {
            return true;
        } else if (current >= -15) {
            return true;
        } else if (current >= -120 && time <= 10) {
            return true;
        }

        return false;
    }

    private static boolean dischargingFaultDetection(double current, double time, boolean airCooling) {
        if (airCooling && current <= 120) {
            return true;
        } else if (current <= 60) {
            return true;
        } else return current <= 180 && time < 10;
    }
}
