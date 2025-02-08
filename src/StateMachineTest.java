import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StateMachineTest {

    /*
    Structure for self Test State:
    1. All Pass
    2. OV, UV test
    3. not connected
    4. OT, UT test
    5. if Charging
    6. power off
    When multiple fault occur, the report will be based on the order of power, connection, voltage, temperature
     */
    @Test
    public void selfTestPass() {
        System.out.println("Self Test Pass");
        assertEquals("Init State", (StateMachine.selfTest(true, 3.4, 3.2, 30, 29, 0.2, true, false)));
    }

    @Test
    public void selfTestFailOV() {
        System.out.println("Self Test OV");
        assertEquals("Fault State", (StateMachine.selfTest(true, 4.3, 3.2, 30, 29, 0.2, true, false)));

    }

    @Test
    public void selfTestFailUV() {
        System.out.println("Self Test UV");
        assertEquals("Fault State", (StateMachine.selfTest(true, 3.4, 2.2, 30, 29, 0.2, true, false)));

    }

    @Test
    public void selfTestFailOT() {
        System.out.println("Self Test OT");
        assertEquals("Fault State", (StateMachine.selfTest(true, 3.4, 3.2, 65, 29, 0.2, true, false)));

    }

    @Test
    public void selfTestFailUT() {
        System.out.println("Self Test UT");
        assertEquals("Fault State", (StateMachine.selfTest(true, 3.4, 3.2, 30, -29, 0.2, true, false)));

    }

    @Test
    public void selfTestNotConnected() {
        System.out.println("Self Test units not connected");
        assertEquals("Fault State", (StateMachine.selfTest(false, 3.4, 3.2, 30, 29, 0.2, true, false)));
    }

    @Test
    public void selfTestPowerOff() {
        System.out.println("Self Test Power off");
        assertEquals("Fault State", (StateMachine.selfTest(true, 3.4, 3.2, 30, 29, 0.2, false, false)));
    }

    @Test
    public void selfTestCharging() {
        System.out.println("Self Test Charger detected");
        assertEquals("Fault State", (StateMachine.selfTest(true, 3.4, 3.2, 30, 29, 0.2, true, true)));
    }

    /*
    init state test
    1.initialization success
    2. initialization failed
     */

    @Test
    public void initializationSuccess() {
        System.out.println("Initialization Success");
        assertEquals("Normal State", StateMachine.initState(true));
    }

    @Test
    public void initializationFailed() {
        System.out.println("Initialization Failed");
        assertEquals("Fault State", StateMachine.initState(false));
    }

    /*
    Normal State test

     */

    @Test
    public void normalStateStay() {
        System.out.println("Normal State stay");
        assertEquals("Normal State", StateMachine.normalState(true, 3.7, 3.7, 45,
                42, 20, true, 0, false, true, 20, false, false));
    }

    @Test
    public void normalStateTransitCharging() {
        System.out.println("Transit Charging Test");
        assertEquals("Charging State", StateMachine.normalState(true, 3.7, 3.7, 40,
                35, 0.2, true, 0, false, true, 0, true, false));
    }

    @Test
    public void normalStateTransitChargingFail() {
        System.out.println("Transit Charging failed");
        assertEquals("Normal State", StateMachine.normalState(true, 3.7, 3.7, 47,
                35, 0.2, true, 0, false, true, 0, true, false));
        assertEquals("Normal State", StateMachine.normalState(true, 3.7, 3.7, 47,
                35, 15, true, 0, false, true, 0, true, false));
    }

    @Test
    public void normalStateTransitSleep() {
        System.out.println("Transit to Sleep test");
        assertEquals("Sleep State", StateMachine.normalState(true, 3.7, 3.7, 45,
                35, 5, true, 130, false, true, 0, false, false));
    }

    @Test
    public void normalStateTransitSleepFailed() {
        System.out.println("Unsuccessful by Sleep time");
        assertEquals("Normal State", StateMachine.normalState(true, 3.7, 3.7, 45,
                35, 5, true, 100, false, true, 0, false, false));
        System.out.println("Unsuccessful by over sleep current");
        assertEquals("Normal State", StateMachine.normalState(true, 3.7, 3.7, 45,
                35, 20, true, 0, false, true, 0, false, false));

    }

    @Test
    public void normalToDischarging() {
        System.out.println("Normal to Discharging state");
        assertEquals("Discharging State", StateMachine.normalState(true, 3.7, 3.7, 45,
                35, 5, true, 100, true, true, 0, false, false));
    }

    /*
    Current Fault
     */
    @Test
    public void dischargingFault() {
        System.out.println("Discharging Fault Test");
        System.out.println("180A more than 10 second");
        assertEquals("Fault State", StateMachine.normalState(true, 3.7, 3.7, 45,
                35, 120, true, 0, false, false , 11, false, false));
        System.out.println("120A without air cooling");
        assertEquals("Fault State", StateMachine.normalState(true, 3.7, 3.7, 45,
                35, 120, true, 0, false, false , 11, false, false));
    }

    @Test
    public void generalFaultTest() {
        System.out.println("General Fault Test");
        System.out.println("UV");
        assertEquals("Fault State", StateMachine.normalState(true, 3.7, 2.2, 45,
                42, 20, true, 0, false, true, 20, false, false));
        System.out.println("OV");
        assertEquals("Fault State", StateMachine.normalState(true, 4.5, 3.7, 45,
                42, 20, true, 0, false, true, 20, false, false));
        System.out.println("OT");
        assertEquals("Fault State", StateMachine.normalState(true, 3.7, 3.7, 65,
                42, 20, true, 0, false, true, 20, false, false));
        System.out.println("UT");
        assertEquals("Fault State", StateMachine.normalState(true, 3.7, 3.7, 45,
                -21, 20, true, 0, false, true, 20, false, false));
        System.out.println("Power off");
        assertEquals("Fault State", StateMachine.normalState(true, 3.7, 3.7, 45,
                21, 20, false, 0, false, true, 20, false, false));
        System.out.println("Disconnection");
        assertEquals("Fault State", StateMachine.normalState(false, 3.7, 3.7, 45,
                21, 20, true, 0, false, true, 20, false, false));
    }

    @Test
    public void WarningStateTest() {
        System.out.println("General Fault Test");
        System.out.println("UV");
        assertEquals("Warning State", StateMachine.normalState(true, 3.7, 2.6, 45,
                42, 20, true, 0, false, true, 20, false, false));
        System.out.println("OV");
        assertEquals("Warning State", StateMachine.normalState(true, 4.0, 3.7, 45,
                42, 20, true, 0, false, true, 20, false, false));
        System.out.println("OT");
        assertEquals("Warning State", StateMachine.normalState(true, 3.7, 3.7, 59,
                42, 20, true, 0, false, true, 20, false, false));
        System.out.println("UT");
        assertEquals("Warning State", StateMachine.normalState(true, 3.7, 3.7, 45,
                -19, 20, true, 0, false, true, 20, false, false));
    }





    /*
    Sleep State test
     */

    @Test
    public void sleepStateStay() {
        System.out.println ("Sleep State Basic Test");
        assertEquals("Sleep State", StateMachine.sleepState(true, 3.7,3.7,
                45, 30, 4, true, 200, false, false, false));
    }

    @Test
    public void sleepToCharging() {
        System.out.println("Sleep State charging");
        assertEquals("Charging State", StateMachine.sleepState(true, 3.7,3.7,
                40, 30, 4, true, 200, false, true, false));
    }

    @Test
    public void sleepToChargingFailed() {
        System.out.println("Transit to charging state failed, ");
        assertEquals("Sleep State", StateMachine.sleepState(true, 3.7,3.7,
                47, 30, 4, true, 200, false, true, false));
    }

    @Test
    public void sleepToNormal() {
        System.out.println("Transit to Normal State");
        assertEquals("Normal State", StateMachine.sleepState(true, 3.7,3.7,
                47, 30, 20, true, 200, false, false, false));
    }

    @Test
    public void toNormalWithChargerOn() {
        System.out.println("Transit to Normal State with charger on");
        assertEquals("Normal State", StateMachine.sleepState(true, 3.7,3.7,
                47, 30, 20, true, 200, false, true, false));
    }

    @Test
    public void toDischargingState() {
        System.out.println("Transit to discharging state by pressing button");
        assertEquals("Discharging State", StateMachine.sleepState(true, 3.7,3.7,
                    47, 30, 5, true, 200, true, false, false));
        System.out.println("Transit to discharging state by time");
        assertEquals("Discharging State", StateMachine.sleepState(true, 3.7,3.7,
                47, 30, 5, true, 980, true, false, false));

    }

/*
Discharging state test
 */
    @Test
    public void dischargingStayTest() {
        System.out.println("Discharging Stay Test");
        assertEquals("Discharging State", StateMachine.dischargingState(true, 3.7,3.7,
                45, 40, 5, true, false, 80, false));

    }

    @Test
    public void dischargingFaultTest() {
        System.out.println("Discharging Fault Test");
        System.out.println("Discharging current higher than the standard discharging current");
        assertEquals("Fault State", StateMachine.dischargingState(true, 3.7,3.7,
                45, 40, 30, true, false, 80, false));
        System.out.println("Charging during the discharging state");
        assertEquals("Fault State", StateMachine.dischargingState(true, 3.7,3.7,
                45, 40, 25, true, false, 80, true));

    }

    @Test
    public void dischargingToDeepSleep() {
        System.out.println("Discharging State to Deep Sleep State");
        assertEquals("Deep Sleep", StateMachine.dischargingState(true, 3.7,3.7,
                45, 40, 5, true, false, 30, false));
    }

    @Test
    public void toInitStateButton() {
        System.out.println("Discharging to init state by pressing the wake button");
        assertEquals("Init State", StateMachine.dischargingState(true, 3.7,3.7,
                45, 40, 5, true, true, 50, false));
    }


    /*
    Charging State Test
     */

    @Test
    public void chargingStateTest() {
        System.out.println("Charging State stay test");
        assertEquals("Charging State", StateMachine.chargingState(true, 3.7, 3.7,
                33, 33, -10, true, 5,
                false, false, true, true));
    }

    @Test
    public void chargingFaultTest() {
        System.out.println("Charging Fault Test");
        System.out.println("120A for more than 10 seconds");
        assertEquals("Fault State", StateMachine.chargingState(true, 3.7, 3.7,
                33, 33, -120, true, 11,
                false, false, true, true));
        System.out.println("Air Cooling off, more than 10 seconds, 20A");
        assertEquals("Fault State", StateMachine.chargingState(true, 3.7, 3.7,
                33, 33, -20, true, 11,
                false, false, false, true));

    }

    @Test
    public void chargingToSleep() {
        System.out.println("Charging State to Sleep State test.");
        System.out.println("SOC full");
        assertEquals("Sleep State", StateMachine.chargingState(true, 3.7, 3.7,
                33, 33, -18, true, 5,
                false, true, true, true));
        System.out.println("Charger Removed");
        assertEquals("Sleep State", StateMachine.chargingState(true, 3.7, 3.7,
                33, 33, -18, true, 5,
                false, false, true, false));
        System.out.println("Time Limit");
        assertEquals("Sleep State", StateMachine.chargingState(true, 3.7, 3.7,
                33, 33, -50, true, 10,
                false, false, true, true));
    }

    @Test
    public void chargingToNormal() {
        System.out.println("Charging State to Normal State Test");
        assertEquals("Normal State", StateMachine.chargingState(true, 3.7, 3.7,
                33, 33, 20, true, 5,
                false, false, true, true));
    }

    @Test
    public void chargingToDischarging() {
        System.out.println("Charging State to Discharging State");
        assertEquals("Discharging State", StateMachine.chargingState(true, 3.7, 3.7,
                33, 33, -18, true, 5,
                true, false, true, true));
    }


    /*
    Warning State
     */

    @Test
    public void warningState() {
        System.out.println("Warning State Stay test");
        assertEquals("Warning State", StateMachine.warningState(false, 2, true,
                4.1, 3.7, 35, 35, 50, true, "Normal State"));
    }

    @Test
    public void backToPreviousState() {
        System.out.println("Back to previous State");
        System.out.println("When the value is out of the danger zone");
        assertEquals("Normal State", StateMachine.warningState(false, 2, true,
                3.8, 3.7, 35, 35, 50, true, "Normal State"));
        System.out.println("When manually pressed the button to return to the previous state");
        assertEquals("Normal State", StateMachine.warningState(true, 2, true, 4.1,
                3.7, 35, 35, 50, true, "Normal State"));
    }

    @Test
    public void warningToFaultState() {
        System.out.println("Warning to Fault State Test");
        System.out.println("When pass time limit");
        assertEquals("Fault State", StateMachine.warningState(false, 15, true,
                4.1, 3.7, 35, 35, 50, true, "Normal State"));
        System.out.println("When the value reach the default zone");
        assertEquals("Fault State", StateMachine.warningState(false, 2, true,
                4.5, 3.7, 35, 35, 50, true, "Normal State"));
    }


    /*
    Fault State
     */

    @Test
    public void faultStateTest() {
        System.out.println("Button pressed");
        assertEquals("Deep Sleep", StateMachine.faultState(true));
        System.out.println("Button not pressed");
        assertEquals("Fault State", StateMachine.faultState(false));
    }

    /*
    Deep Sleep
     */

    @Test
    public void buttonPressed() {
        System.out.println("Button pressed");
        assertEquals("Self Test", StateMachine.deepSleepState(true));
        System.out.println("Button not pressed");
        assertEquals("Deep Sleep", StateMachine.deepSleepState(false));
    }











}