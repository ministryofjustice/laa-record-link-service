package uk.gov.justice.record.link.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import uk.gov.justice.record.link.entity.CcmsUser;
import uk.gov.justice.record.link.respository.CcmsUserRepository;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class CsvCcmsDataImportServiceTest {

    private CcmsUserRepository repository;
    private CsvCcmsDataImportService service;

    @BeforeEach
    public void setUp() {
        repository = mock(CcmsUserRepository.class);
        service = new CsvCcmsDataImportService(repository);
    }

    @Test
    void testImportCsv() throws Exception {

        String csvData = " CCMS_PROVIDERFIRM_ID,PROVIDER_FIRM_NAME,CWA_FIRM_CODE,CCMS_USER_ID,USER_LOGIN_ID,EMAIL_ADDRESS,SURNAME_FIRSTNAME\n" +
                "10000,Schaefer & Co,20000,50000,cynthiawardthomas,cynthiawardthomas@schaeferandco.com,\"Ward-Thomas, Cynthia\" \n" +
                "10001,Pierce Associates,20001,50008,jenniferbass@pierceassociates.com,jenniferbass@pierceassociates.com,\"Bass, Jennifer\"\n";

        InputStream inputStream = new ByteArrayInputStream(csvData.getBytes());

        service.importCsv(inputStream);

        ArgumentCaptor<CcmsUser> userCaptor = ArgumentCaptor.forClass(CcmsUser.class);
        verify(repository, times(2)).save(userCaptor.capture());

        List<CcmsUser> savedUsers = userCaptor.getAllValues();
        assertEquals(2, savedUsers.size());

        CcmsUser user = savedUsers.get(0);
        assertEquals("Ward-Thomas", user.getLastName());
        assertEquals("Cynthia", user.getFirstName());
        assertEquals("cynthiawardthomas@schaeferandco.com", user.getEmail());
        assertEquals("cynthiawardthomas", user.getLoginId());
        assertEquals("20000", user.getFirmCode());
        assertEquals("Schaefer & Co", user.getFirmName());

        CcmsUser user2 = savedUsers.get(1);
        assertEquals("Bass", user2.getLastName());
        assertEquals("Jennifer", user2.getFirstName());
        assertEquals("jenniferbass@pierceassociates.com", user2.getEmail());
        assertEquals("jenniferbass@pierceassociates.com", user2.getLoginId());
        assertEquals("20001", user2.getFirmCode());
        assertEquals("Pierce Associates", user2.getFirmName());
    }
}