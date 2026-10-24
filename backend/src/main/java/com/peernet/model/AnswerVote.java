package com.peernet.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "answer_votes", uniqueConstraints = @UniqueConstraint(columnNames = {"answer_id", "user_id"}))
@Getter @Setter @NoArgsConstructor
public class AnswerVote {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false) @JoinColumn(name = "answer_id")
    private Answer answer;
    @ManyToOne(optional = false) @JoinColumn(name = "user_id")
    private AppUser user;
    /** +1 for upvote, -1 for downvote */
    private int value;
}
