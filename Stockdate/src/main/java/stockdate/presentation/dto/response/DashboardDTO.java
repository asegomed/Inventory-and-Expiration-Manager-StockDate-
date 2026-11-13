package stockdate.presentation.dto.response;

import java.util.List;

// Dashboard summary (UC-05): number of batches per tier, the batches that need attention (Expired and Red)
// and the next batch to expire (null when every batch is expired or there are none).
public record DashboardDTO(
        int expiredCount,
        int redCount,
        int yellowCount,
        int greenCount,
        List<BatchDTO> pinnedBatches,
        BatchDTO nextExpiring
) {
}
