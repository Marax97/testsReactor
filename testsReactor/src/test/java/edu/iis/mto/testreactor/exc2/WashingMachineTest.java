package edu.iis.mto.testreactor.exc2;

import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;

public class WashingMachineTest {

    private WashingMachine washingMachine;

    private DirtDetector dirtDetector;
    private Engine engine;
    private WaterPump waterPump;

    private LaundryBatch laundryBatch;
    private ProgramConfiguration programConfiguration;

    @Before
    public void setUp() {
        dirtDetector = mock(DirtDetector.class);
        engine = mock(Engine.class);
        waterPump = mock(WaterPump.class);
        washingMachine = new WashingMachine(dirtDetector, engine, waterPump);

        programConfiguration = ProgramConfiguration.builder()
                                                   .withProgram(Program.SHORT)
                                                   .withSpin(true)
                                                   .build();

        laundryBatch = LaundryBatch.builder()
                                   .withWeightKg(5)
                                   .withType(Material.COTTON)
                                   .build();
    }

    @Test
    public void testIfLaudryIsTooHeavy() {
        laundryBatch = LaundryBatch.builder()
                                   .withWeightKg(10)
                                   .withType(Material.COTTON)
                                   .build();

        LaundryStatus laundryStatus = washingMachine.start(laundryBatch, programConfiguration);

        assertThat(laundryStatus.getResult(), Matchers.equalTo(Result.FAILURE));
        assertThat(laundryStatus.getErrorCode(), Matchers.equalTo(ErrorCode.TOO_HEAVY));
    }

    @Test
    public void testIfLaudryIsTooHeavyForWoolMaterial() {
        laundryBatch = LaundryBatch.builder()
                                   .withWeightKg(5)
                                   .withType(Material.WOOL)
                                   .build();

        LaundryStatus laundryStatus = washingMachine.start(laundryBatch, programConfiguration);

        assertThat(laundryStatus.getResult(), Matchers.equalTo(Result.FAILURE));
        assertThat(laundryStatus.getErrorCode(), Matchers.equalTo(ErrorCode.TOO_HEAVY));
    }

    @Test
    public void testifLaudryHasFinshedWithSuccess() {
        LaundryStatus laundryStatus = washingMachine.start(laundryBatch, programConfiguration);

        assertThat(laundryStatus.getResult(), Matchers.equalTo(Result.SUCCESS));
        assertThat(laundryStatus.getRunnedProgram(), Matchers.equalTo(Program.SHORT));
    }

    @Test
    public void testIfEngineWasCalledOnce() {
        washingMachine.start(laundryBatch, programConfiguration);

        verify(engine).runWashing(any(Integer.class));
    }

    @Test
    public void testIfEngineWasCalledOnceForSpinMode() {
        washingMachine.start(laundryBatch, programConfiguration);

        verify(engine).spin();
    }

    @Test
    public void testIfEngineSpinWontBeCalled() {
        programConfiguration = ProgramConfiguration.builder()
                                                   .withProgram(Program.SHORT)
                                                   .withSpin(false)
                                                   .build();
        washingMachine.start(laundryBatch, programConfiguration);

        verify(engine, never()).spin();
    }

    @Test
    public void testIfWaterPumpPourAndRelaseWasCalledOnce() {

        washingMachine.start(laundryBatch, programConfiguration);

        verify(waterPump).pour(any(Double.class));
        verify(waterPump).release();
    }

    @Test
    public void testIfDirtDetectorWillBeCalledForAutodetectProgram() {
        programConfiguration = ProgramConfiguration.builder()
                                                   .withProgram(Program.AUTODETECT)
                                                   .withSpin(true)
                                                   .build();

        when(dirtDetector.detectDirtDegree(laundryBatch)).thenReturn(new Percentage(50));

        washingMachine.start(laundryBatch, programConfiguration);

        verify(dirtDetector).detectDirtDegree(laundryBatch);
    }

    @Test
    public void testIfAutodetectProgramWillDetectProgramCorrectly() {
        programConfiguration = ProgramConfiguration.builder()
                                                   .withProgram(Program.AUTODETECT)
                                                   .withSpin(true)
                                                   .build();

        when(dirtDetector.detectDirtDegree(laundryBatch)).thenReturn(new Percentage(50));

        LaundryStatus laundryStatus = washingMachine.start(laundryBatch, programConfiguration);

        assertThat(laundryStatus.getRunnedProgram(), Matchers.equalTo(Program.LONG));

        when(dirtDetector.detectDirtDegree(laundryBatch)).thenReturn(new Percentage(20));

        laundryStatus = washingMachine.start(laundryBatch, programConfiguration);

        assertThat(laundryStatus.getRunnedProgram(), Matchers.equalTo(Program.MEDIUM));
    }

    @Test
    public void itCompiles() {
        assertThat(true, Matchers.equalTo(true));
    }

}
