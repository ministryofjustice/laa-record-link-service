package uk.gov.justice.record.link.service;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import uk.gov.justice.record.link.entity.CcmsUser;
import uk.gov.justice.record.link.respository.CcmsUserRepository;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ProdCcmsDataImportServiceTest {

    private CcmsUserRepository repository;
    private ProdCcmsDataImportService service;

    @BeforeEach
    public void setUp() {
        repository = mock(CcmsUserRepository.class);
        service = new ProdCcmsDataImportService(repository);
    }

    @Test
    void testImportExcel() throws Exception {
        when(repository.findByLoginId(anyString())).thenReturn(Optional.empty());

        // Create test Excel data
        InputStream inputStream = createTestExcelFile();

        service.importExcel(inputStream);

        ArgumentCaptor<CcmsUser> userCaptor = ArgumentCaptor.forClass(CcmsUser.class);
        verify(repository, times(2)).save(userCaptor.capture());

        List<CcmsUser> savedUsers = userCaptor.getAllValues();
        assertEquals(2, savedUsers.size());

        CcmsUser user1 = savedUsers.get(0);
        assertEquals("Johnson", user1.getLastName());
        assertEquals("Alice", user1.getFirstName());
        assertEquals("alice.johnson@testfirm.com", user1.getEmail());
        assertEquals("ajohnson", user1.getLoginId());
        assertEquals("TF001", user1.getFirmCode());
        assertEquals("Test Legal Firm", user1.getFirmName());

        CcmsUser user2 = savedUsers.get(1);
        assertEquals("Smith", user2.getLastName());
        assertEquals("Robert", user2.getFirstName());
        assertEquals("robert.smith@mocklaw.com", user2.getEmail());
        assertEquals("rsmith", user2.getLoginId());
        assertEquals("ML002", user2.getFirmCode());
        assertEquals("Mock Law Associates", user2.getFirmName());
    }

    @Test
    void testImportExcelSkipsExistingUsers() throws Exception {
        CcmsUser existingUser = new CcmsUser();
        when(repository.findByLoginId("ajohnson")).thenReturn(Optional.of(existingUser));
        when(repository.findByLoginId("rsmith")).thenReturn(Optional.empty());

        InputStream inputStream = createTestExcelFile();

        service.importExcel(inputStream);

        ArgumentCaptor<CcmsUser> userCaptor = ArgumentCaptor.forClass(CcmsUser.class);
        verify(repository, times(1)).save(userCaptor.capture());

        CcmsUser savedUser = userCaptor.getValue();
        assertEquals("rsmith", savedUser.getLoginId());
    }

    private InputStream createTestExcelFile() throws Exception {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Users");

        // header row
        Row headerRow = sheet.createRow(0);
        createCell(headerRow, 0, "CCMS_PROVIDERFIRM_ID");
        createCell(headerRow, 1, "PROVIDER_FIRM_NAME");
        createCell(headerRow, 2, "CWA_FIRM_CODE");
        createCell(headerRow, 3, "CCMS_USER_ID");
        createCell(headerRow, 4, "USER_LOGIN_ID");
        createCell(headerRow, 5, "EMAIL_ADDRESS");
        createCell(headerRow, 6, "SURNAME_FIRSTNAME");

        // first data row
        Row dataRow1 = sheet.createRow(1);
        createCell(dataRow1, 0, "10000");
        createCell(dataRow1, 1, "Test Legal Firm");
        createCell(dataRow1, 2, "TF001");
        createCell(dataRow1, 3, "50000");
        createCell(dataRow1, 4, "ajohnson");
        createCell(dataRow1, 5, "alice.johnson@testfirm.com");
        createCell(dataRow1, 6, "Johnson, Alice");

        // second data row
        Row dataRow2 = sheet.createRow(2);
        createCell(dataRow2, 0, "10001");
        createCell(dataRow2, 1, "Mock Law Associates");
        createCell(dataRow2, 2, "ML002");
        createCell(dataRow2, 3, "50008");
        createCell(dataRow2, 4, "rsmith");
        createCell(dataRow2, 5, "robert.smith@mocklaw.com");
        createCell(dataRow2, 6, "Smith, Robert");

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();

        return new ByteArrayInputStream(outputStream.toByteArray());
    }

    private void createCell(Row row, int columnIndex, String value) {
        Cell cell = row.createCell(columnIndex, CellType.STRING);
        cell.setCellValue(value);
    }
}
