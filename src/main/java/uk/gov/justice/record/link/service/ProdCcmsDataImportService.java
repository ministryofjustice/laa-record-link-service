package uk.gov.justice.record.link.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import uk.gov.justice.record.link.entity.CcmsUser;
import uk.gov.justice.record.link.respository.CcmsUserRepository;

import java.io.IOException;
import java.io.InputStream;

@RequiredArgsConstructor
@Service
@Slf4j
public class ProdCcmsDataImportService {

    private final CcmsUserRepository ccmsUserRepository;
    
    @Value("${INGEST_PROD_DATA:false}")
    private boolean ingestProdData;

    @PostConstruct
    public void importExcel() {
        if (!ingestProdData) {
            log.info("Skipping production data import");
            return;
        }
        
        log.info("Starting production data import");
        try {
            ClassPathResource resource = new ClassPathResource("data/uesrs.xlsx");
            InputStream inputStream = resource.getInputStream();
            importExcel(inputStream);
            log.info("Production data import completed successfully");
        } catch (Exception e) {
            log.error("Error while reading file: {}", e.getMessage(), e);
        }
    }

    public void importExcel(InputStream inputStream) throws IOException {
        try (Workbook workbook = new XSSFWorkbook(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);
            
            boolean headerSkipped = false;
            for (Row row : sheet) {
                if (!headerSkipped) {
                    headerSkipped = true;
                    continue;
                }

                if (isRowEmpty(row)) {
                    continue;
                }
                
                try {
                    CcmsUser user = createCcmsUserFromRow(row);
                    
                    if (ccmsUserRepository.findByLoginId(user.getLoginId()).isPresent()) {
                        log.info("User with login ID {} already exists, skipping", user.getLoginId());
                        continue;
                    }
                    
                    ccmsUserRepository.save(user);
                    log.debug("Saved user: {}", user.getLoginId());
                    
                } catch (Exception e) {
                    log.warn("Error processing row {}: {}", row.getRowNum() + 1, e.getMessage());
                }
            }
        }
    }

    private CcmsUser createCcmsUserFromRow(Row row) {
        CcmsUser user = new CcmsUser();

        user.setFirmName(getCellValueAsString(row.getCell(1)));
        user.setFirmCode(getCellValueAsString(row.getCell(2)));
        user.setLoginId(getCellValueAsString(row.getCell(4)));
        user.setEmail(getCellValueAsString(row.getCell(5)));

        String surnameFirstname = getCellValueAsString(row.getCell(6));
        if (surnameFirstname != null && surnameFirstname.contains(",")) {
            String[] nameParts = surnameFirstname.split(",", 2);
            user.setLastName(nameParts[0].trim());
            user.setFirstName(nameParts[1].trim());
        } else {
            user.setLastName(surnameFirstname);
            user.setFirstName("");
        }
        
        return user;
    }
    
    private String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return null;
        }
        
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> String.valueOf((long) cell.getNumericCellValue());
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case FORMULA -> cell.getCellFormula();
            default -> null;
        };
    }
    
    private boolean isRowEmpty(Row row) {
        if (row == null) {
            return true;
        }
        
        for (int i = 0; i < row.getLastCellNum(); i++) {
            Cell cell = row.getCell(i);
            if (cell != null && cell.getCellType() != CellType.BLANK) {
                String value = getCellValueAsString(cell);
                if (value != null && !value.trim().isEmpty()) {
                    return false;
                }
            }
        }
        return true;
    }
}
