package uk.gov.justice.record.link.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.justice.record.link.entity.CcmsUser;
import uk.gov.justice.record.link.entity.LinkedRequest;
import uk.gov.justice.record.link.entity.Status;

import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class DataDownloadService {

    public String escapeCsv(String input) {
        if (input == null) {
            return "";
        }
        if (input.contains(",") || input.contains("\"") || input.contains("\n") || input.contains("\r")) {
            return "\"" + input.replace("\"", "\"\"") + "\"";
        }
        return input;
    }

    public String formatDate(LocalDateTime date, DateTimeFormatter formatter) {
        return date != null ? date.format(formatter) : "";
    }

    public DateTimeFormatter dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE;

    public String formatStatus(Status status) {
        return status != null ? status.name() : "";
    }

    public String formatLoginId(CcmsUser ccmsUser) {
        return ccmsUser != null ? ccmsUser.getLoginId() : "";
    }

    public String fileNameDateFormatter(LocalDateTime dateTime, DateTimeFormatter formatter) {
        return dateTime.format(formatter);
    }

    public void writeLinkedRequestsToWriter(PrintWriter writer, String columns, List<LinkedRequest> linkedRequests) {
        writer.println(columns);

        for (LinkedRequest request : linkedRequests) {
            String row = String.format(Locale.ROOT, "%s,%s,%s,%s,%s,%s,%s,%s,%s,%s",
                    request.getOldLoginId(),
                    escapeCsv(request.getIdamFirmName()),
                    escapeCsv(request.getIdamFirmCode()),
                    escapeCsv(formatDate(request.getCreatedDate(), dateFormatter)),
                    escapeCsv(formatDate(request.getAssignedDate(), dateFormatter)),
                    escapeCsv(formatDate(request.getDecisionDate(), dateFormatter)),
                    escapeCsv(formatStatus(request.getStatus())),
                    escapeCsv(request.getDecisionReason()),
                    escapeCsv(request.getLaaAssignee()),
                    escapeCsv(formatLoginId(request.getCcmsUser())));
            writer.println(row);
        }
    }

}
