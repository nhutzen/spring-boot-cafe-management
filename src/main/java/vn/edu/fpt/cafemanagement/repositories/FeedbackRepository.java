package vn.edu.fpt.cafemanagement.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.edu.fpt.cafemanagement.entities.Feedback;

import java.util.List;

@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, Integer> {

    List<Feedback> findFeedbackByProductProId(int productId);

    Feedback findFeedbackByFeedbackId(int feedbackId);

    void deleteFeedbackByFeedbackId(int id);


}
