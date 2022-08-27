package patient.service.medical;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import ru.netology.patient.entity.BloodPressure;
import ru.netology.patient.entity.HealthInfo;
import ru.netology.patient.entity.PatientInfo;
import ru.netology.patient.repository.PatientInfoRepository;
import ru.netology.patient.service.alert.SendAlertService;
import ru.netology.patient.service.medical.MedicalServiceImpl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;

public class MedicalServiceImplTest {
    MedicalServiceImpl sut;
    public static PatientInfoRepository patientInfoRepository;
    public static SendAlertService alertService;

    @BeforeEach
    public void init(){
        patientInfoRepository = Mockito.mock(PatientInfoRepository.class);
        Mockito.when(patientInfoRepository.getById(any())).thenReturn(new PatientInfo(
                "9d26c527-8b49-4d20-a512-e325000e5e2e",
                "Семен",
                "Петров",
                LocalDate.of(1990, 12, 25),
                new HealthInfo(
                        new BigDecimal("36.5"),
                        new BloodPressure(130, 80)
                )
        ));
        alertService = Mockito.mock(SendAlertService.class);
        sut = new MedicalServiceImpl(patientInfoRepository, alertService);
    }

    @ParameterizedTest
    @MethodSource
    public void checkBloodPressureTest(String patientId, BloodPressure bloodPressure, int count) {
        sut.checkBloodPressure(patientId, bloodPressure);
        Mockito.verify(alertService, Mockito.times(count)).send(Mockito.anyString());
    }

    public static Stream<Arguments> checkBloodPressureTest() {
        return Stream.of(
                Arguments.of(UUID.randomUUID().toString(), new BloodPressure(120, 80), 1),
                Arguments.of(UUID.randomUUID().toString(), new BloodPressure(130, 90), 1),
                Arguments.of(UUID.randomUUID().toString(), new BloodPressure(120, 70), 1),
                Arguments.of(UUID.randomUUID().toString(), new BloodPressure(110, 80), 1),
                Arguments.of(UUID.randomUUID().toString(), new BloodPressure(130, 80), 0)
        );
    }

    @ParameterizedTest
    @MethodSource
    public void checkTemperatureTest(String patientId, BigDecimal temperature, int count) {
        sut.checkTemperature(patientId, temperature);
        Mockito.verify(alertService, Mockito.times(count)).send(Mockito.anyString());
    }

    public static Stream<Arguments> checkTemperatureTest() {
        return Stream.of(
                Arguments.of(UUID.randomUUID().toString(), new BigDecimal("36.8"), 0),
                Arguments.of(UUID.randomUUID().toString(), new BigDecimal("34.9"), 1)
        );
    }

    @Test
    public void testSendAlertService() {
        ArgumentCaptor<String> aCaptor = ArgumentCaptor.forClass(String.class);
        sut.checkBloodPressure("4ab2fe14-bf25-4e53-ac16-5003c7c407b1", new BloodPressure(120, 100));
        Mockito.verify(alertService).send(aCaptor.capture());
        assertEquals("Warning, patient with id: 9d26c527-8b49-4d20-a512-e325000e5e2e, need help", aCaptor.getValue());
    }

    @AfterEach
    public void finalized(){
        sut = null;
        patientInfoRepository = null;
        alertService = null;
    }
}
