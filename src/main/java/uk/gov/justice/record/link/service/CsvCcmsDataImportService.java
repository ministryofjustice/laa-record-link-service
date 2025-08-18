package uk.gov.justice.record.link.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import uk.gov.justice.record.link.entity.CcmsUser;
import uk.gov.justice.record.link.respository.CcmsUserRepository;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

@RequiredArgsConstructor
@Service
public class CsvCcmsDataImportService {

    private final CcmsUserRepository ccmsUserRepository;

    @PostConstruct
    public void importCsv() {
        try {
            ClassPathResource resource = new ClassPathResource("data/ccms_dummy_data.csv");
            InputStream inputStream = resource.getInputStream();
            importCsv(inputStream);

        } catch (Exception e) {
            System.err.println("Error while reading ccms_dummy_data.csv " + e.getMessage());
        }
    }

    public void importCsv(InputStream inputStream) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        boolean headerSkipped = true;
        while ((line = reader.readLine()) != null) {
            if (headerSkipped) {
                headerSkipped = false;
                continue;
            }

            String[] tokens = line.split(",");
            if (tokens.length < 3) {
                continue;
            }

            String firmName = tokens[1].trim();
            String firmCode = tokens[2].trim();
            String userLogin = tokens[4].trim();
            String email = tokens[5].trim();
            String surname = tokens[6].trim().replace("\"", "");
            String firstName = tokens[7].trim().replace("\"", "");

            CcmsUser user = new CcmsUser();
            user.setFirmName(firmName);
            user.setLoginId(userLogin);
            user.setEmail(email);
            user.setLastName(surname);
            user.setFirstName(firstName);
            user.setFirmCode(firmCode);

            if (ccmsUserRepository.findByLoginId(user.getLoginId()).isPresent()) {
                System.out.println(userLogin + " already exists");
                break;
            }
            ccmsUserRepository.save(user);
        }
    }
}
