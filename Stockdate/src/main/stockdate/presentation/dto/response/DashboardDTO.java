package stockdate.presentation.dto.response;

import java.util.List;

// Dashboard summary: number of batches per tier and the batches that need attention (Expired and Red).
public record DashboardDTO(
        int expiredCount,
        int redCount,
        int yellowCount,
        int greenCount,
        List<BatchDTO> pinnedBatches
) {
}