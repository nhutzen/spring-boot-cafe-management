package vn.edu.fpt.cafemanagement.services;

import org.springframework.stereotype.Service;
import vn.edu.fpt.cafemanagement.entities.PointHistory;
import vn.edu.fpt.cafemanagement.repositories.PointHistoryRepository;

import java.util.List;
import java.util.Optional;

@Service
public class PointHistoryService {
    private final PointHistoryRepository pointHistoryRepository;

    public PointHistoryService(PointHistoryRepository pointHistoryRepository) {
        this.pointHistoryRepository = pointHistoryRepository;
    }

    public List<PointHistory> getAllHistories() {
        return pointHistoryRepository.findAll();
    }

    public Optional<PointHistory> getHistoryById(int id) {
        return pointHistoryRepository.findById(id);
    }

    public PointHistory saveHistory(PointHistory history) {
        return pointHistoryRepository.save(history);
    }

    public void deleteHistory(int id) {
        pointHistoryRepository.deleteById(id);
    }
}
