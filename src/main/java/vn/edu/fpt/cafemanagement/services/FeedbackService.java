package vn.edu.fpt.cafemanagement.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.fpt.cafemanagement.entities.Feedback;
import vn.edu.fpt.cafemanagement.repositories.FeedbackRepository;

import java.util.List;

@Service
public class FeedbackService {
    FeedbackRepository feedbackRepository;

    public FeedbackService(FeedbackRepository feedbackRepository) {
        this.feedbackRepository = feedbackRepository;
    }

    public List<Feedback> getAllFeedback (int proId) {
        return feedbackRepository.findFeedbackByProductProId(proId);
    }

    public void saveFeedback(Feedback feedback) {
        feedbackRepository.save(feedback);
    }

    public Feedback getFeedbackById(int feedbackId) {
        return feedbackRepository.findFeedbackByFeedbackId(feedbackId);
    }

    @Transactional
    public void deleteFeedback(int id) {
        feedbackRepository.deleteFeedbackByFeedbackId(id);
    }
}
